#!/usr/bin/env python3
"""
PathoVision BreakHis Training Script - Offline Compatible Version
Robust training pipeline with fallback mechanisms for network issues
"""

import os
import random
import numpy as np
import pandas as pd
from glob import glob
from PIL import Image
import matplotlib.pyplot as plt
import seaborn as sns
from tqdm import tqdm

import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
import torchvision.transforms as T
import torchvision.models as models

from sklearn.model_selection import train_test_split
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score, f1_score,
    confusion_matrix, roc_curve, auc, classification_report
)

# ============================================================================
# CONFIGURATION
# ============================================================================

CONFIG = {
    'data_root': '/kaggle/input/breakhis',  # Change this for local training
    'seed': 42,
    'img_size': 224,
    'batch_size': 32,
    'epochs': 30,
    'learning_rate': 1e-3,
    'weight_decay': 1e-5,
    'dropout': 0.5,
    'label_smoothing': 0.1,
    'early_stopping_patience': 8,
    'early_stopping_min_delta': 0.002,
    'device': 'cuda' if torch.cuda.is_available() else 'cpu'
}

# ============================================================================
# SET SEEDS FOR REPRODUCIBILITY
# ============================================================================

def set_seed(seed=42):
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)
    torch.backends.cudnn.deterministic = True
    torch.backends.cudnn.benchmark = False
    print(f'✓ Random seed set to {seed}')

# ============================================================================
# DATA LOADING WITH ERROR HANDLING
# ============================================================================

def load_dataset(data_root, config):
    """Load and validate BreakHis dataset"""
    print('\n' + '='*60)
    print('LOADING DATASET')
    print('='*60)
    
    # Check path exists
    if not os.path.exists(data_root):
        print(f'✗ Error: Dataset path not found: {data_root}')
        raise FileNotFoundError(f'Dataset not found at {data_root}')
    
    # Find images
    benign_paths = glob(os.path.join(data_root, '**', 'benign', '**', '*.png'), recursive=True)
    malignant_paths = glob(os.path.join(data_root, '**', 'malignant', '**', '*.png'), recursive=True)
    
    print(f'Initial Benign images: {len(benign_paths)}')
    print(f'Initial Malignant images: {len(malignant_paths)}')
    
    if len(benign_paths) == 0 or len(malignant_paths) == 0:
        raise ValueError(f'No images found. Check dataset structure.')
    
    # Create dataframe
    all_paths = benign_paths + malignant_paths
    all_labels = [0] * len(benign_paths) + [1] * len(malignant_paths)
    df = pd.DataFrame({'path': all_paths, 'label': all_labels})
    
    # Validate images
    print('\nValidating image integrity...')
    valid_indices = []
    corrupted_count = 0
    
    for idx, row in tqdm(df.iterrows(), total=len(df), desc='Validating'):
        try:
            img = Image.open(row['path']).convert('RGB')
            img_array = np.array(img)
            if img_array.shape == (img_array.shape[0], img_array.shape[1], 3) and img_array.size > 0:
                valid_indices.append(idx)
            else:
                corrupted_count += 1
        except Exception as e:
            corrupted_count += 1
    
    df = df.loc[valid_indices].reset_index(drop=True)
    print(f'\n✓ Valid images: {len(df)} (removed {corrupted_count} corrupted)')
    print(f'Class distribution:')
    print(df['label'].value_counts().sort_index())
    
    return df

# ============================================================================
# DATA PREPROCESSING
# ============================================================================

def create_dataloaders(df, config):
    """Create train/val/test dataloaders"""
    print('\n' + '='*60)
    print('PREPARING DATASETS')
    print('='*60)
    
    # Split data
    train_df, temp_df = train_test_split(
        df, test_size=0.30, random_state=config['seed'], stratify=df['label']
    )
    val_df, test_df = train_test_split(
        temp_df, test_size=0.50, random_state=config['seed'], stratify=temp_df['label']
    )
    
    print(f'Train: {len(train_df)} | Val: {len(val_df)} | Test: {len(test_df)}')
    
    # Augmentation transforms
    train_tfms = T.Compose([
        T.Resize((config['img_size'], config['img_size'])),
        T.RandomAffine(degrees=15, translate=(0.1, 0.1), scale=(0.85, 1.15), shear=5),
        T.RandomRotation(degrees=20),
        T.RandomHorizontalFlip(p=0.5),
        T.RandomVerticalFlip(p=0.5),
        T.RandomPerspective(distortion_scale=0.2, p=0.3),
        T.ColorJitter(brightness=0.2, contrast=0.2, saturation=0.15, hue=0.05),
        T.ToTensor(),
        T.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])
    
    val_tfms = T.Compose([
        T.Resize((config['img_size'], config['img_size'])),
        T.ToTensor(),
        T.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])
    
    # Dataset class
    class BreakHisDataset(Dataset):
        def __init__(self, df, transforms=None):
            self.df = df.reset_index(drop=True)
            self.transforms = transforms
        
        def __len__(self):
            return len(self.df)
        
        def __getitem__(self, idx):
            row = self.df.iloc[idx]
            img = Image.open(row['path']).convert('RGB')
            label = int(row['label'])
            if self.transforms:
                img = self.transforms(img)
            return img, label
    
    # Datasets
    train_ds = BreakHisDataset(train_df, transforms=train_tfms)
    val_ds = BreakHisDataset(val_df, transforms=val_tfms)
    test_ds = BreakHisDataset(test_df, transforms=val_tfms)
    
    # Class-balanced sampling
    class_counts = train_df['label'].value_counts().sort_index().values
    class_weights = torch.Tensor(1.0 / class_counts)
    class_weights = class_weights / class_weights.sum() * 2
    sample_weights = [class_weights[label] for label in train_df['label']]
    sampler = torch.utils.data.WeightedRandomSampler(sample_weights, len(sample_weights), replacement=True)
    
    # DataLoaders
    train_loader = DataLoader(train_ds, batch_size=config['batch_size'], sampler=sampler, 
                             num_workers=2, pin_memory=True)
    val_loader = DataLoader(val_ds, batch_size=config['batch_size'], shuffle=False, 
                           num_workers=2, pin_memory=True)
    test_loader = DataLoader(test_ds, batch_size=config['batch_size'], shuffle=False, 
                            num_workers=2, pin_memory=True)
    
    print(f'✓ DataLoaders created (batch size: {config["batch_size"]})')
    
    return train_loader, val_loader, test_loader, class_weights, (train_df, val_df, test_df)

# ============================================================================
# MODEL SETUP
# ============================================================================

def build_model(config, class_weights):
    """Build model with offline-safe weight loading"""
    print('\n' + '='*60)
    print('BUILDING MODEL')
    print('='*60)
    
    device = torch.device(config['device'])
    
    # Load ResNet50 with graceful fallback
    print('Attempting to load ResNet50 with ImageNet weights...')
    try:
        from torchvision.models import ResNet50_Weights
        model = models.resnet50(weights=ResNet50_Weights.IMAGENET1K_V2)
        print('✓ Loaded with ImageNet V2 weights (transfer learning)')
        pretrained = True
    except Exception as e:
        print(f'⚠ Could not download weights: {type(e).__name__}')
        print('  Initializing with random weights (training from scratch)')
        model = models.resnet50(weights=None)
        pretrained = False
    
    # Progressive unfreezing
    for param in model.parameters():
        param.requires_grad = False
    
    for param in model.layer4.parameters():
        param.requires_grad = True
    
    for param in model.layer3[-1].parameters():
        param.requires_grad = True
    
    for module in model.modules():
        if isinstance(module, nn.BatchNorm2d):
            module.requires_grad = True
            module.momentum = 0.01
    
    # Enhanced classification head
    num_ftrs = model.fc.in_features
    model.fc = nn.Sequential(
        nn.Dropout(p=0.5),
        nn.Linear(num_ftrs, 512),
        nn.BatchNorm1d(512),
        nn.ReLU(inplace=True),
        nn.Dropout(p=0.3),
        nn.Linear(512, 2)
    )
    
    model = model.to(device)
    
    # Stats
    total_params = sum(p.numel() for p in model.parameters())
    trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
    print(f'Total parameters: {total_params:,}')
    print(f'Trainable parameters: {trainable_params:,}')
    print(f'Training mode: {"Transfer Learning" if pretrained else "Training from Scratch"}')
    
    # Loss, optimizer, scheduler
    criterion = nn.CrossEntropyLoss(weight=class_weights.to(device), 
                                    label_smoothing=config['label_smoothing'])
    optimizer = optim.Adam(filter(lambda p: p.requires_grad, model.parameters()),
                          lr=config['learning_rate'], weight_decay=config['weight_decay'])
    scheduler = optim.lr_scheduler.CosineAnnealingWarmRestarts(optimizer, T_0=5, T_mult=2, eta_min=1e-6)
    
    return model, criterion, optimizer, scheduler, device

# ============================================================================
# TRAINING FUNCTIONS
# ============================================================================

class EarlyStoppingAUC:
    def __init__(self, patience=8, min_delta=0.002):
        self.patience = patience
        self.min_delta = min_delta
        self.counter = 0
        self.best_auc = 0
        self.early_stop = False
        self.best_model_state = None
    
    def __call__(self, val_auc, model):
        if val_auc > self.best_auc + self.min_delta:
            self.best_auc = val_auc
            self.counter = 0
            self.best_model_state = {k: v.cpu().clone() for k, v in model.state_dict().items()}
        else:
            self.counter += 1
            if self.counter >= self.patience:
                self.early_stop = True
    
    def load_best_model(self, model):
        if self.best_model_state is not None:
            model.load_state_dict(self.best_model_state)

def train_one_epoch(model, loader, optimizer, criterion, scheduler, device):
    model.train()
    running_loss, correct, total = 0.0, 0, 0
    all_preds, all_labels = [], []
    
    for images, labels in tqdm(loader, desc='Training', leave=False):
        images, labels = images.to(device), labels.to(device)
        
        optimizer.zero_grad()
        outputs = model(images)
        loss = criterion(outputs, labels)
        
        torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
        loss.backward()
        optimizer.step()
        
        running_loss += loss.item() * images.size(0)
        _, preds = torch.max(outputs, 1)
        correct += (preds == labels).sum().item()
        total += labels.size(0)
        
        all_preds.extend(preds.cpu().numpy())
        all_labels.extend(labels.cpu().numpy())
    
    if scheduler is not None:
        scheduler.step()
    
    epoch_loss = running_loss / total
    epoch_acc = correct / total
    epoch_f1 = f1_score(all_labels, all_preds, zero_division=0)
    
    return epoch_loss, epoch_acc, epoch_f1

def evaluate(model, loader, criterion, device):
    model.eval()
    running_loss, correct, total = 0.0, 0, 0
    all_preds, all_probs = [], []
    all_labels = []
    
    with torch.no_grad():
        for images, labels in tqdm(loader, desc='Evaluating', leave=False):
            images, labels = images.to(device), labels.to(device)
            outputs = model(images)
            loss = criterion(outputs, labels)
            
            running_loss += loss.item() * images.size(0)
            probs = torch.softmax(outputs, dim=1)
            preds = torch.argmax(probs, dim=1)
            
            correct += (preds == labels).sum().item()
            total += labels.size(0)
            
            all_preds.extend(preds.cpu().numpy())
            all_probs.extend(probs[:, 1].cpu().numpy())
            all_labels.extend(labels.cpu().numpy())
    
    epoch_loss = running_loss / total
    epoch_acc = correct / total
    epoch_f1 = f1_score(all_labels, all_preds, zero_division=0)
    epoch_auc = auc(*roc_curve(all_labels, all_probs)[:2])
    
    return epoch_loss, epoch_acc, epoch_f1, epoch_auc

# ============================================================================
# MAIN TRAINING LOOP
# ============================================================================

def train(model, train_loader, val_loader, optimizer, criterion, scheduler, config, device):
    """Main training loop"""
    print('\n' + '='*60)
    print('TRAINING')
    print('='*60)
    
    early_stopping = EarlyStoppingAUC(patience=config['early_stopping_patience'],
                                     min_delta=config['early_stopping_min_delta'])
    
    history = {'train_loss': [], 'val_loss': [], 'train_acc': [], 'val_acc': [],
               'train_f1': [], 'val_f1': [], 'val_auc': []}
    
    for epoch in range(config['epochs']):
        train_loss, train_acc, train_f1 = train_one_epoch(model, train_loader, optimizer, 
                                                         criterion, scheduler, device)
        val_loss, val_acc, val_f1, val_auc = evaluate(model, val_loader, criterion, device)
        
        history['train_loss'].append(train_loss)
        history['val_loss'].append(val_loss)
        history['train_acc'].append(train_acc)
        history['val_acc'].append(val_acc)
        history['train_f1'].append(train_f1)
        history['val_f1'].append(val_f1)
        history['val_auc'].append(val_auc)
        
        print(f'Epoch {epoch+1:2d}/{config["epochs"]} | '
              f'Train Loss: {train_loss:.4f} | Val Loss: {val_loss:.4f} | '
              f'Val Acc: {val_acc:.4f} | Val AUC: {val_auc:.4f}')
        
        early_stopping(val_auc, model)
        if early_stopping.early_stop:
            print(f'✓ Early stopping at epoch {epoch+1}')
            early_stopping.load_best_model(model)
            break
    
    return model, history

# ============================================================================
# EVALUATION
# ============================================================================

def evaluate_on_test(model, test_loader, device):
    """Comprehensive test evaluation"""
    print('\n' + '='*60)
    print('TEST EVALUATION')
    print('='*60)
    
    model.eval()
    all_labels = []
    all_preds = []
    all_probs = []
    
    with torch.no_grad():
        for images, labels in tqdm(test_loader, desc='Testing'):
            images = images.to(device)
            outputs = model(images)
            probs = torch.softmax(outputs, dim=1)
            preds = torch.argmax(probs, dim=1)
            
            all_labels.extend(labels.numpy())
            all_preds.extend(preds.cpu().numpy())
            all_probs.extend(probs[:, 1].cpu().numpy())
    
    all_labels = np.array(all_labels)
    all_preds = np.array(all_preds)
    all_probs = np.array(all_probs)
    
    acc = accuracy_score(all_labels, all_preds)
    prec = precision_score(all_labels, all_preds, zero_division=0)
    rec = recall_score(all_labels, all_preds, zero_division=0)
    f1 = f1_score(all_labels, all_preds, zero_division=0)
    fpr, tpr, _ = roc_curve(all_labels, all_probs)
    roc_auc = auc(fpr, tpr)
    
    tn = ((all_preds == 0) & (all_labels == 0)).sum()
    fp = ((all_preds == 1) & (all_labels == 0)).sum()
    fn = ((all_preds == 0) & (all_labels == 1)).sum()
    tp = ((all_preds == 1) & (all_labels == 1)).sum()
    specificity = tn / (tn + fp) if (tn + fp) > 0 else 0
    
    print(f'\nAccuracy:   {acc:.4f}')
    print(f'Precision:  {prec:.4f}')
    print(f'Recall:     {rec:.4f}')
    print(f'Specificity: {specificity:.4f}')
    print(f'F1 Score:   {f1:.4f}')
    print(f'ROC-AUC:    {roc_auc:.4f}')
    
    return {'acc': acc, 'prec': prec, 'rec': rec, 'f1': f1, 'auc': roc_auc}

# ============================================================================
# SAVE MODEL
# ============================================================================

def save_model(model, config, metrics):
    """Save trained model with metadata"""
    os.makedirs('models', exist_ok=True)
    
    model_info = {
        'model_state': model.state_dict(),
        'config': config,
        'metrics': metrics,
        'class_names': {0: 'Benign', 1: 'Malignant'},
        'image_size': config['img_size'],
        'normalization': {'mean': [0.485, 0.456, 0.406], 'std': [0.229, 0.224, 0.225]}
    }
    
    torch.save(model_info, 'models/pathovision_resnet50_v2.pt')
    print('✓ Model saved to models/pathovision_resnet50_v2.pt')

# ============================================================================
# MAIN
# ============================================================================

if __name__ == '__main__':
    print('PathoVision BreakHis Training - Offline Compatible Version')
    print('='*60)
    
    # Setup
    set_seed(CONFIG['seed'])
    device = torch.device(CONFIG['device'])
    print(f'Device: {device}')
    
    # Load data
    df = load_dataset(CONFIG['data_root'], CONFIG)
    
    # Create dataloaders
    train_loader, val_loader, test_loader, class_weights, dfs = create_dataloaders(df, CONFIG)
    train_df, val_df, test_df = dfs
    
    # Build model
    model, criterion, optimizer, scheduler, device = build_model(CONFIG, class_weights)
    
    # Train
    model, history = train(model, train_loader, val_loader, optimizer, criterion, 
                          scheduler, CONFIG, device)
    
    # Evaluate
    metrics = evaluate_on_test(model, test_loader, device)
    
    # Save
    save_model(model, CONFIG, metrics)
    
    print('\n✓ Training pipeline complete!')
