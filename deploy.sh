#!/bin/bash

# 🚀 Быстрый деплой на сервер

echo "🔨 Building application..."
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "📦 Uploading to server..."
scp build/libs/NEW_NEW_MVP-0.0.1-SNAPSHOT.jar root@5.83.140.54:~/start.jar

if [ $? -ne 0 ]; then
    echo "❌ Upload failed!"
    exit 1
fi

echo "🔄 Restarting application on server..."
ssh root@5.83.140.54 << 'EOF'
    pkill -f start.jar
    sleep 2
    nohup java -jar start.jar > app.log 2>&1 &
    echo "✅ Application restarted!"
    sleep 3
    echo "📊 Checking logs..."
    tail -20 app.log
EOF

echo ""
echo "🎉 Deployment complete!"
echo "📝 View logs: ssh root@5.83.140.54 'tail -f app.log'"

