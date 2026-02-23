const express = require('express');
const router = express.Router();
const { sendMessage, getMessages, getInbox } = require('../controllers/chatController');
const { authenticateToken } = require('../middleware/auth');

router.get('/inbox', authenticateToken, getInbox);
router.post('/', authenticateToken, sendMessage);
router.get('/:caseId', authenticateToken, getMessages);

module.exports = router;
