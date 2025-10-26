const axios = require('axios');

class WebhookService {
  constructor() {
    // Webhook URLs for three buttons
    this.WEBHOOK_ANALYZE_COMPETENCIES = process.env.WEBHOOK_ANALYZE_COMPETENCIES || 
      'https://guglovskij.app.n8n.cloud/webhook/0d0a654b-772e-447a-9223-8b443f788175';
    
    this.WEBHOOK_ASSIGN_JOB_ROLE = process.env.WEBHOOK_ASSIGN_JOB_ROLE || 
      'https://guglovskij.app.n8n.cloud/webhook/113447c6-c39e-410c-ab15-4f5ab7809fd9';
    
    this.WEBHOOK_GENERATE_AI_PROFILE = process.env.WEBHOOK_GENERATE_AI_PROFILE || 
      'https://guglovskij.app.n8n.cloud/webhook/bbd2959f-bedc-43fc-a558-69c0fe7b4db';
  }

  // Button 1: Analyze competencies
  async sendCompetencyAnalysisWebhook(userId, userEmail, userName, profileData, companyName) {
    try {
      const webhookPayload = {
        action: 'analyze_competencies',
        userId: userId,
        userEmail: userEmail,
        userName: userName,
        profileData: profileData,
        companyName: companyName,
        timestamp: new Date().toISOString()
      };

      console.log('=== WEBHOOK SEND START ===');
      console.log('URL:', this.WEBHOOK_ANALYZE_COMPETENCIES);
      console.log('Payload:', JSON.stringify(webhookPayload, null, 2));

      const response = await this.sendWebhook(this.WEBHOOK_ANALYZE_COMPETENCIES, webhookPayload);
      
      console.log('=== WEBHOOK RESPONSE ===');
      console.log(response);
      console.log('=== END ===');

      return response;
    } catch (error) {
      console.error('Error sending competency analysis webhook:', error.message);
      throw new Error(`Failed to send webhook: ${error.message}`);
    }
  }

  // Button 1: Analyze competencies with files
  async sendCompetencyAnalysisWebhookWithFiles(userId, userEmail, userName, profileData, companyName, files) {
    try {
      const filesData = files.map(file => ({
        fileName: file.originalname,
        fileSize: file.size,
        mimeType: file.mimetype,
        content: file.buffer.toString('base64')
      }));

      const webhookPayload = {
        action: 'analyze_competencies',
        userId: userId,
        userEmail: userEmail,
        userName: userName,
        profileData: profileData,
        companyName: companyName,
        files: filesData,
        timestamp: new Date().toISOString()
      };

      console.log('=== WEBHOOK JSON WITH BASE64 FILES ===');
      console.log('URL:', this.WEBHOOK_ANALYZE_COMPETENCIES);
      console.log('Files count:', files ? files.length : 0);
      console.log('User:', userEmail);
      console.log('Payload size:', JSON.stringify(webhookPayload).length, 'bytes');

      const response = await this.sendWebhook(this.WEBHOOK_ANALYZE_COMPETENCIES, webhookPayload);
      return response;
    } catch (error) {
      console.error('=== WEBHOOK ERROR ===');
      console.error('Error:', error.message);
      throw new Error(`Failed to send webhook: ${error.message}`);
    }
  }

  // Button 2: Assign job role
  async sendJobRoleAssignmentWebhook(userId, userEmail, userName, activationCode, jobRoleId, jobRoleTitle, jobRoleDescription, profileData, companyName) {
    try {
      const webhookPayload = {
        action: 'assign_job_role',
        userId: userId,
        userEmail: userEmail,
        userName: userName,
        activationCode: activationCode,
        jobRoleId: jobRoleId,
        jobRoleTitle: jobRoleTitle,
        jobRoleDescription: jobRoleDescription,
        profileData: profileData,
        companyName: companyName,
        timestamp: new Date().toISOString()
      };

      console.log('=== WEBHOOK SEND START ===');
      console.log('URL:', this.WEBHOOK_ASSIGN_JOB_ROLE);
      console.log('Payload:', JSON.stringify(webhookPayload, null, 2));

      const response = await this.sendWebhook(this.WEBHOOK_ASSIGN_JOB_ROLE, webhookPayload);
      
      console.log('=== WEBHOOK RESPONSE ===');
      console.log(response);
      console.log('=== END ===');

      return response;
    } catch (error) {
      console.error('Error sending job role assignment webhook:', error.message);
      throw new Error(`Failed to send webhook: ${error.message}`);
    }
  }

  // Button 3: Generate AI profile
  async sendAIProfileGenerationWebhook(userId, userEmail, userName, profileData, companyName, activationCode, telegramChatId, status) {
    try {
      const webhookPayload = {
        action: 'generate_ai_profile',
        userId: userId,
        userEmail: userEmail,
        userName: userName,
        profileData: profileData,
        companyName: companyName,
        activationCode: activationCode,
        telegramChatId: telegramChatId,
        status: status,
        timestamp: new Date().toISOString()
      };

      console.log('=== WEBHOOK SEND START ===');
      console.log('URL:', this.WEBHOOK_GENERATE_AI_PROFILE);
      console.log('Payload:', JSON.stringify(webhookPayload, null, 2));

      const response = await this.sendWebhook(this.WEBHOOK_GENERATE_AI_PROFILE, webhookPayload);
      
      console.log('=== WEBHOOK RESPONSE ===');
      console.log(response);
      console.log('=== END ===');

      return response;
    } catch (error) {
      console.error('Error sending AI profile generation webhook:', error.message);
      throw new Error(`Failed to send webhook: ${error.message}`);
    }
  }

  // Universal webhook sender
  async sendWebhook(webhookUrl, payload) {
    try {
      const response = await axios.post(webhookUrl, payload, {
        headers: {
          'Content-Type': 'application/json'
        },
        timeout: 30000 // 30 seconds timeout
      });

      return response.data;
    } catch (error) {
      console.error('=== WEBHOOK ERROR ===');
      console.error('Error:', error.message);
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
      }
      console.error('=== END ERROR ===');
      throw error;
    }
  }

  // Test webhook connectivity
  async testWebhook(webhookUrl) {
    try {
      const testPayload = {
        action: 'test',
        timestamp: new Date().toISOString()
      };

      const response = await this.sendWebhook(webhookUrl, testPayload);
      return {
        success: true,
        response,
        message: 'Webhook test successful'
      };
    } catch (error) {
      return {
        success: false,
        error: error.message,
        message: 'Webhook test failed'
      };
    }
  }

  // Test all webhooks
  async testAllWebhooks() {
    const results = {};

    // Test analyze competencies webhook
    results.analyzeCompetencies = await this.testWebhook(this.WEBHOOK_ANALYZE_COMPETENCIES);

    // Test assign job role webhook
    results.assignJobRole = await this.testWebhook(this.WEBHOOK_ASSIGN_JOB_ROLE);

    // Test generate AI profile webhook
    results.generateAIProfile = await this.testWebhook(this.WEBHOOK_GENERATE_AI_PROFILE);

    return results;
  }
}

module.exports = new WebhookService();
