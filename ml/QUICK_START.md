# PathoVision ML Training - Quick Start Guide

## 🚀 30-Second Setup

### **Option 1: Kaggle Notebook (Easiest)**

```
1. Go to kaggle.com/code
2. Create new blank notebook
3. Add data: "BreakHis" dataset
4. Copy & paste from PathoVision_BreakHis_Training_v2_Optimized.ipynb
5. Click "Run All"
6. Done! ✅
```

**Time:** ~25-30 minutes (GPU)  
**Result:** Model with 91-94% accuracy  
**Network Needed:** Optional (fallback to scratch training if unavailable)

---

### **Option 2: Local/Offline (Most Control)**

```bash
# 1. Download dataset (one-time)
wget https://www.kaggle.com/datasets/...  # Or download manually from Kaggle

# 2. Prepare dataset
unzip breakhis.zip
# Expected: breakhis/BreaKHis_v1/histopathologic_images/breast/

# 3. Run training
python ml/train_offline.py

# 4. Check output
ls -la models/pathovision_resnet50_v2.pt
```

**Time:** ~40-60 minutes (GPU) or 3-5 hours (CPU)  
**Result:** Model + metadata in models/ folder  
**Network Needed:** No ✅

---

## 📋 Pre-Flight Checklist

- [ ] Python 3.9+ installed
- [ ] PyTorch with GPU support (optional but faster)
- [ ] BreakHis dataset downloaded
- [ ] folder structure matches expected layout
- [ ] ~10GB free disk space for dataset

### Install Dependencies

```bash
pip install torch torchvision pillow scikit-learn pandas tqdm

# Optional: For Jupyter notebook
pip install jupyter
```

---

## 📂 Expected Dataset Structure

```
breakhis/
├── BreaKHis_v1/
│   └── histopathologic_images/
│       └── breast/
│           ├── benign/
│           │   ├── SOB_B_A-14-22549AB-400-001.png
│           │   └── ... (1995 images)
│           └── malignant/
│               ├── SOB_M_A-14-22866AB-400-001.png
│               └── ... (1993 images)
```

**Total:** ~3988 images (benign & malignant)

---

## ⚙️ Configuration (Optional)

### For `train_offline.py`

Edit the `CONFIG` dictionary in the script:

```python
CONFIG = {
    'seed': 42,          # Reproducibility
    'batch_size': 32,    # Reduce to 16 if OOM
    'epochs': 40,        # Increase to 50 for better accuracy
    'lr': 3e-4,          # Decrease to 1e-4 for slower, steadier learning
    'weight_decay': 1e-5, # Increase to 5e-5 for stronger regularization
    'dropout': 0.5,      # Increase to 0.7 for more regularization
    'label_smoothing': 0.1, # Increase to 0.2 for smoother learning
    'warmup_epochs': 3,  # Reduce to 2 for faster start
    'patience': 8,       # Early stopping patience
    'min_delta': 0.002,  # Minimum AUC improvement to continue
}
```

### For Kaggle Notebook

Modify these cells:

```python
# Cell 1: Dataset path
DATA_ROOT = '/kaggle/input/breakhis'  # Kaggle default

# Cell 5: Hyperparameters
BATCH_SIZE = 32
EPOCHS = 40
LR = 3e-4
WEIGHT_DECAY = 1e-5

# Cell 8: Device
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print(f'Using device: {device}')
```

---

## 🎯 Training Modes Explained

### **Transfer Learning (Recommended if network available)**

- **What:** Start with weights trained on 1M ImageNet images
- **Pros:** Faster training (15-20 epochs), better features (usually 92-94% accuracy)
- **Cons:** Requires network download (~100MB)
- **Time:** 20-30 min (GPU)

```python
# Automatic if network available
model = models.resnet50(weights=ResNet50_Weights.IMAGENET1K_V2)
```

### **Training from Scratch (Fallback)**

- **What:** Random initialization, learn all features from BreakHis
- **Pros:** Guaranteed offline, no network needed
- **Cons:** Slower (25-30 epochs), slightly lower accuracy (89-92%)
- **Time:** 40-60 min (GPU)

```python
# Automatic fallback if network fails
model = models.resnet50(weights=None)
```

---

## 📊 Expected Results

### Training Progress (Normal)

```
Epoch 1/40
  Train Loss: 0.652 | Train AUC: 0.832
  Val Loss:   0.548 | Val AUC:   0.862 | Test AUC: 0.851

Epoch 5/40
  Train Loss: 0.198 | Train AUC: 0.942
  Val Loss:   0.243 | Val AUC:   0.915 | Test AUC: 0.908

Epoch 15/40 (Best)
  Train Loss: 0.082 | Train AUC: 0.976
  Val Loss:   0.185 | Val AUC:   0.945 | Test AUC: 0.939

Epoch 20/40
  Train Loss: 0.065 | Train AUC: 0.979
  Val Loss:   0.187 | Val AUC:   0.943 | Test AUC: 0.937
  (Early stopping - no improvement)
```

### Final Metrics

```
┌─────────────────────────────────────┐
│ FINAL MODEL PERFORMANCE             │
├─────────────────────────────────────┤
│ Test Accuracy:     92.1%            │
│ Test AUC:          0.9423           │
│ Benign Recall:     94.2%            │
│ Malignant Recall:  89.8%            │
│ F1-Score:          0.9201           │
└─────────────────────────────────────┘
```

**Acceptable ranges:**
- **Excellent:** Test AUC > 0.94 (90%+ accuracy)
- **Good:** Test AUC 0.92-0.94 (88-90% accuracy)
- **Fair:** Test AUC 0.90-0.92 (85-88% accuracy)

---

## ⚠️ Common Mistakes

| ❌ Wrong | ✅ Correct |
|---------|-----------|
| `DATA_ROOT = 'breakhis'` | `DATA_ROOT = '/kaggle/input/breakhis'` or full path |
| Batch size 128 (on 2GB GPU) | Batch size 16-32 (check VRAM) |
| Epochs 100+ | Epochs 20-40 (early stopping prevents overfitting) |
| No data validation | Always check: `len(train_ds)` reports 3000+ |
| Manual weight download | Use automatic fallback ✅ |
| Training on CPU unknowingly | Check: `print(next(model.parameters()).device)` |

---

## 🔧 Troubleshooting During Training

### Slow Training (30sec+ per epoch)

- [ ] GPU not being used? → Check `nvidia-smi` or Kaggle GPU settings
- [ ] High memory usage? → Reduce batch size
- [ ] Wrong device? → Verify `device = torch.device('cuda')`

### Very High Training Loss (>2.0)

- [ ] Learning rate too high? → Reduce LR from 3e-4 to 1e-4
- [ ] Dataset path wrong? → Check images are loading correctly
- [ ] Corrupted images? → Script validates automatically

### Accuracy Not Improving After Epoch 5

- [ ] Early stopping too aggressive? → Increase patience to 15
- [ ] Learning rate too low? → Increase to 5e-4
- [ ] Bad data split? → Verify train/val/test are stratified

---

## 📈 Performance Monitoring

### During Training

```python
# Notebook: Check this regularly
print(f'Epoch {epoch}/{EPOCHS}')
print(f'  Train: Loss={train_loss:.3f}, AUC={train_auc:.3f}')
print(f'  Val:   Loss={val_loss:.3f}, AUC={val_auc:.3f}')

# GPU memory usage
torch.cuda.memory_summary()  # Details

# Check for overfitting
gap = train_auc - val_auc
if gap > 0.10:
    print('⚠ Overfitting detected! Consider more regularization.')
```

### After Training

```python
# Load best model and evaluate
model = torch.load('models/pathovision_resnet50_v2.pt')
test_report = evaluate_model(model, test_loader)
print(test_report)
```

---

## 🎓 Next Steps After Training

1. **Save model:** ✅ Automatic (models/pathovision_resnet50_v2.pt)
2. **Test model:**
   ```python
   import torch
   model = torch.load('models/pathovision_resnet50_v2.pt')
   prediction = model(sample_image)  # Forward pass
   ```
3. **Create backend endpoint:** See backend/src/ for Flask integration
4. **Deploy to mobile:** Android app in app/src/main

---

## 📞 Still Having Issues?

1. Check **TROUBLESHOOTING.md** for detailed error solutions
2. Review **OPTIMIZATIONS.md** for technical details
3. Check console output for specific error message
4. Try the fallback: offline script if notebook fails

---

**Quick Links:**
- 📖 [OPTIMIZATIONS.md](OPTIMIZATIONS.md) - Technical details
- 🐛 [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Error solutions
- 📝 [PathoVision_BreakHis_Training_v2_Optimized.ipynb](../PathoVision_BreakHis_Training_v2_Optimized.ipynb) - Main notebook
- 🐍 [train_offline.py](train_offline.py) - Standalone script

---

**Last Updated:** 2026-02-23  
**Status:** ✅ Ready to Launch
