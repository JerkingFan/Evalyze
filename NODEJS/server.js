const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const compression = require('compression');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

const authRoutes = require('./routes/auth');
const profileRoutes = require('./routes/profile');
const companyRoutes = require('./routes/company');
const fileRoutes = require('./routes/files');
const webhookRoutes = require('./routes/webhooks');
const healthRoutes = require('./routes/health');

const app = express();
const PORT = process.env.PORT || 8089;

// Security middleware - отключаем CSP для демо
app.use(helmet({
  contentSecurityPolicy: false
}));
app.use(compression());

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  message: 'Too many requests from this IP, please try again later.'
});
app.use('/api/', limiter);

// CORS configuration - разрешаем все для фронтенда
app.use(cors({
  origin: ['http://localhost:8089', 'http://localhost:3000', 'http://127.0.0.1:8089'],
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
}));

// Logging
app.use(morgan('combined'));

// Body parsing middleware
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Static files - используем фронтенд из Java проекта
const path = require('path');
app.use('/css', express.static(path.join(__dirname, '../src/main/resources/static/css')));
app.use('/js', express.static(path.join(__dirname, '../src/main/resources/static/js')));
app.use('/static', express.static(path.join(__dirname, '../src/main/resources/static')));

// HTML страницы
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, '../src/main/resources/templates/index.html'));
});

app.get('/profile', (req, res) => {
  res.sendFile(path.join(__dirname, '../src/main/resources/templates/profile.html'));
});

app.get('/company', (req, res) => {
  res.sendFile(path.join(__dirname, '../src/main/resources/templates/company.html'));
});

app.get('/create-employee', (req, res) => {
  res.sendFile(path.join(__dirname, 'public/create-employee.html'));
});

// API для создания сотрудника (публичный для демо)
app.post('/api/demo/create-employee', async (req, res) => {
  try {
    const { email, fullName, telegramChatId } = req.body;

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
      const { supabase } = require('./config/supabase');
      
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
          error: 'Failed to create employee: ' + error.message
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
});

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/profile', profileRoutes);
app.use('/api/company', companyRoutes);
app.use('/api/files', fileRoutes);
app.use('/api/webhooks', webhookRoutes);
app.use('/api/health', healthRoutes);

// API info endpoint
app.get('/api', (req, res) => {
  res.json({
    message: 'Evalyze Node.js Backend API',
    version: '1.0.0',
    status: 'running',
    timestamp: new Date().toISOString(),
    endpoints: {
      auth: '/api/auth',
      profile: '/api/profile',
      files: '/api/files',
      webhooks: '/api/webhooks',
      company: '/api/company',
      health: '/api/health'
    }
  });
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Not Found',
    message: `Route ${req.originalUrl} not found`,
    timestamp: new Date().toISOString()
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Error:', err);
  
  res.status(err.status || 500).json({
    error: err.message || 'Internal Server Error',
    timestamp: new Date().toISOString(),
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 Evalyze Node.js Backend running on port ${PORT}`);
  console.log(`📊 Environment: ${process.env.NODE_ENV || 'development'}`);
  console.log(`🔗 API Base URL: http://localhost:${PORT}/api`);
});

module.exports = app;
