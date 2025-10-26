const webhookService = require('../services/WebhookService');
const profileService = require('../services/ProfileService');
const { authenticateToken } = require('../config/jwt');

class WebhookController {
  // Test webhook connectivity
  async testWebhook(req, res) {
    try {
      const { webhookUrl } = req.body;

      if (!webhookUrl) {
        return res.status(400).json({
          error: 'Webhook URL is required'
        });
      }

      const result = await webhookService.testWebhook(webhookUrl);
      res.json(result);
    } catch (error) {
      console.error('Test webhook error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Test all webhooks
  async testAllWebhooks(req, res) {
    try {
      const results = await webhookService.testAllWebhooks();
      res.json({
        message: 'Webhook tests completed',
        results
      });
    } catch (error) {
      console.error('Test all webhooks error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Send competency analysis webhook (Button 1)
  async sendCompetencyAnalysisWebhook(req, res) {
    try {
      const userId = req.user.userId;
      const { userEmail, userName, profileData, companyName } = req.body;

      if (!userEmail || !userName) {
        return res.status(400).json({
          error: 'User email and name are required'
        });
      }

      const result = await webhookService.sendCompetencyAnalysisWebhook(
        userId,
        userEmail,
        userName,
        profileData || '{}',
        companyName || null
      );

      res.json({
        message: 'Competency analysis webhook sent successfully',
        result
      });
    } catch (error) {
      console.error('Send competency analysis webhook error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Send job role assignment webhook (Button 2)
  async sendJobRoleAssignmentWebhook(req, res) {
    try {
      const {
        userId,
        userEmail,
        userName,
        activationCode,
        jobRoleId,
        jobRoleTitle,
        jobRoleDescription,
        profileData,
        companyName
      } = req.body;

      if (!userId || !userEmail || !jobRoleId) {
        return res.status(400).json({
          error: 'User ID, email, and job role ID are required'
        });
      }

      const result = await webhookService.sendJobRoleAssignmentWebhook(
        userId,
        userEmail,
        userName,
        activationCode,
        jobRoleId,
        jobRoleTitle,
        jobRoleDescription,
        profileData || '{}',
        companyName || null
      );

      res.json({
        message: 'Job role assignment webhook sent successfully',
        result
      });
    } catch (error) {
      console.error('Send job role assignment webhook error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Send AI profile generation webhook (Button 3)
  async sendAIProfileGenerationWebhook(req, res) {
    try {
      const {
        userId,
        userEmail,
        userName,
        profileData,
        companyName,
        activationCode,
        telegramChatId,
        status
      } = req.body;

      if (!userId || !userEmail) {
        return res.status(400).json({
          error: 'User ID and email are required'
        });
      }

      const result = await webhookService.sendAIProfileGenerationWebhook(
        userId,
        userEmail,
        userName,
        profileData || '{}',
        companyName || null,
        activationCode,
        telegramChatId,
        status
      );

      res.json({
        message: 'AI profile generation webhook sent successfully',
        result
      });
    } catch (error) {
      console.error('Send AI profile generation webhook error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Get webhook configuration
  async getWebhookConfig(req, res) {
    try {
      const config = {
        analyzeCompetencies: webhookService.WEBHOOK_ANALYZE_COMPETENCIES,
        assignJobRole: webhookService.WEBHOOK_ASSIGN_JOB_ROLE,
        generateAIProfile: webhookService.WEBHOOK_GENERATE_AI_PROFILE
      };

      res.json({
        message: 'Webhook configuration',
        config
      });
    } catch (error) {
      console.error('Get webhook config error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }
}

module.exports = new WebhookController();
