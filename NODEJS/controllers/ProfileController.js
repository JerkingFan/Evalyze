const profileService = require('../services/ProfileService');
const { authenticateToken, optionalAuth } = require('../config/jwt');

class ProfileController {
  // Create or update profile
  async createOrUpdateProfile(req, res) {
    try {
      const userId = req.user.userId;
      const { profileData } = req.body;

      const profile = await profileService.createOrUpdateProfile(userId, profileData);
      res.json({ profile });
    } catch (error) {
      console.error('Create/update profile error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Get profile by user ID
  async getProfileByUserId(req, res) {
    try {
      const { userId } = req.params;
      const profile = await profileService.getProfileByUserId(userId);

      if (!profile) {
        return res.status(404).json({
          error: 'Profile not found'
        });
      }

      res.json({ profile });
    } catch (error) {
      console.error('Get profile error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Get current user's profile
  async getCurrentUserProfile(req, res) {
    try {
      const userId = req.user.userId;
      const profile = await profileService.getProfileByUserId(userId);

      if (!profile) {
        return res.status(404).json({
          error: 'Profile not found'
        });
      }

      res.json({ profile });
    } catch (error) {
      console.error('Get current user profile error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Get company profiles
  async getCompanyProfiles(req, res) {
    try {
      const { companyId } = req.params;
      const profiles = await profileService.getCompanyProfiles(companyId);
      res.json({ profiles });
    } catch (error) {
      console.error('Get company profiles error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Get all profiles
  async getAllProfiles(req, res) {
    try {
      const profiles = await profileService.getAllProfiles();
      res.json({ profiles });
    } catch (error) {
      console.error('Get all profiles error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Update profile status
  async updateProfileStatus(req, res) {
    try {
      const { userId } = req.params;
      const { status } = req.body;

      if (!status) {
        return res.status(400).json({
          error: 'Status is required'
        });
      }

      const profile = await profileService.updateProfileStatus(userId, status);
      res.json({ profile });
    } catch (error) {
      console.error('Update profile status error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Find profile by email
  async findProfileByEmail(req, res) {
    try {
      const { email } = req.params;
      const profile = await profileService.findProfileByEmail(email);

      if (!profile) {
        return res.status(404).json({
          error: 'Profile not found'
        });
      }

      res.json({ profile });
    } catch (error) {
      console.error('Find profile by email error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Create employee profile
  async createEmployeeProfile(req, res) {
    try {
      const {
        employeeEmail,
        employeeName,
        currentPosition,
        currentSkills,
        currentResponsibilities,
        desiredPosition,
        desiredSkills,
        careerGoals
      } = req.body;

      if (!employeeEmail || !employeeName) {
        return res.status(400).json({
          error: 'Employee email and name are required'
        });
      }

      const result = await profileService.createEmployeeProfile({
        employeeEmail,
        employeeName,
        currentPosition,
        currentSkills,
        currentResponsibilities,
        desiredPosition,
        desiredSkills,
        careerGoals
      });

      res.status(201).json(result);
    } catch (error) {
      console.error('Create employee profile error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Generate AI profile (Button 3)
  async generateAIProfile(req, res) {
    try {
      const { profileId } = req.params;
      
      // В мок-режиме просто возвращаем успех
      const result = {
        message: 'AI profile generation initiated successfully',
        userId: profileId,
        userEmail: 'user@example.com',
        activationCode: 'mock-activation-code',
        status: 'webhook_sent',
        webhookResponse: 'Mock webhook response'
      };
      
      res.json(result);
    } catch (error) {
      console.error('Generate AI profile error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Assign job role to user (Button 2)
  async assignJobRoleToUser(req, res) {
    try {
      const { userId } = req.params;
      const { jobRoleId } = req.body;

      if (!jobRoleId) {
        return res.status(400).json({
          error: 'Job role ID is required'
        });
      }

      // В мок-режиме просто возвращаем успех
      const result = {
        message: 'Job role assigned successfully',
        userId: userId,
        userEmail: 'user@example.com',
        jobRoleTitle: 'Mock Job Role',
        jobRoleId: jobRoleId,
        status: 'success'
      };
      
      res.json(result);
    } catch (error) {
      console.error('Assign job role error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Assign job role flexible
  async assignJobRoleFlexible(req, res) {
    try {
      const { jobRoleId, activationCode, email } = req.body;

      if (!jobRoleId) {
        return res.status(400).json({
          error: 'Job role ID is required'
        });
      }

      if (!activationCode && !email) {
        return res.status(400).json({
          error: 'Either activation code or email is required'
        });
      }

      // В мок-режиме просто возвращаем успех
      const result = {
        message: 'Job role assigned successfully',
        userEmail: email || 'user@example.com',
        activationCode: activationCode || 'mock-activation-code',
        jobRoleTitle: 'Mock Job Role',
        jobRoleId: jobRoleId,
        status: 'success'
      };
      
      res.json(result);
    } catch (error) {
      console.error('Assign job role flexible error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Analyze competencies (Button 1)
  async analyzeCompetencies(req, res) {
    try {
      const { userEmail, activationCode } = req.body;

      if (!userEmail) {
        return res.status(400).json({
          error: 'User email is required'
        });
      }

      // В мок-режиме просто возвращаем успех
      const result = {
        message: 'Competencies analysis initiated successfully',
        userEmail: userEmail,
        activationCode: activationCode || 'mock-activation-code',
        status: 'analysis_started',
        webhookResponse: 'Mock webhook response'
      };
      
      res.json(result);
    } catch (error) {
      console.error('Analyze competencies error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }
}

module.exports = new ProfileController();
