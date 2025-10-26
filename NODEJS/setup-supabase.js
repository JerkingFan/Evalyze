#!/usr/bin/env node

const readline = require('readline');
const fs = require('fs');
const path = require('path');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

console.log('🚀 Настройка Supabase для Evalyze\n');

const questions = [
  {
    key: 'SUPABASE_URL',
    question: 'Введите Supabase Project URL (например: https://abcdefgh.supabase.co): ',
    validate: (value) => value.startsWith('https://') && value.includes('.supabase.co')
  },
  {
    key: 'SUPABASE_ANON_KEY',
    question: 'Введите Supabase anon key: ',
    validate: (value) => value.length > 50
  },
  {
    key: 'SUPABASE_SERVICE_ROLE_KEY',
    question: 'Введите Supabase service role key: ',
    validate: (value) => value.length > 50
  },
  {
    key: 'JWT_SECRET',
    question: 'Введите JWT secret (или нажмите Enter для автогенерации): ',
    validate: () => true,
    default: () => require('crypto').randomBytes(64).toString('hex')
  }
];

const answers = {};

function askQuestion(index) {
  if (index >= questions.length) {
    createEnvFile();
    return;
  }

  const question = questions[index];
  
  rl.question(question.question, (answer) => {
    let value = answer.trim();
    
    // Если пустой ответ и есть значение по умолчанию
    if (!value && question.default) {
      value = question.default();
    }
    
    // Валидация
    if (value && question.validate && !question.validate(value)) {
      console.log('❌ Неверный формат. Попробуйте еще раз.\n');
      askQuestion(index);
      return;
    }
    
    if (!value && !question.default) {
      console.log('❌ Это поле обязательно. Попробуйте еще раз.\n');
      askQuestion(index);
      return;
    }
    
    answers[question.key] = value;
    console.log('✅ Сохранено\n');
    askQuestion(index + 1);
  });
}

function createEnvFile() {
  const envContent = `# Server Configuration
PORT=8089
NODE_ENV=development

# Supabase Configuration
SUPABASE_URL=${answers.SUPABASE_URL}
SUPABASE_ANON_KEY=${answers.SUPABASE_ANON_KEY}
SUPABASE_SERVICE_ROLE_KEY=${answers.SUPABASE_SERVICE_ROLE_KEY}

# JWT Configuration
JWT_SECRET=${answers.JWT_SECRET}
JWT_EXPIRES_IN=24h

# Google OAuth Configuration
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8089/api/auth/google/callback

# n8n Webhook URLs
WEBHOOK_ANALYZE_COMPETENCIES=https://guglovskij.app.n8n.cloud/webhook/0d0a654b-772e-447a-9223-8b443f788175
WEBHOOK_ASSIGN_JOB_ROLE=https://guglovskij.app.n8n.cloud/webhook/113447c6-c39e-410c-ab15-4f5ab7809fd9
WEBHOOK_GENERATE_AI_PROFILE=https://guglovskij.app.n8n.cloud/webhook/bbd2959f-bedc-43fc-a558-69c0fe7b4db

# File Upload Configuration
UPLOAD_DIR=uploads
MAX_FILE_SIZE=10485760

# CORS Configuration
CORS_ORIGIN=http://localhost:3000
`;

  const envPath = path.join(__dirname, '.env');
  
  try {
    fs.writeFileSync(envPath, envContent);
    console.log('✅ Файл .env создан успешно!');
    console.log('📁 Путь:', envPath);
    console.log('\n🚀 Теперь перезапустите сервер:');
    console.log('   npm run dev');
    console.log('\n📋 Не забудьте создать таблицу users в Supabase!');
    console.log('   Смотрите SUPABASE_SETUP.md для инструкций.');
  } catch (error) {
    console.error('❌ Ошибка создания файла .env:', error.message);
  }
  
  rl.close();
}

// Начинаем опрос
askQuestion(0);
