const express = require('express');
const router = express.Router();
const authController = require('../controllers/AuthController');
const { authenticateToken } = require('../config/jwt');

// Public routes
router.post('/register', authController.register);
router.post('/login', authController.login);
router.post('/login/activation-code', authController.loginWithActivationCode);
router.get('/google/callback', authController.handleGoogleCallback);
router.post('/verify-activation-code', authController.verifyActivationCode);

// Protected routes
router.get('/me', authenticateToken, authController.getCurrentUser);
router.put('/me', authenticateToken, authController.updateUser);
router.get('/user/:email', authenticateToken, authController.getUserByEmail);

// Create employee (только для компаний)
router.post('/create-employee', authenticateToken, authController.createEmployee);

module.exports = router;
