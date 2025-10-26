// Функция для создания мок-клиента
const createMockClient = () => ({
  from: () => ({
    select: () => ({
      eq: () => ({
        single: () => Promise.resolve({ data: null, error: { code: 'PGRST116' } })
      }),
      limit: () => Promise.resolve({ data: [], error: null })
    }),
    insert: () => ({
      select: () => ({
        single: () => Promise.resolve({ data: null, error: null })
      })
    }),
    update: () => ({
      eq: () => ({
        select: () => ({
          single: () => Promise.resolve({ data: null, error: null })
        })
      })
    }),
    delete: () => ({
      eq: () => Promise.resolve({ error: null })
    })
  })
});

let supabase, supabaseAdmin;

// Проверяем валидность URL
const isValidUrl = (url) => {
  if (!url) return false;
  try {
    const urlObj = new URL(url);
    return urlObj.protocol === 'https:' && urlObj.hostname.includes('supabase');
  } catch {
    return false;
  }
};

// Пытаемся создать реальный клиент
try {
  const supabaseUrl = process.env.SUPABASE_URL;
  const supabaseAnonKey = process.env.SUPABASE_ANON_KEY;
  const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
  
  // Проверяем, что URL и ключи валидны
  if (isValidUrl(supabaseUrl) && supabaseAnonKey && supabaseAnonKey.length > 20) {
    const { createClient } = require('@supabase/supabase-js');
    
    supabase = createClient(supabaseUrl, supabaseAnonKey);
    supabaseAdmin = createClient(supabaseUrl, supabaseServiceKey || supabaseAnonKey);
    
    console.log('✅ Using real Supabase configuration');
    console.log('🔗 Supabase URL:', supabaseUrl);
  } else {
    throw new Error('Invalid Supabase configuration');
  }
} catch (error) {
  // Если не удалось создать реальный клиент, используем мок
  console.warn('⚠️  Using mock Supabase configuration for testing');
  console.warn('⚠️  Reason:', error.message);
  console.warn('⚠️  To use real Supabase:');
  console.warn('    1. Set SUPABASE_URL to valid Supabase project URL (https://xxx.supabase.co)');
  console.warn('    2. Set SUPABASE_ANON_KEY to your anon/public key');
  console.warn('    3. Set SUPABASE_SERVICE_ROLE_KEY to your service role key (optional)');
  
  supabase = createMockClient();
  supabaseAdmin = createMockClient();
}

module.exports = {
  supabase,
  supabaseAdmin
};
