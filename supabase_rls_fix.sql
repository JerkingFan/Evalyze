-- ============================================
-- Supabase RLS Fix for Registration (400 Error)
-- ============================================

-- Решение 1: Отключить RLS на таблице users (самый простой способ)
-- Используйте это, если хотите разрешить любые операции с users
ALTER TABLE public.users DISABLE ROW LEVEL SECURITY;

-- ИЛИ

-- Решение 2: Настроить правильные RLS политики (более безопасный способ)
-- Сначала включаем RLS
-- ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

-- Разрешаем INSERT для анонимных пользователей (для регистрации)
-- CREATE POLICY "Allow public insert for registration" 
-- ON public.users 
-- FOR INSERT 
-- TO anon 
-- WITH CHECK (true);

-- Разрешаем SELECT для авторизованных пользователей (свои данные)
-- CREATE POLICY "Users can view own data" 
-- ON public.users 
-- FOR SELECT 
-- TO authenticated 
-- USING (auth.uid()::text = id::text);

-- Разрешаем UPDATE для авторизованных пользователей (свои данные)
-- CREATE POLICY "Users can update own data" 
-- ON public.users 
-- FOR UPDATE 
-- TO authenticated 
-- USING (auth.uid()::text = id::text);

-- ============================================
-- Проверка текущего состояния RLS
-- ============================================

-- Проверить, включен ли RLS на таблице
SELECT 
    tablename, 
    rowsecurity AS rls_enabled
FROM pg_tables 
WHERE schemaname = 'public' 
    AND tablename = 'users';

-- Посмотреть существующие политики
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual,
    with_check
FROM pg_policies
WHERE schemaname = 'public' 
    AND tablename = 'users';

-- ============================================
-- Также проверьте таблицу companies
-- ============================================

ALTER TABLE public.companies DISABLE ROW LEVEL SECURITY;

-- ИЛИ настройте политики для companies:
-- ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;
-- CREATE POLICY "Allow public insert for companies" ON public.companies FOR INSERT TO anon WITH CHECK (true);
-- CREATE POLICY "Allow authenticated to view companies" ON public.companies FOR SELECT TO authenticated USING (true);

