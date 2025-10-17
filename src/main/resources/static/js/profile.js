// Profile Panel JavaScript

class ProfilePanel {
    constructor() {
        this.currentProfile = null;
        this.init();
    }

    init() {
        this.checkAuth();
        this.setupEventListeners();
    }

    checkAuth() {
        const user = localStorage.getItem('user');
        if (user) {
            const userData = JSON.parse(user);
            document.getElementById('userName').textContent = userData.fullName;
        }
    }

    setupEventListeners() {
        // Profile search form
        document.getElementById('profileSearchForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.searchProfile();
        });

        // Verification form
        document.getElementById('verificationForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.verifyProfile();
        });

        // Build AI Profile button
        const buildBtn = document.getElementById('buildProfileBtn');
        console.log('buildProfileBtn element:', buildBtn);
        if (buildBtn) {
            buildBtn.addEventListener('click', (e) => {
                console.log('Build AI Profile button clicked!');
                e.preventDefault();
                this.buildAIProfile();
            });
            console.log('Event listener added to buildProfileBtn');
        } else {
            console.error('buildProfileBtn element not found!');
        }
    }

    async searchProfile() {
        const email = document.getElementById('searchEmail').value;
        
        if (!email) {
            app.showAlert('Пожалуйста, введите email', 'warning');
            return;
        }

        try {
            const response = await fetch(`/api/profiles/search?email=${encodeURIComponent(email)}`);
            
            if (response.ok) {
                this.currentProfile = await response.json();
                this.displayProfile();
            } else if (response.status === 404) {
                app.showAlert('Профиль с указанным email не найден. Обратитесь к вашей компании.', 'warning');
            } else {
                app.showAlert('Ошибка поиска профиля', 'danger');
            }
        } catch (error) {
            console.error('Error searching profile:', error);
            app.showAlert('Ошибка соединения с сервером', 'danger');
        }
    }

    displayProfile() {
        if (!this.currentProfile) return;

        console.log('displayProfile called with:', this.currentProfile);
        console.log('userId:', this.currentProfile.userId);

        // Update profile info
        document.getElementById('profileAvatar').textContent = 
            (this.currentProfile.employeeName || this.currentProfile.employeeEmail).charAt(0).toUpperCase();
        document.getElementById('profileName').textContent = 
            this.currentProfile.employeeName || 'Не указано';
        document.getElementById('profileEmail').textContent = this.currentProfile.employeeEmail;

        // Update verification status
        const verificationBadge = document.getElementById('verificationBadge');
        const buildProfileBtn = document.getElementById('buildProfileBtn');
        
        console.log('Profile verification status:', this.currentProfile.isVerified);
        console.log('Profile status:', this.currentProfile.status);
        
        // Временно активируем кнопку для тестирования webhook
        verificationBadge.className = 'badge bg-success';
        verificationBadge.textContent = 'Готов к использованию';
        buildProfileBtn.disabled = false;
        console.log('Button enabled for webhook testing');
        console.log('Button disabled state:', buildProfileBtn.disabled);
        console.log('Button element:', buildProfileBtn);
        
        // Добавляем стили для активной кнопки
        buildProfileBtn.style.cursor = 'pointer';
        buildProfileBtn.style.opacity = '1';
        
        // Оригинальная логика (закомментирована):
        // if (this.currentProfile.isVerified) {
        //     verificationBadge.className = 'badge bg-success';
        //     verificationBadge.textContent = 'Подтвержден';
        //     buildProfileBtn.disabled = false;
        //     console.log('Button enabled - profile is verified');
        // } else {
        //     verificationBadge.className = 'badge bg-warning';
        //     verificationBadge.textContent = 'Требует подтверждения';
        //     buildProfileBtn.disabled = true;
        //     console.log('Button disabled - profile not verified');
        // }

        // Update profile details
        document.getElementById('currentPosition').textContent = 
            this.currentProfile.currentPosition || 'Не указано';
        document.getElementById('currentSkills').textContent = 
            this.currentProfile.currentSkills || 'Не указано';
        document.getElementById('currentResponsibilities').textContent = 
            this.currentProfile.currentResponsibilities || 'Не указано';
        document.getElementById('desiredPosition').textContent = 
            this.currentProfile.desiredPosition || 'Не указано';
        document.getElementById('desiredSkills').textContent = 
            this.currentProfile.desiredSkills || 'Не указано';
        document.getElementById('careerGoals').textContent = 
            this.currentProfile.careerGoals || 'Не указано';

        // Show AI profile if exists
        if (this.currentProfile.aiProfileGenerated) {
            document.getElementById('aiProfileContent').textContent = this.currentProfile.aiProfileGenerated;
            document.getElementById('aiProfileCard').style.display = 'block';
        }

        // Show appropriate sections
        document.getElementById('profileSearchSection').style.display = 'none';
        document.getElementById('profileContentSection').style.display = 'block';
        
        if (!this.currentProfile.isVerified) {
            document.getElementById('verificationSection').style.display = 'block';
            document.getElementById('profileStatusSection').innerHTML = `
                <div class="col-12">
                    <div class="alert alert-warning" role="alert">
                        <div class="d-flex align-items-center">
                            <i class="fas fa-exclamation-triangle fa-2x me-3"></i>
                            <div>
                                <h5 class="alert-heading mb-1">Подтверждение требуется</h5>
                                <p class="mb-0">Ваш профиль создан компанией, но требует подтверждения через email</p>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        } else {
            document.getElementById('profileStatusSection').innerHTML = `
                <div class="col-12">
                    <div class="alert alert-success" role="alert">
                        <div class="d-flex align-items-center">
                            <i class="fas fa-check-circle fa-2x me-3"></i>
                            <div>
                                <h5 class="alert-heading mb-1">Профиль подтвержден</h5>
                                <p class="mb-0">Ваш профиль готов! Вы можете создать AI-профиль для получения рекомендаций</p>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }
    }

    async verifyProfile() {
        const verificationCode = document.getElementById('verificationCode').value;
        
        if (!verificationCode) {
            app.showAlert('Пожалуйста, введите код подтверждения', 'warning');
            return;
        }

        try {
            const response = await fetch(`/api/profiles/verify/${verificationCode}`, {
                method: 'POST'
            });

            if (response.ok) {
                app.showAlert('Профиль успешно подтвержден!', 'success');
                document.getElementById('verificationSection').style.display = 'none';
                
                // Update profile status
                this.currentProfile.isVerified = true;
                this.displayProfile();
            } else {
                const error = await response.text();
                app.showAlert(error || 'Неверный код подтверждения', 'danger');
            }
        } catch (error) {
            console.error('Error verifying profile:', error);
            app.showAlert('Ошибка соединения с сервером', 'danger');
        }
    }

    async buildAIProfile() {
        console.log('buildAIProfile called');
        console.log('currentProfile:', this.currentProfile);
        
        if (!this.currentProfile) {
            app.showAlert('Профиль не найден. Сначала найдите профиль по email.', 'warning');
            return;
        }
        
        // Временно убираем проверку на подтверждение для тестирования webhook
        // if (!this.currentProfile.isVerified) {
        //     app.showAlert('Профиль должен быть подтвержден перед созданием AI-профиля', 'warning');
        //     return;
        // }

        if (!confirm('Создать AI-профиль? Это может занять несколько секунд.')) {
            return;
        }

        const buildBtn = document.getElementById('buildProfileBtn');
        const originalText = buildBtn.innerHTML;
        buildBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Создание AI-профиля...';
        buildBtn.disabled = true;

        try {
            // Отправляем запрос через backend (избегаем CORS)
            console.log('Sending request to backend:', `/api/profiles/generate-ai/${this.currentProfile.userId}`);
            const response = await fetch(`/api/profiles/generate-ai/${this.currentProfile.userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const result = await response.json();
                console.log('Webhook sent successfully via backend:', result);
                
                // Show success message
                app.showAlert('✅ Webhook отправлен в n8n! AI-профиль будет создан автоматически.', 'success');
                
                // Update AI profile display if available
                if (document.getElementById('aiProfileContent')) {
                    document.getElementById('aiProfileContent').textContent = 'Webhook отправлен в n8n для генерации AI-профиля';
                    document.getElementById('aiProfileCard').style.display = 'block';
                    
                    // Scroll to AI profile
                    document.getElementById('aiProfileCard').scrollIntoView({ behavior: 'smooth' });
                }
            } else {
                console.error('Webhook failed:', response.status, response.statusText);
                app.showAlert('❌ Ошибка отправки webhook: ' + response.status + ' ' + response.statusText, 'danger');
            }
        } catch (error) {
            console.error('Error building AI profile:', error);
            app.showAlert('Ошибка соединения с сервером', 'danger');
        } finally {
            buildBtn.innerHTML = originalText;
            buildBtn.disabled = false;
        }
    }

    async resendVerificationCode() {
        if (!this.currentProfile) return;

        try {
            // In a real app, you'd have an endpoint to resend verification code
            app.showAlert('Функция повторной отправки кода будет реализована в следующих версиях', 'info');
        } catch (error) {
            console.error('Error resending verification code:', error);
            app.showAlert('Ошибка повторной отправки кода', 'danger');
        }
    }

    downloadProfile() {
        if (!this.currentProfile || !this.currentProfile.aiProfileGenerated) {
            app.showAlert('AI-профиль не создан', 'warning');
            return;
        }

        // Simple text download (in real app, you'd generate PDF)
        const element = document.createElement('a');
        const file = new Blob([this.currentProfile.aiProfileGenerated], {type: 'text/plain'});
        element.href = URL.createObjectURL(file);
        element.download = `ai-profile-${this.currentProfile.employeeName || this.currentProfile.employeeEmail}.txt`;
        document.body.appendChild(element);
        element.click();
        document.body.removeChild(element);
        
        app.showAlert('Профиль скачан!', 'success');
    }

    printProfile() {
        if (!this.currentProfile || !this.currentProfile.aiProfileGenerated) {
            app.showAlert('AI-профиль не создан', 'warning');
            return;
        }

        const printWindow = window.open('', '_blank');
        printWindow.document.write(`
            <html>
                <head>
                    <title>AI-Профиль - ${this.currentProfile.employeeName || this.currentProfile.employeeEmail}</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }
                        pre { white-space: pre-wrap; font-family: Arial, sans-serif; }
                    </style>
                </head>
                <body>
                    <h1>AI-Профиль сотрудника</h1>
                    <p><strong>Сотрудник:</strong> ${this.currentProfile.employeeName || 'Не указано'}</p>
                    <p><strong>Email:</strong> ${this.currentProfile.employeeEmail}</p>
                    <p><strong>Дата создания:</strong> ${new Date().toLocaleDateString('ru-RU')}</p>
                    <hr>
                    <pre>${this.currentProfile.aiProfileGenerated}</pre>
                </body>
            </html>
        `);
        printWindow.document.close();
        printWindow.print();
    }
}

// Global functions
function logout() {
    app.logout();
}

async function buildAIProfile() {
    console.log('Global buildAIProfile function called');
    
    // Получаем данные профиля из DOM напрямую
    const profileName = document.getElementById('profileName')?.textContent || 'Не указано';
    const profileEmail = document.getElementById('profileEmail')?.textContent || '';
    
    console.log('Profile data from DOM:', { profileName, profileEmail });
    
    if (!profileEmail || profileEmail === 'Загрузка...') {
        alert('Сначала найдите профиль по email');
        return;
    }
    
    if (!confirm('Создать AI-профиль? Будет отправлен webhook в n8n.')) {
        return;
    }
    
    try {
        // Пытаемся найти userId из window.currentProfile или из другого источника
        let userId = null;
        
        if (window.profilePanel && window.profilePanel.currentProfile) {
            userId = window.profilePanel.currentProfile.userId;
            console.log('Found userId from ProfilePanel:', userId);
        } else if (window.currentProfile) {
            userId = window.currentProfile.userId;
            console.log('Found userId from window.currentProfile:', userId);
        }
        
        if (!userId) {
            // Если userId не найден, используем email для поиска профиля
            console.log('UserId not found, searching by email...');
            const searchResponse = await fetch(`/api/profiles/search?email=${encodeURIComponent(profileEmail)}`);
            
            if (searchResponse.ok) {
                const profile = await searchResponse.json();
                userId = profile.userId;
                console.log('Found userId by search:', userId);
            } else {
                throw new Error('Profile not found');
            }
        }
        
        if (!userId) {
            alert('Не удалось найти ID профиля');
            return;
        }
        
        // Отправляем запрос через backend
        console.log('Sending request to backend:', `/api/profiles/generate-ai/${userId}`);
        const response = await fetch(`/api/profiles/generate-ai/${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            console.log('Webhook sent successfully via backend:', result);
            alert('✅ Webhook отправлен в n8n! AI-профиль будет создан автоматически.');
            
            // Update AI profile display if available
            if (document.getElementById('aiProfileContent')) {
                document.getElementById('aiProfileContent').textContent = 'Webhook отправлен в n8n для генерации AI-профиля';
                document.getElementById('aiProfileCard').style.display = 'block';
                
                // Scroll to AI profile
                document.getElementById('aiProfileCard').scrollIntoView({ behavior: 'smooth' });
            }
        } else {
            console.error('Webhook failed:', response.status, response.statusText);
            alert('❌ Ошибка отправки webhook: ' + response.status + ' ' + response.statusText);
        }
    } catch (error) {
        console.error('Error sending webhook:', error);
        alert('❌ Ошибка соединения с сервером: ' + error.message);
    }
}

// Альтернативная функция для прямого вызова
window.enableBuildButton = function() {
    const buildBtn = document.getElementById('buildProfileBtn');
    if (buildBtn) {
        buildBtn.disabled = false;
        buildBtn.style.cursor = 'pointer';
        buildBtn.style.opacity = '1';
        console.log('Button manually enabled');
    }
};

function downloadProfile() {
    if (window.profilePanel) {
        window.profilePanel.downloadProfile();
    } else {
        console.error('ProfilePanel not initialized yet');
        alert('Система еще загружается. Попробуйте через несколько секунд.');
    }
}

function printProfile() {
    if (window.profilePanel) {
        window.profilePanel.printProfile();
    } else {
        console.error('ProfilePanel not initialized yet');
        alert('Система еще загружается. Попробуйте через несколько секунд.');
    }
}

function resendVerificationCode() {
    if (window.profilePanel) {
        window.profilePanel.resendVerificationCode();
    } else {
        console.error('ProfilePanel not initialized yet');
        alert('Система еще загружается. Попробуйте через несколько секунд.');
    }
}

// Initialize profile panel
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM loaded, initializing ProfilePanel...');
    window.profilePanel = new ProfilePanel();
    console.log('ProfilePanel initialized successfully');
    
    // Дополнительная проверка кнопки после инициализации
    setTimeout(() => {
        const buildBtn = document.getElementById('buildProfileBtn');
        console.log('Post-init buildProfileBtn check:', buildBtn);
        if (buildBtn) {
            console.log('Button disabled state after init:', buildBtn.disabled);
            console.log('Button style:', buildBtn.style);
        }
    }, 1000);
});
