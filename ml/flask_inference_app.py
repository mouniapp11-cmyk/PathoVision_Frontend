"""
Flask Inference Service for PathoVision Model
Serves predictions via REST API for Android app
"""

import os
import io
import torch
import numpy as np
from PIL import Image
import torchvision.transforms as T
import torchvision.models as models
from flask import Flask, request, jsonify
from flask_cors import CORS
import logging

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# Device setup
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
logger.info(f'Using device: {device}')

# Global model variable
model = None
config = None
model_metrics = {}  # Store model performance metrics

def load_model(model_path):
    """Load trained model and config."""
    global model, config, model_metrics
    
    logger.info(f'Loading model from {model_path}...')
    
    if not os.path.exists(model_path):
        raise FileNotFoundError(f'Model not found at {model_path}')
    
    # Load checkpoint (weights_only=False for compatibility)
    checkpoint = torch.load(model_path, map_location=device, weights_only=False)
    config = checkpoint['config']
    
    # Store metrics globally
    model_metrics = {
        'test_acc': checkpoint.get('test_acc', 0.0),
        'test_auc': checkpoint.get('test_auc', 0.0),
        'best_val_auc': checkpoint.get('best_val_auc', 0.0)
    }
    
    # Build model architecture
    model = models.resnet50(weights=None)
    
    # Freeze early layers
    for param in model.parameters():
        param.requires_grad = False
    
    # Unfreeze layer4 + layer3[-1]
    for param in model.layer4.parameters():
        param.requires_grad = True
    for param in model.layer3[-1].parameters():
        param.requires_grad = True
    
    # Trainable BatchNorm
    for module in model.modules():
        if isinstance(module, torch.nn.BatchNorm2d):
            module.requires_grad = True
            module.momentum = 0.01
    
    # Replace classifier
    num_ftrs = model.fc.in_features
    model.fc = torch.nn.Sequential(
        torch.nn.Dropout(p=config['dropout_fc1']),
        torch.nn.Linear(num_ftrs, 1024),
        torch.nn.ReLU(),
        torch.nn.BatchNorm1d(1024),
        torch.nn.Dropout(p=config['dropout_fc2']),
        torch.nn.Linear(1024, 512),
        torch.nn.ReLU(),
        torch.nn.BatchNorm1d(512),
        torch.nn.Dropout(p=config['dropout_fc3']),
        torch.nn.Linear(512, 2)
    )
    
    # Load weights
    model.load_state_dict(checkpoint['model_state_dict'])
    model = model.to(device)
    model.eval()
    
    logger.info('✓ Model loaded successfully')
    logger.info(f'  Test Accuracy: {checkpoint["test_acc"]:.4f}')
    logger.info(f'  Test AUC: {checkpoint["test_auc"]:.4f}')
    logger.info(f'  Best Val AUC: {checkpoint["best_val_auc"]:.4f}')

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint."""
    if model is None:
        return jsonify({'status': 'error', 'message': 'Model not loaded'}), 503
    return jsonify({'status': 'ok', 'device': str(device), 'model_loaded': True}), 200

@app.route('/predict', methods=['POST'])
def predict():
    """
    Predict cancer type from image.
    
    Expected input:
    - image: Binary image file (PNG/JPG)
    - return_grad_cam: Optional boolean (default False)
    
    Response:
    {
        'prediction': 'benign' or 'malignant',
        'confidence': float [0, 1],
        'benign_prob': float [0, 1],
        'malignant_prob': float [0, 1],
        'model_info': {
            'test_acc': float,
            'test_auc': float
        }
    }
    """
    try:
        # Check model is loaded
        if model is None:
            return jsonify({'error': 'Model not loaded'}), 503
        
        # Get image from request
        if 'image' not in request.files:
            return jsonify({'error': 'No image provided'}), 400
        
        image_file = request.files['image']
        if image_file.filename == '':
            return jsonify({'error': 'Empty filename'}), 400
        
        # Read and process image
        try:
            image = Image.open(image_file.stream).convert('RGB')
        except Exception as e:
            return jsonify({'error': f'Invalid image: {str(e)}'}), 400
        
        # Transform image
        transform = T.Compose([
            T.Resize((224, 224)),
            T.ToTensor(),
            T.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
        ])
        
        img_tensor = transform(image).unsqueeze(0).to(device)
        
        # Forward pass
        with torch.no_grad():
            outputs = model(img_tensor)
            probs = torch.softmax(outputs, dim=1)
            benign_prob, malignant_prob = probs[0].cpu().numpy()
        
        # Log detailed probabilities
        logger.info(f'Probabilities: Benign={benign_prob:.4f}, Malignant={malignant_prob:.4f}')
        
        # Determine prediction
        prediction_label = 'malignant' if malignant_prob > 0.5 else 'benign'
        prediction_int = 1 if malignant_prob > 0.5 else 0  # 0=Benign, 1=Malignant
        confidence = max(benign_prob, malignant_prob)
        class_name = 'Malignant' if prediction_int == 1 else 'Benign'
        
        logger.info(f'Final prediction: {class_name} (confidence: {confidence:.4f})')
        
        # Format response for Android app
        response = {
            'prediction': prediction_int,  # 0 or 1
            'confidence': float(confidence),
            'class_name': class_name,  # "Benign" or "Malignant"
            'benign_prob': float(benign_prob),
            'malignant_prob': float(malignant_prob),
            'model_info': {
                'test_acc': float(model_metrics.get('test_acc', 0.0)),
                'test_auc': float(model_metrics.get('test_auc', 0.0)),
                'device': str(device)
            }
        }
        
        logger.info(f'Final prediction: {class_name} (confidence: {confidence:.4f})')
        return jsonify(response), 200
    
    except Exception as e:
        logger.error(f'Prediction error: {str(e)}', exc_info=True)
        return jsonify({'error': str(e)}), 500

@app.route('/model-info', methods=['GET'])
def model_info():
    """Get model information."""
    if model is None:
        return jsonify({'error': 'Model not loaded'}), 503
    
    return jsonify({
        'status': 'loaded',
        'device': str(device),
        'model_architecture': 'ResNet50',
        'performance': {
            'test_accuracy': float(checkpoint['test_acc']),
            'test_auc': float(checkpoint['test_auc']),
            'best_val_auc': float(checkpoint['best_val_auc']),
            'train_val_gap': float(checkpoint['train_val_gap'])
        },
        'config': {
            'input_size': 224,
            'batch_size': checkpoint['config']['batch_size'],
            'learning_rate': checkpoint['config']['lr'],
            'dropout_fc1': checkpoint['config']['dropout_fc1'],
            'dropout_fc2': checkpoint['config']['dropout_fc2'],
            'dropout_fc3': checkpoint['config']['dropout_fc3']
        }
    }), 200

@app.route('/batch-predict', methods=['POST'])
def batch_predict():
    """
    Batch prediction endpoint for multiple images.
    
    Expected input: JSON array of images or multipart with multiple 'images'
    """
    try:
        if model is None:
            return jsonify({'error': 'Model not loaded'}), 503
        
        if 'images' not in request.files:
            return jsonify({'error': 'No images provided'}), 400
        
        image_files = request.files.getlist('images')
        if not image_files:
            return jsonify({'error': 'No images in request'}), 400
        
        transform = T.Compose([
            T.Resize((224, 224)),
            T.ToTensor(),
            T.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
        ])
        
        results = []
        
        with torch.no_grad():
            for image_file in image_files:
                try:
                    image = Image.open(image_file.stream).convert('RGB')
                    img_tensor = transform(image).unsqueeze(0).to(device)
                    
                    outputs = model(img_tensor)
                    probs = torch.softmax(outputs, dim=1)
                    benign_prob, malignant_prob = probs[0].cpu().numpy()
                    
                    prediction = 'malignant' if malignant_prob > 0.5 else 'benign'
                    confidence = max(benign_prob, malignant_prob)
                    
                    results.append({
                        'filename': image_file.filename,
                        'prediction': prediction,
                        'confidence': float(confidence),
                        'benign_prob': float(benign_prob),
                        'malignant_prob': float(malignant_prob)
                    })
                except Exception as e:
                    results.append({
                        'filename': image_file.filename,
                        'error': str(e)
                    })
        
        return jsonify({
            'total': len(results),
            'results': results
        }), 200
    
    except Exception as e:
        logger.error(f'Batch prediction error: {str(e)}', exc_info=True)
        return jsonify({'error': str(e)}), 500

@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': 'Endpoint not found'}), 404

@app.errorhandler(500)
def server_error(error):
    logger.error(f'Server error: {error}')
    return jsonify({'error': 'Internal server error'}), 500

if __name__ == '__main__':
    # Load model on startup
    model_path = os.path.join(os.path.dirname(__file__), 'pathovision_anti_overfitting_kaggle.pt')
    
    try:
        load_model(model_path)
        
        # Start Flask server
        logger.info('Starting Flask inference server...')
        logger.info('Endpoints:')
        logger.info('  POST /predict - Single image prediction')
        logger.info('  POST /batch-predict - Batch predictions')
        logger.info('  GET /model-info - Model information')
        logger.info('  GET /health - Health check')
        
        app.run(
            host='0.0.0.0',
            port=5000,
            debug=False,
            threaded=True
        )
    except Exception as e:
        logger.error(f'Failed to start server: {e}', exc_info=True)
