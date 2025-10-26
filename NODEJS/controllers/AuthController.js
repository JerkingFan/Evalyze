const authService = require('../services/AuthService');
const { authenticateToken } = require('../config/jwt');

class AuthController {
  // Register new user (Company or Employee)
  async register(req, res) {
    try {
      const { email, fullName, password, role, companyName } = req.body;

      if (!email || !fullName) {
        return res.status(400).json({
          error: 'Email and full name are required'
        });
      }

      // Определяем роль
      const userRole = role || 'EMPLOYEE';
      
      if (userRole === 'COMPANY') {
        // Регистрация компании
        const mockUser = {
          id: 'mock-company-id',
          email,
          fullName,
          role: 'COMPANY',
          companyName: companyName || fullName,
          status: 'company',
          activationCode: 'mock-company-code'
        };

        const token = require('../config/jwt').generateToken({
          userId: mockUser.id,
          email: mockUser.email,
          role: 'COMPANY'
        });

        res.status(201).json({
          user: mockUser,
          token,
          message: 'Company registered successfully'
        });
      } else {
        // Регистрация сотрудника (обычно не используется, сотрудники создаются компаниями)
        const mockUser = {
          id: 'mock-employee-id',
          email,
          fullName,
          role: 'EMPLOYEE',
          status: 'invited',
          activationCode: 'mock-employee-code'
        };

        const token = require('../config/jwt').generateToken({
          userId: mockUser.id,
          email: mockUser.email,
          role: 'EMPLOYEE'
        });

        res.status(201).json({
          user: mockUser,
          token,
          message: 'Employee registered successfully'
        });
      }
    } catch (error) {
      console.error('Register error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Login with email and password
  async login(req, res) {
    try {
      const { email, password } = req.body;

      if (!email || !password) {
        return res.status(400).json({
          error: 'Email and password are required'
        });
      }

      // В мок-режиме принимаем любой email/password
      const mockUser = {
        id: 'mock-user-id',
        email,
        fullName: email.split('@')[0],
        status: 'active',
        activationCode: 'mock-activation-code'
      };

      const token = require('../config/jwt').generateToken({
        userId: mockUser.id,
        email: mockUser.email,
        role: 'EMPLOYEE'
      });

      res.json({
        user: mockUser,
        token,
        message: 'Login successful'
      });
    } catch (error) {
      console.error('Login error:', error);
      res.status(401).json({
        error: error.message
      });
    }
  }

  // Login with activation code (для сотрудников)
  async loginWithActivationCode(req, res) {
    try {
      const { activationCode } = req.body;

      if (!activationCode) {
        return res.status(400).json({
          error: 'Activation code is required'
        });
      }

      // Проверяем, используется ли реальная конфигурация
      const isRealConfig = process.env.SUPABASE_URL && 
                          process.env.SUPABASE_URL !== 'https://placeholder.supabase.co' &&
                          process.env.SUPABASE_ANON_KEY &&
                          process.env.SUPABASE_ANON_KEY !== 'placeholder-anon-key';

      console.log('🔧 Config check:', {
        SUPABASE_URL: process.env.SUPABASE_URL,
        SUPABASE_ANON_KEY: process.env.SUPABASE_ANON_KEY ? 'SET' : 'NOT SET',
        isRealConfig: isRealConfig
      });

      if (isRealConfig) {
        // Реальная авторизация через Supabase
        const { supabase } = require('../config/supabase');
        
        console.log('🔍 Searching for activation code:', activationCode);
        
        // Ищем пользователя по activation_code
        const { data: users, error } = await supabase
          .from('users')
          .select('*')
          .eq('activation_code', activationCode)
          .single();

        console.log('📊 Supabase response:', { users, error });

        if (error) {
          console.error('❌ Supabase error:', error);
          return res.status(401).json({
            error: 'Invalid activation code: ' + error.message
          });
        }

        if (!users) {
          console.log('❌ No user found with activation code:', activationCode);
          return res.status(401).json({
            error: 'Invalid activation code - user not found'
          });
        }

        // Обновляем статус на "active" при первом входе
        if (users.status === 'invited') {
          const { error: updateError } = await supabase
            .from('users')
            .update({ status: 'active' })
            .eq('activation_code', activationCode);

          if (updateError) {
            console.error('Update status error:', updateError);
          } else {
            users.status = 'active';
          }
        }

        // Генерируем JWT токен
        const token = require('../config/jwt').generateToken({
          userId: users.id,
          email: users.email,
          role: 'EMPLOYEE'
        });

        res.json({
          user: {
            id: users.id,
            email: users.email,
            fullName: users.full_name,
            role: 'EMPLOYEE',
            status: users.status,
            activationCode: users.activation_code,
            companyName: 'Company' // Можно добавить связь с компанией
          },
          token,
          message: 'Employee login successful'
        });
      } else {
        // Мок-режим для тестирования
        console.warn('⚠️  Using mock activation code login');
        console.log('🔍 Mock searching for activation code:', activationCode);
        
        // В мок-режиме принимаем любой код, но логируем его
        const mockUser = {
          id: 'mock-employee-' + Date.now(),
          email: 'employee@example.com',
          fullName: 'Test Employee',
          role: 'EMPLOYEE',
          status: 'active',
          activationCode,
          companyName: 'Test Company'
        };

        const token = require('../config/jwt').generateToken({
          userId: mockUser.id,
          email: mockUser.email,
          role: 'EMPLOYEE'
        });

        console.log('✅ Mock login successful for code:', activationCode);

        res.json({
          user: mockUser,
          token,
          message: 'Employee login successful (mock mode)'
        });
      }
    } catch (error) {
      console.error('Activation code login error:', error);
      res.status(401).json({
        error: error.message
      });
    }
  }

  // Create employee (для компаний)
  async createEmployee(req, res) {
    try {
      const { email, fullName, telegramChatId } = req.body;
      const companyEmail = req.user.email; // Из JWT токена

      if (!email || !fullName) {
        return res.status(400).json({
          error: 'Employee email and full name are required'
        });
      }

      // Проверяем, используется ли реальная конфигурация
      const isRealConfig = process.env.SUPABASE_URL && 
                          process.env.SUPABASE_URL !== 'https://placeholder.supabase.co' &&
                          process.env.SUPABASE_ANON_KEY &&
                          process.env.SUPABASE_ANON_KEY !== 'placeholder-anon-key';

      if (isRealConfig) {
        // Реальное создание сотрудника в Supabase
        const { supabase } = require('../config/supabase');
        
        // Проверяем, что email не занят
        const { data: existingUser } = await supabase
          .from('users')
          .select('id')
          .eq('email', email)
          .single();

        if (existingUser) {
          return res.status(400).json({
            error: 'Email already exists'
          });
        }

        // Генерируем activation_code
        const activationCode = 'EMP-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
        
        // Создаем сотрудника в БД
        const { data: newEmployee, error } = await supabase
          .from('users')
          .insert([{
            email: email,
            full_name: fullName,
            activation_code: activationCode,
            status: 'invited',
            telegram_chat_id: telegramChatId || null,
            created_at: new Date().toISOString()
          }])
          .select()
          .single();

        if (error) {
          console.error('Supabase insert error:', error);
          return res.status(500).json({
            error: 'Failed to create employee'
          });
        }

        res.status(201).json({
          employee: {
            id: newEmployee.id,
            email: newEmployee.email,
            fullName: newEmployee.full_name,
            role: 'EMPLOYEE',
            status: newEmployee.status,
            activationCode: newEmployee.activation_code,
            companyEmail: companyEmail,
            telegramChatId: newEmployee.telegram_chat_id
          },
          message: `Employee created successfully. Activation code: ${activationCode}`,
          activationCode: activationCode
        });
      } else {
        // Мок-режим для тестирования
        console.warn('⚠️  Using mock employee creation');
        
        const activationCode = 'EMP-' + Date.now(); // Генерируем код
        
        const mockEmployee = {
          id: 'mock-employee-' + Date.now(),
          email,
          fullName,
          role: 'EMPLOYEE',
          status: 'invited',
          activationCode,
          companyEmail,
          telegramChatId: telegramChatId || null
        };

        res.status(201).json({
          employee: mockEmployee,
          message: `Employee created successfully. Activation code: ${activationCode}`,
          activationCode: activationCode
        });
      }
    } catch (error) {
      console.error('Create employee error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Google OAuth callback
  async handleGoogleCallback(req, res) {
    try {
      const { token, email, name, picture } = req.query;

      if (!email) {
        return res.status(400).json({
          error: 'Email is required'
        });
      }

      const result = await authService.handleGoogleCallback({
        email,
        name,
        picture,
        token
      });

      res.json(result);
    } catch (error) {
      console.error('Google callback error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Get current user
  async getCurrentUser(req, res) {
    try {
      const userId = req.user.userId;
      const user = await authService.getUserById(userId);
      res.json({ user });
    } catch (error) {
      console.error('Get current user error:', error);
      res.status(404).json({
        error: error.message
      });
    }
  }

  // Update user
  async updateUser(req, res) {
    try {
      const userId = req.user.userId;
      const updateData = req.body;

      const user = await authService.updateUser(userId, updateData);
      res.json({ user });
    } catch (error) {
      console.error('Update user error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Verify activation code
  async verifyActivationCode(req, res) {
    try {
      const { activationCode } = req.body;

      if (!activationCode) {
        return res.status(400).json({
          error: 'Activation code is required'
        });
      }

      const user = await authService.verifyActivationCode(activationCode);
      res.json({ user });
    } catch (error) {
      console.error('Verify activation code error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Get user by email
  async getUserByEmail(req, res) {
    try {
      const { email } = req.params;

      if (!email) {
        return res.status(400).json({
          error: 'Email is required'
        });
      }

      const user = await authService.getUserByEmail(email);
      res.json({ user });
    } catch (error) {
      console.error('Get user by email error:', error);
      res.status(404).json({
        error: error.message
      });
    }
  }
}

module.exports = new AuthController();
