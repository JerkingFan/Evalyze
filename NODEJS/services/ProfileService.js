const userRepository = require('../repositories/UserRepository');
const profileRepository = require('../repositories/ProfileRepository');
const companyRepository = require('../repositories/CompanyRepository');
const jobRoleRepository = require('../repositories/JobRoleRepository');
const webhookService = require('./WebhookService');
const Profile = require('../models/Profile');
const Company = require('../models/Company');

class ProfileService {
  // Create or update profile
  async createOrUpdateProfile(userId, profileData) {
    try {
      const user = await userRepository.findById(userId);
      if (!user) {
        throw new Error('User not found');
      }

      let profile = await profileRepository.findByUserId(userId);
      
      if (!profile) {
        profile = new Profile({
          userId,
          profileData: {},
          status: 'PENDING'
        });
      }

      // Update profile data
      profile.updateProfileData(profileData);
      profile.lastUpdated = new Date().toISOString();

      const savedProfile = await profileRepository.save(profile);
      return savedProfile.toDto();
    } catch (error) {
      console.error('Error in createOrUpdateProfile:', error);
      throw error;
    }
  }

  // Get profile by user ID
  async getProfileByUserId(userId) {
    try {
      const profile = await profileRepository.findByUserId(userId);
      if (!profile) {
        return null;
      }

      return profile.toDto();
    } catch (error) {
      console.error('Error in getProfileByUserId:', error);
      throw error;
    }
  }

  // Get company profiles
  async getCompanyProfiles(companyId) {
    try {
      const profiles = await profileRepository.findByUserCompanyId(companyId);
      return profiles.map(profile => profile.toDto());
    } catch (error) {
      console.error('Error in getCompanyProfiles:', error);
      throw error;
    }
  }

  // Get all profiles
  async getAllProfiles() {
    try {
      const profiles = await profileRepository.findAll();
      return profiles.map(profile => profile.toDto());
    } catch (error) {
      console.error('Error in getAllProfiles:', error);
      throw error;
    }
  }

  // Update profile status
  async updateProfileStatus(userId, status) {
    try {
      const profile = await profileRepository.findByUserId(userId);
      if (!profile) {
        throw new Error('Profile not found');
      }

      profile.setStatus(status);
      const savedProfile = await profileRepository.save(profile);
      return savedProfile.toDto();
    } catch (error) {
      console.error('Error in updateProfileStatus:', error);
      throw error;
    }
  }

  // Find profile by email
  async findProfileByEmail(email) {
    try {
      const user = await userRepository.findByEmail(email);
      if (!user) {
        return null;
      }

      let profile = await profileRepository.findByUserId(user.id);
      
      if (!profile) {
        // Create empty profile
        profile = new Profile({
          userId: user.id,
          profileData: {},
          status: 'PENDING'
        });
        
        if (user.company) {
          profile.companyId = user.company.id;
        }
        
        profile = await profileRepository.save(profile);
      }

      return {
        userId: profile.userId,
        employeeEmail: user.email,
        employeeName: user.fullName,
        profileData: profile.profileData,
        status: profile.status,
        isVerified: profile.isCompleted(),
        companyId: user.company?.id || null,
        companyName: user.company?.name || null
      };
    } catch (error) {
      console.error('Error in findProfileByEmail:', error);
      throw error;
    }
  }

  // Create employee profile
  async createEmployeeProfile(request) {
    try {
      // Get or create default company
      let company = await companyRepository.findByName('Default Company');
      if (!company) {
        company = new Company({
          name: 'Default Company',
          description: 'Default company for new users'
        });
        company = await companyRepository.save(company);
      }

      // Check if user exists
      let user = await userRepository.findByEmail(request.employeeEmail);
      
      if (!user) {
        // Create new user
        user = new User({
          email: request.employeeEmail,
          fullName: request.employeeName,
          status: 'invited'
        });
        user.generateActivationCode();
        user = await userRepository.save(user);
      }

      // Create profile data
      const profileData = {
        currentPosition: request.currentPosition || '',
        currentSkills: request.currentSkills || '',
        currentResponsibilities: request.currentResponsibilities || '',
        desiredPosition: request.desiredPosition || '',
        desiredSkills: request.desiredSkills || '',
        careerGoals: request.careerGoals || ''
      };

      // Create or update profile
      let profile = await profileRepository.findByUserId(user.id);
      if (!profile) {
        profile = new Profile({
          userId: user.id,
          companyId: company.id,
          profileData,
          status: 'COMPLETED'
        });
      } else {
        profile.updateProfileData(profileData);
        profile.setStatus('COMPLETED');
      }

      const savedProfile = await profileRepository.save(profile);

      return {
        message: 'Profile created successfully',
        userId: savedProfile.userId,
        employeeEmail: user.email,
        employeeName: user.fullName,
        status: savedProfile.status
      };
    } catch (error) {
      console.error('Error in createEmployeeProfile:', error);
      throw error;
    }
  }

  // Generate AI profile (Button 3)
  async generateAIProfile(profileId) {
    try {
      const user = await userRepository.findById(profileId);
      if (!user) {
        throw new Error('User not found');
      }

      let profile = await profileRepository.findByUserId(user.id);
      
      if (!profile) {
        // Create empty profile
        profile = new Profile({
          userId: user.id,
          profileData: {},
          status: 'PENDING'
        });
        
        if (user.company) {
          profile.companyId = user.company.id;
        }
        
        profile = await profileRepository.save(profile);
      }

      // Send webhook to n8n
      const webhookResponse = await webhookService.sendAIProfileGenerationWebhook(
        profile.userId,
        user.email,
        user.fullName,
        JSON.stringify(profile.profileData),
        user.company?.name || null,
        user.activationCode,
        user.telegramChatId,
        user.status
      );

      return {
        message: 'AI profile generation initiated successfully',
        userId: profile.userId,
        userEmail: user.email,
        activationCode: user.activationCode,
        status: 'webhook_sent',
        webhookResponse
      };
    } catch (error) {
      console.error('Error in generateAIProfile:', error);
      throw error;
    }
  }

  // Assign job role to user (Button 2)
  async assignJobRoleToUser(userId, jobRoleId) {
    try {
      const user = await userRepository.findById(userId);
      if (!user) {
        throw new Error('User not found');
      }

      const jobRole = await jobRoleRepository.findById(jobRoleId);
      if (!jobRole) {
        throw new Error('Job role not found');
      }

      // Update user role
      await userRepository.updateUserRoleByEmail(user.email, jobRoleId);

      // Update profile
      let profile = await profileRepository.findByUserId(userId);
      if (!profile) {
        profile = new Profile({
          userId,
          profileData: {},
          status: 'PENDING'
        });
      }

      profile.setJobRoleData(jobRole);
      const savedProfile = await profileRepository.save(profile);

      // Send webhook
      try {
        const webhookResponse = await webhookService.sendJobRoleAssignmentWebhook(
          savedProfile.userId,
          user.email,
          user.fullName,
          user.activationCode,
          jobRoleId,
          jobRole.title,
          jobRole.description,
          JSON.stringify(savedProfile.profileData),
          user.company?.name || null
        );
        console.log('Webhook sent successfully:', webhookResponse);
      } catch (webhookError) {
        console.log('Error sending webhook (non-critical):', webhookError.message);
      }

      return {
        message: 'Job role assigned successfully',
        userId: user.id,
        userEmail: user.email,
        jobRoleTitle: jobRole.title,
        jobRoleId,
        status: 'success'
      };
    } catch (error) {
      console.error('Error in assignJobRoleToUser:', error);
      throw error;
    }
  }

  // Assign job role flexible (by activation code or email)
  async assignJobRoleFlexible(jobRoleId, activationCode, email) {
    try {
      let user = null;
      
      if (activationCode) {
        user = await userRepository.findByActivationCode(activationCode);
      }
      
      if (!user && email) {
        user = await userRepository.findByEmail(email);
      }
      
      if (!user) {
        throw new Error('User not found for provided identifiers');
      }

      const jobRole = await jobRoleRepository.findById(jobRoleId);
      if (!jobRole) {
        throw new Error('Job role not found');
      }

      // Update user role
      let roleUpdated = false;
      if (user.activationCode) {
        roleUpdated = await userRepository.updateUserRoleByActivationCode(user.activationCode, jobRoleId);
      }
      if (!roleUpdated) {
        roleUpdated = await userRepository.updateUserRoleByEmail(user.email, jobRoleId);
      }

      // Update profile
      let profile = await profileRepository.findByUserId(user.id);
      if (!profile) {
        profile = new Profile({
          userId: user.id,
          profileData: {},
          status: 'PENDING'
        });
      }

      profile.setJobRoleData(jobRole);
      const savedProfile = await profileRepository.save(profile);

      // Send webhook
      try {
        const webhookResponse = await webhookService.sendJobRoleAssignmentWebhook(
          savedProfile.userId,
          user.email,
          user.fullName,
          user.activationCode,
          jobRoleId,
          jobRole.title,
          jobRole.description,
          JSON.stringify(savedProfile.profileData),
          user.company?.name || null
        );
        console.log('Webhook (flex) sent successfully:', webhookResponse);
      } catch (webhookError) {
        console.log('Error sending webhook (flex, non-critical):', webhookError.message);
      }

      return {
        message: 'Job role assigned successfully',
        userId: user.id,
        userEmail: user.email,
        jobRoleTitle: jobRole.title,
        jobRoleId,
        status: roleUpdated ? 'success' : 'saved_profile_only'
      };
    } catch (error) {
      console.error('Error in assignJobRoleFlexible:', error);
      throw error;
    }
  }
}

module.exports = new ProfileService();
