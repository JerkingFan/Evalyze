const express = require('express');
const router = express.Router();
const profileController = require('../controllers/ProfileController');
const { authenticateToken, optionalAuth } = require('../config/jwt');

// Protected routes
router.post('/', authenticateToken, profileController.createOrUpdateProfile);
router.get('/me', authenticateToken, profileController.getCurrentUserProfile);
router.get('/user/:userId', authenticateToken, profileController.getProfileByUserId);
router.get('/company/:companyId', authenticateToken, profileController.getCompanyProfiles);
router.get('/all', authenticateToken, profileController.getAllProfiles);
router.put('/:userId/status', authenticateToken, profileController.updateProfileStatus);
router.post('/employee', authenticateToken, profileController.createEmployeeProfile);
router.post('/:profileId/generate-ai', authenticateToken, profileController.generateAIProfile);
router.post('/:userId/assign-job-role', authenticateToken, profileController.assignJobRoleToUser);
router.post('/assign-job-role-flexible', authenticateToken, profileController.assignJobRoleFlexible);

// Analyze competencies (Button 1) - без аутентификации для демо
router.post('/analyze-competencies', profileController.analyzeCompetencies);

// Public routes
router.get('/email/:email', profileController.findProfileByEmail);

module.exports = router;
