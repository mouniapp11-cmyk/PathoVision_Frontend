# PathoVision ML Training - Troubleshooting Guide

## Common Issues & Solutions

---

## 🔴 Issue 1: `gaierror: [Errno -3] Temporary failure in name resolution`

**Error Pattern:**
```
URLError: <urlopen error [Errno -3] Temporary failure in name resolution>
  File "torch/hub.py", line 876, in download_url_to_file
```

**Cause:** Network connectivity issue when downloading ResNet50 ImageNet weights from PyTorch Hub.

**Solutions:**

### Option A: Use Fallback (Automatic - Recommended)

The updated `PathoVision_BreakHis_Training_v2_Optimized.ipynb` now includes **automatic fallback**:

```python
try:
    model = models.resnet50(weights=ResNet50_Weights.IMAGENET1K_V2)
    print('✓ Loaded ResNet50 with ImageNet V2 weights (transfer learning)')
except Exception as e:
    print(f'⚠ Could not download: {type(e).__name__}')
    print('  Initializing with random weights (training from scratch)')
    model = models.resnet50(weights=None)
```

**What happens:**
- ✅ **With internet**: Uses ImageNet-pretrained weights (faster training, ~15-20 epochs to convergence)
- ⚠ **Without internet**: Trains from scratch (slower ~25-30 epochs, but works)
- Expected accuracy: 88-93% (both methods), slightly lower with scratch training

---

### Option B: Pre-Download Weights (For Kaggle)

If you want guaranteed transfer learning on Kaggle:

1. **Create a standalone Kaggle dataset with weights:**
   - Run locally: `wget https://download.pytorch.org/models/resnet50-11ad33ff.pth`
   - Upload to Kaggle as new dataset `breakhis-pretrained-weights`
   - Reference in notebook:

```python
import torch
# Load from local dataset instead of downloading
weights_path = '/kaggle/input/breakhis-pretrained-weights/resnet50-11ad33ff.pth'
if os. path.exists(weights_path):
    pretrained_state_dict = torch.load(weights_path)
    model.load_state_dict(pretrained_state_dict)
    print('✓ Loaded pretrained weights from local dataset')
else:
    print('⚠ Using random initialization')
```

---

### Option C: Use Offline Script

Run locally with the new `train_offline.py` script:

```bash
# Install dependencies
pip install torch torchvision pandas pillow scikit-learn tqdm

# Run training
python ml/train_offline.py

# Output:
# - models/pathovision_resnet50_v2.pt (trained model)
# - Logs printed to console
```

**Advantages:**
- No Kaggle network restrictions
- Can pause/resume training
- Full control over parameters
- Offline-safe

---

## 🔴 Issue 2: `FileNotFoundError: Dataset not found`

**Cause:** BreakHis dataset not attached to Kaggle notebook.

**Solution:**

1. In Kaggle Notebook → Add Data
2. Search: "BreakHis" or "BreaKHis"
3. Select the official dataset
4. Attach it

Expected path: `/kaggle/input/breakhis`

**Alternative:** If path is different, modify in notebook:
```python
DATA_ROOT = '/kaggle/input/your-dataset-name'  # Update this
```

---

## 🔴 Issue 3: CUDA Out of Memory

**Error:**
```
RuntimeError: CUDA out of memory
```

**Solutions:**

1. **Reduce batch size** (in notebook cell #5):
```python
BATCH_SIZE = 16  # Was 32, now 16
```

2. **Clear cache:**
```python
import torch
torch.cuda.empty_cache()
```

3. **Use CPU instead** (slower but works):
```python
device = torch.device('cpu')  # Change from 'cuda'
```

---

## 🔴 Issue 4: Very Slow Training / GPU Not Used

**Cause:** Likely using CPU instead of GPU.

**Check GPU availability:**
```python
import torch
print(f'CUDA available: {torch.cuda.is_available()}')
print(f'GPU count: {torch.cuda.device_count()}')
print(f'Current device: {torch.cuda.current_device()}')
print(f'GPU name: {torch.cuda.get_device_name(0)}')
```

**If GPU not available:**
- For Kaggle: Enable GPU in notebook settings (gear icon → Accelerator → GPU)
- For local: Install CUDA-compatible PyTorch version

---

## 🔴 Issue 5: Early Stopping Too Aggressive

**Symptom:** Training stops at epoch 5-8 with validation accuracy still improving slightly.

**Solution:** Decrease patience in the training cell:

```python
early_stopping = Early StoppingAUC(
    patience=5,      # Was 8, now 5 (stricter)
    min_delta=0.005  # Was 0.002, now 0.005 (require bigger improvement)
)
```

Or increase patience to let training continue:
```python
early_stopping = EarlyStoppingAUC(
    patience=15,     # More patience
    min_delta=0.001  # Allow smaller improvements
)
```

---

## 🔴 Issue 6: Poor Accuracy / High Overfitting

**Symptom:** Val accuracy plateaus at 80-85%, training accuracy 95%+

**Solutions:**

1. **Increase regularization:**
```python
# In model.fc definition
nn.Dropout(p=0.7)  # Was 0.5, now 0.7 (more aggressive)
...
nn.Dropout(p=0.5)  # Was 0.3, now 0.5
```

2. **Increase label smoothing:**
```python
criterion = nn.CrossEntropyLoss(
    weight=class_weights,
    label_smoothing=0.2  # Was 0.1, now 0.2
)
```

3. **Increase weight decay:**
```python
optimizer = optim.Adam(
    params,
    weight_decay=5e-5  # Was 1e-5, now 5e-5
)
```

4. **Check for data leakage:** Ensure train/val/test splits are stratified and non-overlapping

---

## 🔴 Issue 7: Memory Leak / Notebook Crashes After N Epochs

**Cause:** Old tensors not being garbage collected.

**Solution:** Ensure gradients are detached and move tensors to CPU:

```python
# In training loop:
loss.backward()
optimizer.step()
loss.detach()  # Add this to avoid graph retention

# Move non-essential tensors to CPU
if epoch % 5 == 0:
    torch.cuda.empty_cache()
```

---

## 🟡 Optimization Tips

### For Maximum Speed:
```python
# Increase batch size (if GPU memory allows)
BATCH_SIZE = 64

# Use mixed precision
from torch.cuda.amp import autocast, GradScaler
scaler = GradScaler()

# In training loop:
with autocast():
    loss = criterion(outputs, labels)
scaler.scale(loss).backward()
scaler.step(optimizer)
scaler.update()
```

### For Maximum Accuracy:
```python
# Longer training
EPOCHS = 50

# More aggressive augmentation
T.RandomAffine(degrees=20, translate=(0.15, 0.15))  # More
T.RandomRotation(degrees=30)  # More

# More patience for early stopping
early_stopping = EarlyStoppingAUC(patience=15, min_delta=0.0005)

# Better learning rate schedule
scheduler = optim.lr_scheduler.PolynomialLR(optimizer, total_iters=EPOCHS, power=0.9)
```

### For Faster Dataset Loading:
```python
# Increase number of workers
train_loader = DataLoader(
    train_ds,
    batch_size=BATCH_SIZE,
    num_workers=4,  # Was 2, now 4 (if system allows)
    pin_memory=True,
    persistent_workers=True  # Reuse workers across epochs
)
```

---

## 🟢 Verification Checklist

- [ ] BreakHis dataset attached (Kaggle) or available locally
- [ ] GPU enabled in Kaggle settings (if using GPU)
- [ ] Correct ResNet50 weights version
- [ ] Stratified train/val/test splits (no data leakage)
- [ ] Class weights computed for imbalanced data
- [ ] Early stopping patience appropriate for dataset size
- [ ] Memory sufficient for batch size
- [ ] Path to save models exists (models/)
- [ ] TTA enabled for inference (optional but recommended)

---

## 📊 Expected Performance Ranges

| Mode | Accuracy | AUC | Training Time |
|------|----------|-----|---------------|
| Transfer Learning (ImageNet) | 91-94% | 0.95-0.97 | 20-30 min (GPU) |
| Training from Scratch | 88-92% | 0.93-0.96 | 40-60 min (GPU) |
| CPU Only | 88-92% | 0.93-0.96 | 3-5 hours |

---

## 🔗 Quick Reference

| File | Purpose | When to Use |
|------|---------|------------|
| `PathoVision_BreakHis_Training_v2_Optimized.ipynb` | Main notebook with auto-fallback | Kaggle (recommended) |
| `train_offline.py` | Standalone Python script | Local / Offline |
| `OPTIMIZATIONS.md` | Detailed optimization guide | Reference |

---

## 📞 Getting Help

1. **Check error message** - Match against sections above
2. **Review console output** - Look for warnings before error
3. **Verify configuration** - Ensure DATA_ROOT, device, batch_size are correct
4. **Try fallback** - Use offline script or auto-fallback mechanism
5. **Check GitHub issues** - See if others encountered same problem

---

**Last Updated:** 2026-02-23  
**Version:** 1.1  
**Status:** ✅ Production Ready
