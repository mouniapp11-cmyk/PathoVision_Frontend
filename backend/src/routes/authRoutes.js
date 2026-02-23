const express = require('express');
const router = express.Router();
const { register, login, getPatients, getDoctors, getProfile, updateProfile, changePassword } = require('../controllers/authController');
const { authenticateToken } = require('../middleware/auth');
const upload = require('../middleware/upload');

router.post('/register', register);
router.post('/login', login);
router.get('/patients', getPatients);
router.get('/doctors', authenticateToken, getDoctors);
router.get('/debug-test', (req, res) => res.json({ message: 'Debug route works' }));
router.get('/profile', authenticateToken, getProfile);
router.put('/profile', authenticateToken, upload.single('profile_picture'), updateProfile);
router.put('/change-password', authenticateToken, changePassword);

module.exports = router;
