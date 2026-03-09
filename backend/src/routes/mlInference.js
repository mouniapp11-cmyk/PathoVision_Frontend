/**
 * ML Inference Service Integration
 * Routes requests to the Flask ML inference server
 */

const express = require('express');
const axios = require('axios');
const multer = require('multer');
const FormData = require('form-data');

const router = express.Router();
const upload = multer({ storage: multer.memoryStorage() });

// ML Service configuration
const ML_SERVICE_URL = process.env.ML_SERVICE_URL || 'http://localhost:5000';
const ML_SERVICE_TIMEOUT = 30000; // 30 seconds

/**
 * Health check for ML service
 */
router.get('/health', async (req, res) => {
    try {
        const response = await axios.get(`${ML_SERVICE_URL}/health`, {
            timeout: ML_SERVICE_TIMEOUT
        });
        res.json({
            status: 'connected',
            ml_service: response.data
        });
    } catch (error) {
        res.status(503).json({
            status: 'disconnected',
            error: error.message
        });
    }
});

/**
 * Get model information
 */
router.get('/model-info', async (req, res) => {
    try {
        const response = await axios.get(`${ML_SERVICE_URL}/model-info`, {
            timeout: ML_SERVICE_TIMEOUT
        });
        res.json(response.data);
    } catch (error) {
        res.status(500).json({
            error: 'Failed to get model info',
            details: error.message
        });
    }
});

/**
 * Single image prediction
 * POST /api/ml/predict
 * Body: multipart form-data with 'image' file
 */
router.post('/predict', upload.single('image'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ error: 'No image provided' });
        }

        // Create FormData for Flask
        const formData = new FormData();
        formData.append('image', req.file.buffer, {
            filename: req.file.originalname,
            contentType: req.file.mimetype
        });

        // Call Flask inference service
        const mlResponse = await axios.post(
            `${ML_SERVICE_URL}/predict`,
            formData,
            {
                headers: formData.getHeaders(),
                timeout: ML_SERVICE_TIMEOUT
            }
        );

        // Add timestamp and metadata
        const result = {
            ...mlResponse.data,
            timestamp: new Date().toISOString(),
            model_version: '1.0-anti-overfitting'
        };

        res.json(result);
    } catch (error) {
        console.error('Prediction error:', error.message);
        res.status(error.response?.status || 500).json({
            error: 'Prediction failed',
            details: error.message
        });
    }
});

/**
 * Batch predictions
 * POST /api/ml/batch-predict
 * Body: multipart form-data with multiple 'images' files
 */
router.post('/batch-predict', upload.array('images', 10), async (req, res) => {
    try {
        if (!req.files || req.files.length === 0) {
            return res.status(400).json({ error: 'No images provided' });
        }

        // Create FormData for Flask
        const formData = new FormData();
        req.files.forEach((file) => {
            formData.append('images', file.buffer, {
                filename: file.originalname,
                contentType: file.mimetype
            });
        });

        // Call Flask inference service
        const mlResponse = await axios.post(
            `${ML_SERVICE_URL}/batch-predict`,
            formData,
            {
                headers: formData.getHeaders(),
                timeout: ML_SERVICE_TIMEOUT * 2 // Longer timeout for batch
            }
        );

        res.json({
            ...mlResponse.data,
            timestamp: new Date().toISOString()
        });
    } catch (error) {
        console.error('Batch prediction error:', error.message);
        res.status(error.response?.status || 500).json({
            error: 'Batch prediction failed',
            details: error.message
        });
    }
});

module.exports = router;
