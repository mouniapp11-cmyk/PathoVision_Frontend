const express = require('express');
const router = express.Router();
const { createCase, getCases, getCaseById } = require('../controllers/caseController');
const { authenticateToken, authorizeRole } = require('../middleware/auth');
const upload = require('../middleware/upload');

router.post(
    '/',
    authenticateToken,
    authorizeRole(['PATHOLOGIST']),
    createCase
);

router.get('/', authenticateToken, getCases);
router.get('/:id', authenticateToken, getCaseById);

module.exports = router;
