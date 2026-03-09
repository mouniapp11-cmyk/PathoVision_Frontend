# PathoVision ML Inference Setup Guide

## Overview
This guide explains how to set up and run the ML inference service alongside your app.

## Architecture

```
Android App
    ↓ (HTTP)
Node.js Backend (Express)
    ↓ (HTTP)
Flask ML Service (PyTorch)
    ↓
PathoVision ResNet50 Model
```

## Setup Instructions

### Step 1: Install Python Dependencies

```bash
cd ml
pip install -r requirements.txt
```

**Note:** PyTorch installation may take time. If you're on GPU:
```bash
pip install torch==2.0.1+cu118 torchvision==0.15.2+cu118 -f https://download.pytorch.org/whl/torch_stable.html
```

### Step 2: Start Flask ML Service

```bash
cd ml
python flask_inference_app.py
```

**Expected output:**
```
Using device: cuda (or cpu)
Loading model from pathovision_anti_overfitting_kaggle.pt...
✓ Model loaded successfully
  Test Accuracy: 0.8815
  Test AUC: 0.8873
  Best Val AUC: 0.8873
Starting Flask inference server...
Endpoints:
  POST /predict - Single image prediction
  POST /batch-predict - Batch predictions
  GET /model-info - Model information
  GET /health - Health check
```

The service runs on `http://localhost:5000`

### Step 3: Update Backend

Add the ML inference routes to your Express app (`backend/src/app.js`):

```javascript
const mlInferenceRouter = require('./routes/mlInference');

// ML Inference endpoints
app.use('/api/ml', mlInferenceRouter);
```

### Step 4: Update Backend Environment

Create/update `.env` file in backend:
```
ML_SERVICE_URL=http://localhost:5000
```

### Step 5: Update Android App

Add the API call in your Android app (example in Kotlin):

```kotlin
// In your ViewModel or Repository
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

suspend fun predictCancer(imageFile: File): PredictionResult {
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "image",
            imageFile.name,
            imageFile.asRequestBody("image/*".toMediaType())
        )
        .build()

    val request = Request.Builder()
        .url("http://<your-backend-url>/api/ml/predict")
        .post(requestBody)
        .build()

    return withContext(Dispatchers.IO) {
        httpClient.newCall(request).execute().use { response ->
            val json = response.body?.string() ?: ""
            JSONObject(json).let {
                PredictionResult(
                    prediction = it.getString("prediction"),
                    confidence = it.getDouble("confidence"),
                    benignProb = it.getDouble("benign_prob"),
                    malignantProb = it.getDouble("malignant_prob")
                )
            }
        }
    }
}
```

## API Endpoints

### 1. Single Image Prediction

**Request:**
```
POST /api/ml/predict
Content-Type: multipart/form-data

image: <binary image file>
```

**Response:**
```json
{
  "prediction": "benign",
  "confidence": 0.92,
  "benign_prob": 0.92,
  "malignant_prob": 0.08,
  "model_info": {
    "test_acc": 0.8815,
    "test_auc": 0.8873,
    "device": "cuda"
  }
}
```

### 2. Batch Predictions

**Request:**
```
POST /api/ml/batch-predict
Content-Type: multipart/form-data

images: <multiple image files>
```

**Response:**
```json
{
  "total": 3,
  "results": [
    {
      "filename": "image1.png",
      "prediction": "benign",
      "confidence": 0.92
    },
    {
      "filename": "image2.png",
      "prediction": "malignant",
      "confidence": 0.87
    }
  ]
}
```

### 3. Model Information

**Request:**
```
GET /api/ml/model-info
```

**Response:**
```json
{
  "status": "loaded",
  "device": "cuda",
  "model_architecture": "ResNet50",
  "performance": {
    "test_accuracy": 0.8815,
    "test_auc": 0.8873,
    "best_val_auc": 0.8873,
    "train_val_gap": 0.023
  }
}
```

### 4. Health Check

**Request:**
```
GET /api/ml/health
```

**Response:**
```json
{
  "status": "ok",
  "device": "cuda",
  "model_loaded": true
}
```

## Running the Full Stack

### Terminal 1: Start Flask ML Service
```bash
cd ml
python flask_inference_app.py
```

### Terminal 2: Start Node.js Backend
```bash
cd backend
npm start
```

### Terminal 3: Run Android App
```bash
# In Android Studio or
adb shell am start -n com.patholvision/.MainActivity
```

## Docker Deployment (Optional)

Create `Dockerfile` for ML service:
```dockerfile
FROM python:3.10-slim

WORKDIR /app

COPY ml/requirements.txt .
RUN pip install -r requirements.txt

COPY ml/ .

EXPOSE 5000

CMD ["python", "flask_inference_app.py"]
```

Build and run:
```bash
docker build -t pathovision-ml .
docker run -p 5000:5000 pathovision-ml
```

## Performance Notes

**On GPU (T4/RTX 2080):**
- Inference time: 200-400ms per image
- Batch prediction: 50-100ms per image (amortized)

**On CPU:**
- Inference time: 1-2 seconds per image
- Batch prediction: 500-800ms per image (amortized)

## Troubleshooting

### Model not loading
- Ensure `pathovision_anti_overfitting_kaggle.pt` exists in `ml/` directory
- Check file permissions
- Verify PyTorch installation

### Connection errors (Android to Backend)
- Check firewall settings
- Use correct backend IP (not localhost on real device)
- Verify network connectivity

### Flask service crashes
- Install all requirements: `pip install -r requirements.txt`
- Check GPU memory availability
- Enable debug mode in Flask for more details

### Slow predictions
- Monitor GPU usage: `nvidia-smi`
- Consider batch predictions for multiple images
- Increase Flask worker threads

## Next Steps

1. ✅ Test single image prediction via curl:
```bash
curl -X POST -F "image=@test_image.png" http://localhost:5000/predict
```

2. ✅ Integrate with Android app
3. ✅ Add logging/monitoring
4. ✅ Deploy to production (Heroku, AWS, GCP, etc.)

## Model Information

- **Architecture:** ResNet50 (fine-tuned)
- **Input Size:** 224×224 RGB
- **Classes:** Benign vs Malignant
- **Test Accuracy:** 88.15%
- **Test AUC:** 0.8873
- **Training Method:** Patient-level splits (no data leakage)

## Support

For issues or questions, refer to:
- [WHY_99_IS_WRONG.md](./WHY_99_IS_WRONG.md) - Technical analysis
- [README.md](./README.md) - Complete training guide
- [KAGGLE_INSTRUCTIONS.md](./KAGGLE_INSTRUCTIONS.md) - Kaggle notebook guide
