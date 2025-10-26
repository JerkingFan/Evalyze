const { supabase, supabaseAdmin } = require('./supabase');

// Database connection test
const testConnection = async () => {
  try {
    // Проверяем, используется ли реальная конфигурация
    const isRealConfig = process.env.SUPABASE_URL && 
                        process.env.SUPABASE_URL !== 'https://placeholder.supabase.co' &&
                        process.env.SUPABASE_ANON_KEY &&
                        process.env.SUPABASE_ANON_KEY !== 'placeholder-anon-key';
    
    if (!isRealConfig) {
      console.warn('⚠️  Using mock database connection for testing');
      return true; // Возвращаем true для тестирования
    }
    
    const { data, error } = await supabase
      .from('users')
      .select('count')
      .limit(1);
    
    if (error) {
      console.error('❌ Database connection failed:', error.message);
      return false;
    }
    
    console.log('✅ Database connection successful');
    return true;
  } catch (error) {
    console.error('❌ Database connection error:', error.message);
    return false;
  }
};

// Initialize database connection
const initializeDatabase = async () => {
  console.log('🔌 Initializing database connection...');
  const isConnected = await testConnection();
  
  if (!isConnected) {
    throw new Error('Failed to connect to database');
  }
  
  return { supabase, supabaseAdmin };
};

module.exports = {
  supabase,
  supabaseAdmin,
  testConnection,
  initializeDatabase
};
