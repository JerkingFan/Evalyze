const express = require('express');
const router = express.Router();
const webhookController = require('../controllers/WebhookController');
const { authenticateToken } = require('../config/jwt');

// Protected routes
router.get('/config', authenticateToken, webhookController.getWebhookConfig);
router.post('/test', authenticateToken, webhookController.testWebhook);
router.post('/test-all', authenticateToken, webhookController.testAllWebhooks);
router.post('/analyze-competencies', authenticateToken, webhookController.sendCompetencyAnalysisWebhook);
router.post('/assign-job-role', authenticateToken, webhookController.sendJobRoleAssignmentWebhook);
router.post('/generate-ai-profile', authenticateToken, webhookController.sendAIProfileGenerationWebhook);

module.exports = router;
