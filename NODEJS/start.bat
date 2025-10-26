@echo off
REM Evalyze Node.js Backend Startup Script for Windows

echo 🚀 Starting Evalyze Node.js Backend...

REM Check if .env file exists
if not exist .env (
    echo ⚠️  .env file not found. Copying from env.example...
    copy env.example .env
    echo 📝 Please edit .env file with your configuration before running again.
    pause
    exit /b 1
)

REM Check if node_modules exists
if not exist node_modules (
    echo 📦 Installing dependencies...
    npm install
)

REM Create uploads directory if it doesn't exist
if not exist uploads (
    echo 📁 Creating uploads directory...
    mkdir uploads
)

REM Set environment
if "%NODE_ENV%"=="" set NODE_ENV=development

REM Start the application
echo 🎯 Starting server in %NODE_ENV% mode...
if "%NODE_ENV%"=="development" (
    npm run dev
) else (
    npm start
)
