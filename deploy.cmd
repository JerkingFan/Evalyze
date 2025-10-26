@echo off
REM 🚀 Быстрый деплой на сервер (Windows)

echo 🔨 Building application...
call gradlew.bat clean build -x test

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Build failed!
    exit /b 1
)

echo 📦 Uploading to server...
scp build\libs\NEW_NEW_MVP-0.0.1-SNAPSHOT.jar root@5.83.140.54:~/start.jar

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Upload failed!
    exit /b 1
)

echo 🔄 Restarting application on server...
ssh root@5.83.140.54 "pkill -f start.jar && sleep 2 && nohup java -jar start.jar > app.log 2>&1 &"

echo.
echo 🎉 Deployment complete!
echo 📝 View logs: ssh root@5.83.140.54 "tail -f app.log"

