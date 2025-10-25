-- Evalyze PostgreSQL Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Drop existing tables if they exist (in correct order due to foreign keys)
DROP TABLE IF EXISTS company_content CASCADE;
DROP TABLE IF EXISTS profile_snapshots CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;
DROP TABLE IF EXISTS invitations CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS companies CASCADE;

-- Drop existing ENUM types if they exist
DROP TYPE IF EXISTS content_type CASCADE;
DROP TYPE IF EXISTS profile_status CASCADE;
DROP TYPE IF EXISTS invitation_status CASCADE;
DROP TYPE IF EXISTS user_role CASCADE;

-- Create ENUM types
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('EMPLOYEE', 'ADMIN', 'COMPANY');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE invitation_status AS ENUM ('PENDING', 'ACCEPTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE profile_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE content_type AS ENUM ('JOB_ROLE', 'INTERNAL_VACANCY', 'COURSE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create companies table
CREATE TABLE IF NOT EXISTS companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID REFERENCES companies(id) ON DELETE CASCADE,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    google_oauth_token JSONB
);

-- Create invitations table
CREATE TABLE IF NOT EXISTS invitations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    invitation_code VARCHAR(255) UNIQUE NOT NULL,
    status invitation_status NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP
);

-- Create profiles table
CREATE TABLE IF NOT EXISTS profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    profile_data JSONB,
    status profile_status NOT NULL DEFAULT 'PENDING',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create profile_snapshots table
CREATE TABLE IF NOT EXISTS profile_snapshots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    snapshot_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    profile_data JSONB
);

-- Create company_content table
CREATE TABLE IF NOT EXISTS company_content (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    content_type content_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    data JSONB
);

-- Create email_verifications table
CREATE TABLE IF NOT EXISTS email_verifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL,
    verification_code VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    attempts INTEGER DEFAULT 0
);

-- Create file_uploads table
CREATE TABLE IF NOT EXISTS file_uploads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    tags VARCHAR(500)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_company_id ON users(company_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

CREATE INDEX IF NOT EXISTS idx_invitations_company_id ON invitations(company_id);
CREATE INDEX IF NOT EXISTS idx_invitations_email ON invitations(email);
CREATE INDEX IF NOT EXISTS idx_invitations_code ON invitations(invitation_code);
CREATE INDEX IF NOT EXISTS idx_invitations_status ON invitations(status);

CREATE INDEX IF NOT EXISTS idx_profiles_user_id ON profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_profiles_status ON profiles(status);

CREATE INDEX IF NOT EXISTS idx_snapshots_user_id ON profile_snapshots(user_id);
CREATE INDEX IF NOT EXISTS idx_snapshots_date ON profile_snapshots(snapshot_date);

CREATE INDEX IF NOT EXISTS idx_content_company_id ON company_content(company_id);
CREATE INDEX IF NOT EXISTS idx_content_type ON company_content(content_type);

CREATE INDEX IF NOT EXISTS idx_email_verifications_email ON email_verifications(email);
CREATE INDEX IF NOT EXISTS idx_email_verifications_code ON email_verifications(verification_code);
CREATE INDEX IF NOT EXISTS idx_email_verifications_expires ON email_verifications(expires_at);
CREATE INDEX IF NOT EXISTS idx_email_verifications_used ON email_verifications(is_used);

CREATE INDEX IF NOT EXISTS idx_file_uploads_user_id ON file_uploads(user_id);
CREATE INDEX IF NOT EXISTS idx_file_uploads_uploaded_at ON file_uploads(uploaded_at);
CREATE INDEX IF NOT EXISTS idx_file_uploads_mime_type ON file_uploads(mime_type);

-- Create indexes on JSONB columns for better performance
CREATE INDEX IF NOT EXISTS idx_users_google_token ON users USING GIN (google_oauth_token);
CREATE INDEX IF NOT EXISTS idx_profiles_data ON profiles USING GIN (profile_data);
CREATE INDEX IF NOT EXISTS idx_snapshots_data ON profile_snapshots USING GIN (profile_data);
CREATE INDEX IF NOT EXISTS idx_content_data ON company_content USING GIN (data);
