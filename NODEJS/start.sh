#!/bin/bash

# Evalyze Node.js Backend Startup Script

echo "🚀 Starting Evalyze Node.js Backend..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found. Copying from env.example..."
    cp env.example .env
    echo "📝 Please edit .env file with your configuration before running again."
    exit 1
fi

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

# Create uploads directory if it doesn't exist
if [ ! -d "uploads" ]; then
    echo "📁 Creating uploads directory..."
    mkdir -p uploads
fi

# Set environment
export NODE_ENV=${NODE_ENV:-development}

# Start the application
echo "🎯 Starting server in $NODE_ENV mode..."
if [ "$NODE_ENV" = "development" ]; then
    npm run dev
else
    npm start
fi
