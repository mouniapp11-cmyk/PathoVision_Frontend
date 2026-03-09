const express = require('express');
const router = express.Router();
const { createCase, getCases, getCaseById, generateReport, signOffReport, getGeminiModels } = require('../controllers/caseController');
const { authenticateToken, authorizeRole } = require('../middleware/auth');
const upload = require('../middleware/upload');

router.post(
    '/',
    authenticateToken,
    authorizeRole(['PATHOLOGIST']),
    createCase
);

router.get('/', authenticateToken, getCases);
router.get('/gemini/models', authenticateToken, authorizeRole(['PATHOLOGIST']), getGeminiModels);
router.get('/:id', authenticateToken, getCaseById);

// Generate pathology report using Gemini AI
router.post(
    '/:id/generate-report',
    authenticateToken,
    authorizeRole(['PATHOLOGIST']),
    generateReport
);

// Sign off the pathology report
router.post(
    '/:id/sign-off',
    authenticateToken,
    authorizeRole(['PATHOLOGIST']),
    signOffReport
);

module.exports = router;
