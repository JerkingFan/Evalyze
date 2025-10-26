const bcrypt = require('bcryptjs');
const { generateToken } = require('../config/jwt');
const userRepository = require('../repositories/UserRepository');
const profileRepository = require('../repositories/ProfileRepository');
const companyRepository = require('../repositories/CompanyRepository');
const User = require('../models/User');
const Profile = require('../models/Profile');

class AuthService {
  // Register new user
  async register(userData) {
    try {
      // Check if user already exists
      const existingUser = await userRepository.findByEmail(userData.email);
      if (existingUser) {
        throw new Error('User with this email already exists');
      }

      // Create new user
      const user = new User({
        email: userData.email,
        fullName: userData.fullName,
        password: userData.password ? await bcrypt.hash(userData.password, 10) : '',
        status: userData.status || 'invited'
      });

      // Generate activation code
      user.generateActivationCode();

      // Save user
      const savedUser = await userRepository.save(user);

      // Create empty profile
      const profile = new Profile({
        userId: savedUser.id,
        profileData: {},
        status: 'PENDING'
      });

      await profileRepository.save(profile);

      // Generate JWT token
      const token = generateToken({
        userId: savedUser.id,
        email: savedUser.email,
        role: savedUser.getUserRole()
      });

      return {
        user: savedUser.toSafeObject(),
        token,
        message: 'User registered successfully'
      };
    } catch (error) {
      console.error('Error in register:', error);
      throw error;
    }
  }

  // Login with email and password
  async login(email, password) {
    try {
      const user = await userRepository.findByEmail(email);
      if (!user) {
        throw new Error('Invalid credentials');
      }

      // Check password if user has one
      if (user.password && !await bcrypt.compare(password, user.password)) {
        throw new Error('Invalid credentials');
      }

      // Generate JWT token
      const token = generateToken({
        userId: user.id,
        email: user.email,
        role: user.getUserRole()
      });

      return {
        user: user.toSafeObject(),
        token,
        message: 'Login successful'
      };
    } catch (error) {
      console.error('Error in login:', error);
      throw error;
    }
  }

  // Login with activation code
  async loginWithActivationCode(activationCode) {
    try {
      const user = await userRepository.findByActivationCode(activationCode);
      if (!user) {
        throw new Error('Invalid activation code');
      }

      // Generate JWT token
      const token = generateToken({
        userId: user.id,
        email: user.email,
        role: user.getUserRole()
      });

      return {
        user: user.toSafeObject(),
        token,
        message: 'Login successful'
      };
    } catch (error) {
      console.error('Error in loginWithActivationCode:', error);
      throw error;
    }
  }

  // Google OAuth callback
  async handleGoogleCallback(tokenData) {
    try {
      const { email, name, picture } = tokenData;

      // Check if user exists
      let user = await userRepository.findByEmail(email);
      
      if (!user) {
        // Create new user
        user = new User({
          email,
          fullName: name,
          status: 'active'
        });
        user.generateActivationCode();
        user = await userRepository.save(user);

        // Create empty profile
        const profile = new Profile({
          userId: user.id,
          profileData: {},
          status: 'PENDING'
        });
        await profileRepository.save(profile);
      } else {
        // Update existing user
        user.fullName = name;
        user.status = 'active';
        user = await userRepository.save(user);
      }

      // Generate JWT token
      const token = generateToken({
        userId: user.id,
        email: user.email,
        role: user.getUserRole()
      });

      return {
        user: user.toSafeObject(),
        token,
        message: 'Google authentication successful'
      };
    } catch (error) {
      console.error('Error in handleGoogleCallback:', error);
      throw error;
    }
  }

  // Get user by ID
  async getUserById(userId) {
    try {
      const user = await userRepository.findById(userId);
      if (!user) {
        throw new Error('User not found');
      }

      return user.toSafeObject();
    } catch (error) {
      console.error('Error in getUserById:', error);
      throw error;
    }
  }

  // Get user by email
  async getUserByEmail(email) {
    try {
      const user = await userRepository.findByEmail(email);
      if (!user) {
        throw new Error('User not found');
      }

      return user.toSafeObject();
    } catch (error) {
      console.error('Error in getUserByEmail:', error);
      throw error;
    }
  }

  // Update user
  async updateUser(userId, updateData) {
    try {
      const user = await userRepository.findById(userId);
      if (!user) {
        throw new Error('User not found');
      }

      // Update user data
      Object.assign(user, updateData);
      user.lastUpdated = new Date().toISOString();

      const updatedUser = await userRepository.save(user);
      return updatedUser.toSafeObject();
    } catch (error) {
      console.error('Error in updateUser:', error);
      throw error;
    }
  }

  // Verify user activation code
  async verifyActivationCode(activationCode) {
    try {
      const user = await userRepository.findByActivationCode(activationCode);
      if (!user) {
        throw new Error('Invalid activation code');
      }

      return user.toSafeObject();
    } catch (error) {
      console.error('Error in verifyActivationCode:', error);
      throw error;
    }
  }
}

module.exports = new AuthService();
