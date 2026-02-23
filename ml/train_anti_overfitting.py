#!/usr/bin/env python3
"""
PathoVision Anti-Overfitting Training Script
============================================
Addresses unrealistic 99%+ accuracy through:
- Patient-level data splits (prevent leakage)
- Aggressive dropout (0.7)
- Strong L2 regularization (5e-4)
- Enhanced augmentation
- Longer training with stricter early stopping
- Class balancing with focal loss
"""

import os
import random
import re
import numpy as np
import pandas as pd
from pathlib import Path
from tqdm import tqdm
from collections import Counter

import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, Subset, WeightedRandomSampler
from torchvision import datasets, models, transforms as T
from torchvision.models import ResNet50_Weights

from sklearn.metrics import (
    accuracy_score, precision_score, recall_score,
    f1_score, roc_auc_score, classification_report,
    confusion_matrix
)

# ============================================================
# ANTI-OVERFITTING CONFIGURATION
# ============================================================
CONFIG = {
    'seed': 42,
    'batch_size': 16,          # Smaller batch = more stochastic
    'epochs': 50,              # Longer training
    'lr': 1e-4,                # Slower learning rate
    'weight_decay': 5e-4,      # Strong L2 regularization (50x increase)
    'dropout_fc1': 0.7,        # Aggressive dropout
    'dropout_fc2': 0.6,
    'dropout_fc3': 0.5,
    'label_smoothing': 0.2,    # Softer labels
    'warmup_epochs': 5,
    'patience': 12,            # Stricter early stopping
    'min_delta': 0.001,
    'data_root': '/kaggle/input/breakhis',  # Modify for local use
    'save_dir': 'models',
}

# ============================================================
# REPRODUCIBILITY
# ============================================================
def set_seed(seed):
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)
    torch.backends.cudnn.deterministic = True
    torch.backends.cudnn.benchmark = False

set_seed(CONFIG['seed'])

# ============================================================
# PATIENT-LEVEL DATA SPLITTING (PREVENT LEAKAGE)
# ============================================================
def extract_patient_id(filepath):
    """Extract patient ID from BreakHis filename.
    Format: SOB_B_A-14-22549AB-400-001.png -> A-14-22549AB
    """
    filename = os.path.basename(filepath)
    match = re.search(r'SOB_[BM]_(.+?)-\d+', filename)
    return match.group(1) if match else filename

def split_by_patient(dataset, test_size=0.2, val_size=0.25, seed=42):
    """Split dataset by PATIENT (not by image) to prevent data leakage."""
    from sklearn.model_selection import train_test_split
    
    # Extract patient IDs and labels
    patient_ids = []
    for path, label in dataset.samples:
        patient_id = extract_patient_id(path)
        patient_ids.append(patient_id)
    
    # Group by unique patients
    unique_patients = list(set(patient_ids))
    patient_to_label = {}
    for pid, label in zip(patient_ids, dataset.targets):
        if pid not in patient_to_label:
            patient_to_label[pid] = label
    
    print(f'\n📊 Dataset Statistics:')
    print(f'  Total images: {len(dataset)}')
    print(f'  Unique patients: {len(unique_patients)}')
    print(f'  Images per patient (avg): {len(dataset) / len(unique_patients):.1f}')
    
    # Split by PATIENT
    train_val_patients, test_patients = train_test_split(
        unique_patients,
        test_size=test_size,
        stratify=[patient_to_label[p] for p in unique_patients],
        random_state=seed
    )
    
    train_patients, val_patients = train_test_split(
        train_val_patients,
        test_size=val_size,  # 25% of 80% = 20%
        stratify=[patient_to_label[p] for p in train_val_patients],
        random_state=seed
    )
    
    # Map images to splits
    train_indices = [i for i, pid in enumerate(patient_ids) if pid in train_patients]
    val_indices = [i for i, pid in enumerate(patient_ids) if pid in val_patients]
    test_indices = [i for i, pid in enumerate(patient_ids) if pid in test_patients]
    
    print(f'\n✓ Patient-Level Splits:')
    print(f'  Train: {len(train_patients)} patients, {len(train_indices)} images')
    print(f'  Val:   {len(val_patients)} patients, {len(val_indices)} images')
    print(f'  Test:  {len(test_patients)} patients, {len(test_indices)} images')
    
    return train_indices, val_indices, test_indices

# ============================================================
# AGGRESSIVE AUGMENTATION (PREVENT MEMORIZATION)
# ============================================================
train_transform = T.Compose([
    T.Resize((256, 256)),
    T.RandomCrop(224),
    T.RandomHorizontalFlip(p=0.5),
    T.RandomVerticalFlip(p=0.5),
    T.RandomRotation(degrees=30),  # Increased
    T.ColorJitter(brightness=0.2, contrast=0.2, saturation=0.15, hue=0.05),  # Increased
    T.RandomAffine(degrees=20, translate=(0.15, 0.15), scale=(0.85, 1.15)),  # Increased
    T.RandomApply([T.GaussianBlur(kernel_size=3, sigma=(0.1, 2.0))], p=0.3),  # NEW
    T.RandomErasing(p=0.2, scale=(0.02, 0.15)),  # NEW: Cutout-style
    T.ToTensor(),
    T.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

val_transform = T.Compose([
    T.Resize((224, 224)),
    T.ToTensor(),
    T.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

# ============================================================
# DATA LOADING WITH VALIDATION
# ============================================================
def load_data(data_root):
    """Load BreakHis dataset with patient-level splits."""
    data_path = Path(data_root) / 'BreaKHis_v1' / 'histopathologic_images' / 'breast'
    
    if not data_path.exists():
        raise FileNotFoundError(
            f'❌ Dataset not found at: {data_path}\n'
            f'Expected structure: {data_root}/BreaKHis_v1/histopathologic_images/breast/'
        )
    
    # Load full dataset (no transform yet)
    full_dataset = datasets.ImageFolder(data_path)
    
    print(f'\n📁 Dataset loaded from: {data_path}')
    print(f'  Classes: {full_dataset.classes}')
    print(f'  Class distribution: {Counter(full_dataset.targets)}')
    
    # Patient-level split
    train_idx, val_idx, test_idx = split_by_patient(full_dataset)
    
    # Create subsets with appropriate transforms
    train_ds = Subset(datasets.ImageFolder(data_path, transform=train_transform), train_idx)
    val_ds = Subset(datasets.ImageFolder(data_path, transform=val_transform), val_idx)
    test_ds = Subset(datasets.ImageFolder(data_path, transform=val_transform), test_idx)
    
    # Compute class weights for imbalanced data
    train_labels = [full_dataset.targets[i] for i in train_idx]
    class_counts = Counter(train_labels)
    class_weights = torch.FloatTensor([
        1.0 / class_counts[0],
        1.0 / class_counts[1]
    ])
    class_weights /= class_weights.sum()  # Normalize
    
    print(f'\n⚖️  Class weights: Benign={class_weights[0]:.3f}, Malignant={class_weights[1]:.3f}')
    
    # Weighted sampler for balanced batches
    sample_weights = [class_weights[label] for label in train_labels]
    sampler = WeightedRandomSampler(
        weights=sample_weights,
        num_samples=len(sample_weights),
        replacement=True
    )
    
    # Data loaders
    train_loader = DataLoader(
        train_ds,
        batch_size=CONFIG['batch_size'],
        sampler=sampler,
        num_workers=2,
        pin_memory=True
    )
    val_loader = DataLoader(
        val_ds,
        batch_size=CONFIG['batch_size'],
        shuffle=False,
        num_workers=2,
        pin_memory=True
    )
    test_loader = DataLoader(
        test_ds,
        batch_size=CONFIG['batch_size'],
        shuffle=False,
        num_workers=2,
        pin_memory=True
    )
    
    return train_loader, val_loader, test_loader, class_weights

# ============================================================
# MODEL WITH AGGRESSIVE REGULARIZATION
# ============================================================
def create_model(device, pretrained=True):
    """Create ResNet50 with aggressive regularization."""
    print('\n🏗️  Building model...')
    
    try:
        if pretrained:
            model = models.resnet50(weights=ResNet50_Weights.IMAGENET1K_V2)
            print('  ✓ Loaded ImageNet pretrained weights')
        else:
            raise Exception('Using fallback')
    except Exception:
        print('  ⚠ Network unavailable, initializing from scratch')
        model = models.resnet50(weights=None)
    
    # Freeze early layers (only train deeper features)
    for name, param in model.named_parameters():
        if 'layer4' not in name and 'fc' not in name:
            param.requires_grad = False
    
    # AGGRESSIVE regularization classifier
    num_ftrs = model.fc.in_features
    model.fc = nn.Sequential(
        nn.Dropout(p=CONFIG['dropout_fc1']),  # 0.7
        nn.Linear(num_ftrs, 1024),
        nn.ReLU(),
        nn.BatchNorm1d(1024),
        nn.Dropout(p=CONFIG['dropout_fc2']),  # 0.6
        nn.Linear(1024, 512),
        nn.ReLU(),
        nn.BatchNorm1d(512),
        nn.Dropout(p=CONFIG['dropout_fc3']),  # 0.5
        nn.Linear(512, 2)
    )
    
    model = model.to(device)
    
    total_params = sum(p.numel() for p in model.parameters())
    trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
    print(f'  Total parameters: {total_params:,}')
    print(f'  Trainable parameters: {trainable_params:,}')
    print(f'  Training ratio: {100 * trainable_params / total_params:.1f}%')
    
    return model

# ============================================================
# FOCAL LOSS (BETTER THAN CE FOR IMBALANCED DATA)
# ============================================================
class FocalLoss(nn.Module):
    """Focal Loss to handle class imbalance better than CrossEntropy."""
    def __init__(self, alpha=None, gamma=2.0, label_smoothing=0.0):
        super().__init__()
        self.alpha = alpha
        self.gamma = gamma
        self.label_smoothing = label_smoothing
        self.ce = nn.CrossEntropyLoss(reduction='none', label_smoothing=label_smoothing)
    
    def forward(self, inputs, targets):
        ce_loss = self.ce(inputs, targets)
        p_t = torch.exp(-ce_loss)
        focal_loss = (1 - p_t) ** self.gamma * ce_loss
        
        if self.alpha is not None:
            alpha_t = self.alpha[targets]
            focal_loss = alpha_t * focal_loss
        
        return focal_loss.mean()

# ============================================================
# EARLY STOPPING (STRICTER)
# ============================================================
class EarlyStoppingAUC:
    """Early stopping based on validation AUC with stricter criteria."""
    def __init__(self, patience=12, min_delta=0.001):
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

# ============================================================
# TRAINING & EVALUATION
# ============================================================
def train_one_epoch(model, loader, optimizer, criterion, device):
    model.train()
    running_loss, correct, total = 0.0, 0, 0
    all_preds, all_labels = [], []
    
    for images, labels in tqdm(loader, desc='Training', leave=False):
        images, labels = images.to(device), labels.to(device)
        
        optimizer.zero_grad()
        outputs = model(images)
        loss = criterion(outputs, labels)
        loss.backward()
        
        # Gradient clipping
        torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
        
        optimizer.step()
        
        running_loss += loss.item()
        _, preds = torch.max(outputs, 1)
        correct += (preds == labels).sum().item()
        total += labels.size(0)
        
        all_preds.extend(preds.cpu().numpy())
        all_labels.extend(labels.cpu().numpy())
    
    acc = accuracy_score(all_labels, all_preds)
    f1 = f1_score(all_labels, all_preds, average='weighted')
    auc = roc_auc_score(all_labels, all_preds)
    
    return running_loss / len(loader), acc, f1, auc

@torch.no_grad()
def evaluate(model, loader, criterion, device):
    model.eval()
    running_loss, all_preds, all_labels = 0.0, [], []
    all_probs = []
    
    for images, labels in loader:
        images, labels = images.to(device), labels.to(device)
        outputs = model(images)
        loss = criterion(outputs, labels)
        
        running_loss += loss.item()
        probs = torch.softmax(outputs, dim=1)
        _, preds = torch.max(outputs, 1)
        
        all_preds.extend(preds.cpu().numpy())
        all_labels.extend(labels.cpu().numpy())
        all_probs.extend(probs[:, 1].cpu().numpy())
    
    acc = accuracy_score(all_labels, all_preds)
    f1 = f1_score(all_labels, all_preds, average='weighted')
    auc = roc_auc_score(all_labels, all_probs)
    
    return running_loss / len(loader), acc, f1, auc

# ============================================================
# MAIN TRAINING LOOP
# ============================================================
def main():
    print('=' * 60)
    print('PathoVision Anti-Overfitting Training')
    print('=' * 60)
    
    # Device
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f'\n🖥️  Device: {device}')
    if torch.cuda.is_available():
        print(f'  GPU: {torch.cuda.get_device_name(0)}')
        print(f'  CUDA Version: {torch.version.cuda}')
    
    # Load data
    train_loader, val_loader, test_loader, class_weights = load_data(CONFIG['data_root'])
    
    # Create model
    model = create_model(device)
    
    # Focal Loss with label smoothing
    criterion = FocalLoss(
        alpha=class_weights.to(device),
        gamma=2.0,
        label_smoothing=CONFIG['label_smoothing']
    )
    
    # Optimizer with strong L2 regularization
    optimizer = optim.Adam(
        filter(lambda p: p.requires_grad, model.parameters()),
        lr=CONFIG['lr'],
        weight_decay=CONFIG['weight_decay'],
        betas=(0.9, 0.999)
    )
    
    # Cosine annealing scheduler
    scheduler = optim.lr_scheduler.CosineAnnealingWarmRestarts(
        optimizer,
        T_0=5,
        T_mult=2,
        eta_min=1e-6
    )
    
    # Early stopping
    early_stopping = EarlyStoppingAUC(
        patience=CONFIG['patience'],
        min_delta=CONFIG['min_delta']
    )
    
    # Training loop
    print(f'\n🚀 Starting training for {CONFIG["epochs"]} epochs...\n')
    
    best_val_auc = 0
    for epoch in range(1, CONFIG['epochs'] + 1):
        # Train
        train_loss, train_acc, train_f1, train_auc = train_one_epoch(
            model, train_loader, optimizer, criterion, device
        )
        
        # Validate
        val_loss, val_acc, val_f1, val_auc = evaluate(
            model, val_loader, criterion, device
        )
        
        # Update LR
        scheduler.step()
        current_lr = optimizer.param_groups[0]['lr']
        
        # Print progress
        print(f'Epoch {epoch:2d}/{CONFIG["epochs"]} | '
              f'Train Loss: {train_loss:.4f} | Train Acc: {train_acc:.4f} | Train AUC: {train_auc:.4f} | '
              f'Val Loss: {val_loss:.4f} | Val Acc: {val_acc:.4f} | Val AUC: {val_auc:.4f} | '
              f'LR: {current_lr:.2e}')
        
        # Track best
        if val_auc > best_val_auc:
            best_val_auc = val_auc
        
        # Early stopping check
        early_stopping(val_auc, model)
        if early_stopping.early_stop:
            print(f'\n✓ Early stopping triggered at epoch {epoch}')
            break
    
    # Load best model
    early_stopping.load_best_model(model)
    print(f'\n✓ Training complete! Best Val AUC: {early_stopping.best_auc:.4f}')
    
    # Final test evaluation
    print('\n' + '=' * 60)
    print('FINAL TEST SET EVALUATION')
    print('=' * 60)
    
    test_loss, test_acc, test_f1, test_auc = evaluate(
        model, test_loader, criterion, device
    )
    
    print(f'\n📊 Test Results:')
    print(f'  Accuracy:  {test_acc:.4f} ({test_acc * 100:.2f}%)')
    print(f'  F1 Score:  {test_f1:.4f}')
    print(f'  AUC-ROC:   {test_auc:.4f}')
    
    # Detailed classification report
    model.eval()
    all_preds, all_labels = [], []
    with torch.no_grad():
        for images, labels in test_loader:
            images = images.to(device)
            outputs = model(images)
            _, preds = torch.max(outputs, 1)
            all_preds.extend(preds.cpu().numpy())
            all_labels.extend(labels.cpu().numpy())
    
    print('\n' + classification_report(
        all_labels, all_preds,
        target_names=['Benign', 'Malignant'],
        digits=4
    ))
    
    # Confusion matrix
    cm = confusion_matrix(all_labels, all_preds)
    print('Confusion Matrix:')
    print(f'         Predicted')
    print(f'           B    M')
    print(f'Actual B {cm[0, 0]:4d} {cm[0, 1]:4d}')
    print(f'       M {cm[1, 0]:4d} {cm[1, 1]:4d}')
    
    # Calculate specificity/sensitivity
    tn, fp, fn, tp = cm.ravel()
    sensitivity = tp / (tp + fn)
    specificity = tn / (tn + fp)
    print(f'\nSensitivity (Recall): {sensitivity:.4f}')
    print(f'Specificity:          {specificity:.4f}')
    
    # Save model
    os.makedirs(CONFIG['save_dir'], exist_ok=True)
    save_path = os.path.join(CONFIG['save_dir'], 'pathovision_anti_overfitting_v1.pt')
    
    torch.save({
        'model_state_dict': model.state_dict(),
        'config': CONFIG,
        'test_acc': test_acc,
        'test_auc': test_auc,
        'test_f1': test_f1,
        'epoch': epoch,
        'best_val_auc': early_stopping.best_auc
    }, save_path)
    
    print(f'\n✅ Model saved to: {save_path}')
    print('=' * 60)

if __name__ == '__main__':
    main()
