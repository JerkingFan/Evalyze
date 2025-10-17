-- Evalyze Database Setup Script
-- Run this script on your PostgreSQL server before deploying the application

-- Create database for Evalyze application
CREATE DATABASE evalyze;

-- Connect to the new database and create extension
\c evalyze;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Verify the setup
SELECT 'Database evalyze created successfully!' as status;
SELECT 'UUID extension enabled!' as extension_status;
