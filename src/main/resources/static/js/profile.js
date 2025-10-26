// Profile Panel JavaScript

class ProfilePanel {
    constructor() {
        this.currentProfile = null;
        this.init();
    }

    init() {
        this.checkAuth();
        this.setupEventListeners();
        this.loadCurrentUserProfile(); // Автоматически загружаем профиль
    }

    checkAuth() {
        const user = localStorage.getItem('user');
        if (user) {
            const userData = JSON.parse(user);
            document.getElementById('userName').textContent = userData.fullName;
        }
    }

    setupEventListeners() {
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

    async loadCurrentUserProfile() {
        // Получаем email текущего пользователя из localStorage
        const user = localStorage.getItem('user');
        if (!user) {
            console.error('User not found in localStorage');
            app.showAlert('Пожалуйста, войдите в систему', 'warning');
            return;
        }

        const userData = JSON.parse(user);
        const email = userData.email || userData.username;
        
        console.log('Loading profile for email:', email);

        if (!email) {
            console.error('Email not found in user data');
            app.showAlert('Не удалось определить email пользователя', 'warning');
            return;
        }

        try {
            const response = await fetch(`/api/profiles/search?email=${encodeURIComponent(email)}`);
            console.log('Profile search response status:', response.status);
            
            if (response.ok) {
                this.currentProfile = await response.json();
                console.log('Profile loaded:', this.currentProfile);
                console.log('Profile userId:', this.currentProfile.userId);
                this.displayProfile();
            } else if (response.status === 404) {
                console.log('Profile not found (404), creating new profile for:', email);
                // Если профиля нет, создаем новый
                const created = await this.createNewProfile(userData);
                if (created && this.currentProfile) {
                    console.log('New profile created with userId:', this.currentProfile.userId);
                }
            } else {
                console.error('Error loading profile, status:', response.status);
                app.showAlert('Ошибка загрузки профиля', 'danger');
            }
        } catch (error) {
            console.error('Error loading profile:', error);
            app.showAlert('Ошибка соединения с сервером', 'danger');
        }
    }

    async createNewProfile(userData) {
        // Создаем новый профиль для текущего пользователя через API
        try {
            console.log('Creating new profile for user:', userData);
            
            // Создаём профиль через API
            const response = await fetch('/api/profiles/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + localStorage.getItem('token')
                },
                body: JSON.stringify({
                    employeeEmail: userData.email || userData.username,
                    employeeName: userData.fullName || userData.username || 'Пользователь',
                    currentPosition: '',
                    currentSkills: '',
                    currentResponsibilities: '',
                    desiredPosition: '',
                    desiredSkills: '',
                    careerGoals: ''
                })
            });
            
            if (response.ok) {
                const result = await response.json();
                console.log('Profile created successfully:', result);
                
                // Загружаем созданный профиль
                this.currentProfile = {
                    userId: result.userId,
                    employeeEmail: result.employeeEmail,
                    employeeName: result.employeeName,
                    status: result.status
                };
                
                console.log('Setting currentProfile with userId:', this.currentProfile.userId);
                
                // Показываем профиль
                this.displayProfile();
                app.showAlert('Добро пожаловать! Ваш профиль создан', 'success');
                return true; // Success
            } else {
                // Если создание не удалось, показываем базовую информацию
                console.error('Failed to create profile:', response.status);
                
                document.getElementById('profileName').textContent = userData.fullName || userData.username || 'Пользователь';
                document.getElementById('profileEmail').textContent = userData.email || userData.username || '';
                
                const verificationBadge = document.getElementById('verificationBadge');
                verificationBadge.className = 'badge bg-warning';
                verificationBadge.textContent = 'Новый пользователь';
                
                app.showAlert('Добро пожаловать! Заполните свой профиль для начала работы', 'info');
                return false; // Failed
            }
        } catch (error) {
            console.error('Error creating profile:', error);
            app.showAlert('Ошибка создания профиля: ' + error.message, 'danger');
            return false; // Failed
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

        // Profile is already displayed
        
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
            // Create a basic profile if none exists
            const userEmail = localStorage.getItem('userEmail') || document.getElementById('profileEmail')?.textContent;
            if (!userEmail) {
                alert('Не удалось определить email пользователя');
                return;
            }
            
            // Create minimal profile
            this.currentProfile = {
                userId: null, // Will be set by backend
                employeeEmail: userEmail,
                employeeName: userEmail.split('@')[0],
                status: 'COMPLETED'
            };
            
            console.log('Created minimal profile for:', userEmail);
        }
        
        // If userId is null, try to find it by email
        if (!this.currentProfile.userId) {
            console.log('userId is null, searching for profile by email:', this.currentProfile.employeeEmail);
            try {
                const searchResponse = await fetch(`/api/profiles/search?email=${encodeURIComponent(this.currentProfile.employeeEmail)}`);
                if (searchResponse.ok) {
                    const profile = await searchResponse.json();
                    this.currentProfile.userId = profile.userId;
                    console.log('Found userId:', this.currentProfile.userId);
                } else {
                    console.error('Profile not found by email');
                    alert('Профиль не найден. Пожалуйста, войдите в систему заново.');
                    return;
                }
            } catch (error) {
                console.error('Error searching for profile:', error);
                alert('Ошибка при поиске профиля');
                return;
            }
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
            console.error('Error building AI profile:', error);
            alert('Ошибка соединения с сервером: ' + error.message);
        } finally {
            buildBtn.innerHTML = originalText;
            buildBtn.disabled = false;
        }
    }

    async resendVerificationCode() {
        if (!this.currentProfile) return;

        try {
            // In a real app, you'd have an endpoint to resend verification code
            alert('Функция повторной отправки кода будет реализована в следующих версиях');
        } catch (error) {
            console.error('Error resending verification code:', error);
            alert('Ошибка повторной отправки кода');
        }
    }

    downloadProfile() {
        if (!this.currentProfile || !this.currentProfile.aiProfileGenerated) {
            alert('AI-профиль не создан');
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
        
        alert('Профиль скачан!');
    }

    printProfile() {
        if (!this.currentProfile || !this.currentProfile.aiProfileGenerated) {
            alert('AI-профиль не создан');
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
    let profileEmail = document.getElementById('profileEmail')?.textContent || '';
    
    console.log('Profile data from DOM:', { profileName, profileEmail });
    
    // If email is not available from DOM, try to get it from other sources
    if (!profileEmail || profileEmail === 'Загрузка...') {
        profileEmail = localStorage.getItem('userEmail') || prompt('Введите ваш email:');
        if (!profileEmail) {
            alert('Не удалось определить email пользователя');
            return;
        }
        console.log('Using email from alternative source:', profileEmail);
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

// Функции для работы с тремя кнопками

/**
 * Кнопка 1: Показать модальное окно для загрузки и анализа файлов
 */
window.showUploadAndAnalyzeModal = function() {
    const modal = new bootstrap.Modal(document.getElementById('employeeUploadModal'));
    modal.show();
};

/**
 * Кнопка 2: Показать модальное окно с выбором ролей
 */
window.showJobRolesModal = async function() {
    const modal = new bootstrap.Modal(document.getElementById('jobRolesModal'));
    
    // Загружаем список ролей
    await loadJobRoles();
    
    modal.show();
};

/**
 * Загрузить список ролей из базы данных
 */
async function loadJobRoles() {
    const jobRolesList = document.getElementById('jobRolesList');
    jobRolesList.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Загрузка...</span></div></div>';
    
    try {
        console.log('Loading job roles from /api/job-roles...');
        const response = await fetch('/api/job-roles');
        
        console.log('Response status:', response.status);
        console.log('Response ok:', response.ok);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            throw new Error('Ошибка загрузки ролей: ' + response.status + ' ' + response.statusText);
        }
        
        const jobRoles = await response.json();
        console.log('Received job roles:', jobRoles);
        
        if (jobRoles.length === 0) {
            jobRolesList.innerHTML = '<div class="alert alert-warning">Роли пока не добавлены в систему</div>';
            return;
        }
        
        let html = '<div class="row g-3">';
        jobRoles.forEach(role => {
            html += `
                <div class="col-md-6">
                    <div class="card" style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1);">
                        <div class="card-body">
                            <h6 class="card-title">${escapeHtml(role.title)}</h6>
                            ${role.description ? `<p class="text-muted small mb-2">${escapeHtml(role.description.substring(0, 100))}${role.description.length > 100 ? '...' : ''}</p>` : ''}
                            ${role.roleType ? `<span class="badge bg-info mb-2">${escapeHtml(role.roleType)}</span>` : ''}
                            <button class="btn btn-primary btn-sm w-100" onclick="selectJobRole('${role.id}', '${role.title}')">
                                <i class="fas fa-check me-2"></i>Выбрать эту роль
                            </button>
                        </div>
                    </div>
                </div>
            `;
        });
        html += '</div>';
        
        jobRolesList.innerHTML = html;
    } catch (error) {
        console.error('Error loading job roles:', error);
        jobRolesList.innerHTML = '<div class="alert alert-danger">Ошибка загрузки ролей: ' + error.message + '</div>';
    }
}

/**
 * Выбрать роль и назначить её пользователю
 */
window.selectJobRole = async function(roleId, roleTitle) {
    if (!confirm(`Вы уверены, что хотите выбрать роль "${roleTitle}"?`)) {
        return;
    }
    
    // Получаем userId из разных источников
    let userId = null;
    let finalEmail = null;
    
    // 1. Попробуем получить из currentProfile
    if (window.profilePanel && window.profilePanel.currentProfile) {
        userId = window.profilePanel.currentProfile.userId;
        console.log('Got userId from currentProfile:', userId);
    }
    
    // 2. Попробуем получить из localStorage
    if (!userId) {
        const user = localStorage.getItem('user');
        if (user) {
            const userData = JSON.parse(user);
            const email = userData.email || userData.username;
            console.log('Got email from localStorage:', email);
            if (email) finalEmail = email;
            
            if (email) {
                try {
                    const searchResponse = await fetch(`/api/profiles/search?email=${encodeURIComponent(email)}`);
                    console.log('Profile search response status:', searchResponse.status);
                    
                    if (searchResponse.ok) {
                        const profile = await searchResponse.json();
                        userId = profile.userId;
                        console.log('Got userId from profile search:', userId);
                    } else if (searchResponse.status === 404) {
                        // Если профиля нет, создаём его
                        console.log('Profile not found, creating...');
                        const createResponse = await fetch('/api/profiles/create', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                employeeEmail: email,
                                employeeName: userData.fullName || email,
                                currentPosition: '',
                                currentSkills: '',
                                currentResponsibilities: '',
                                desiredPosition: '',
                                desiredSkills: '',
                                careerGoals: ''
                            })
                        });
                        
                        if (createResponse.ok) {
                            const createdProfile = await createResponse.json();
                            userId = createdProfile.userId;
                            console.log('Profile created successfully, userId:', userId);
                            // Сохраняем userId для дальнейшего использования
                            if (window.profilePanel) {
                                window.profilePanel.currentProfile = {
                                    userId: userId,
                                    employeeEmail: email
                                };
                            }
                        } else {
                            const errorText = await createResponse.text();
                            console.error('Profile creation failed:', errorText);
                            alert('❌ Ошибка создания профиля: ' + errorText);
                            return;
                        }
                    }
                } catch (error) {
                    console.error('Error searching profile by localStorage email:', error);
                }
            }
        }
    }

    // 2.1 Доп. попытка: взять email из localStorage по ключу 'userEmail'
    if (!userId) {
        const lsEmail = localStorage.getItem('userEmail');
        if (lsEmail) {
            finalEmail = finalEmail || lsEmail;
            try {
                const searchResponse = await fetch(`/api/profiles/search?email=${encodeURIComponent(lsEmail)}`);
                console.log('Profile search (userEmail) response status:', searchResponse.status);
                if (searchResponse.ok) {
                    const profile = await searchResponse.json();
                    userId = profile.userId;
                    console.log('Got userId from userEmail search:', userId);
                } else if (searchResponse.status === 404) {
                    console.log('Profile not found for userEmail, creating...');
                    const createResponse = await fetch('/api/profiles/create', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            employeeEmail: lsEmail,
                            employeeName: lsEmail.split('@')[0],
                            currentPosition: '', currentSkills: '', currentResponsibilities: '',
                            desiredPosition: '', desiredSkills: '', careerGoals: ''
                        })
                    });
                    if (createResponse.ok) {
                        const createdProfile = await createResponse.json();
                        userId = createdProfile.userId;
                        console.log('Profile created (userEmail) successfully, userId:', userId);
                        if (window.profilePanel) {
                            window.profilePanel.currentProfile = { userId, employeeEmail: lsEmail };
                        }
                    }
                }
            } catch (e) {
                console.error('Error using userEmail from localStorage:', e);
            }
        }
    }
    
    // 3. Попробуем получить из DOM
    if (!userId) {
        let profileEmail = document.getElementById('profileEmail')?.textContent;
        console.log('Got email from DOM:', profileEmail);
        if (profileEmail === 'Загрузка...' || !profileEmail) {
            profileEmail = '';
        }
        
        if (profileEmail) {
            if (!finalEmail) finalEmail = profileEmail;
            try {
                const searchResponse = await fetch(`/api/profiles/search?email=${encodeURIComponent(profileEmail)}`);
                console.log('Profile search response status:', searchResponse.status);
                
                if (searchResponse.ok) {
                    const profile = await searchResponse.json();
                    userId = profile.userId;
                    console.log('Got userId from DOM email search:', userId);
                } else if (searchResponse.status === 404) {
                    // Если профиля нет, создаём его
                    console.log('Profile not found from DOM, creating...');
                    const createResponse = await fetch('/api/profiles/create', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            employeeEmail: profileEmail,
                            employeeName: profileEmail.split('@')[0],
                            currentPosition: '',
                            currentSkills: '',
                            currentResponsibilities: '',
                            desiredPosition: '',
                            desiredSkills: '',
                            careerGoals: ''
                        })
                    });
                    
                    if (createResponse.ok) {
                        const createdProfile = await createResponse.json();
                        userId = createdProfile.userId;
                        console.log('Profile created from DOM successfully, userId:', userId);
                        // Сохраняем userId для дальнейшего использования
                        if (window.profilePanel) {
                            window.profilePanel.currentProfile = {
                                userId: userId,
                                employeeEmail: profileEmail
                            };
                        }
                    } else {
                        const errorText = await createResponse.text();
                        console.error('Profile creation failed:', errorText);
                        alert('❌ Ошибка создания профиля: ' + errorText);
                        return;
                    }
                }
            } catch (error) {
                console.error('Error searching profile by DOM email:', error);
            }
        }
    }

    // 4. Последний шанс: спросить email вручную и создать/найти профиль
    if (!userId) {
        const promptEmail = prompt('Введите ваш email для назначения роли:');
        if (promptEmail) {
            finalEmail = finalEmail || promptEmail;
            try {
                const searchResponse = await fetch(`/api/profiles/search?email=${encodeURIComponent(promptEmail)}`);
                if (searchResponse.ok) {
                    const profile = await searchResponse.json();
                    userId = profile.userId;
                } else if (searchResponse.status === 404) {
                    const createResponse = await fetch('/api/profiles/create', {
                        method: 'POST', headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            employeeEmail: promptEmail,
                            employeeName: promptEmail.split('@')[0],
                            currentPosition: '', currentSkills: '', currentResponsibilities: '',
                            desiredPosition: '', desiredSkills: '', careerGoals: ''
                        })
                    });
                    if (createResponse.ok) {
                        const createdProfile = await createResponse.json();
                        userId = createdProfile.userId;
                        if (window.profilePanel) {
                            window.profilePanel.currentProfile = { userId, employeeEmail: promptEmail };
                        }
                    }
                }
            } catch (e) {
                console.error('Error resolving email via prompt:', e);
            }
        }
    }
    
    if (!userId) {
        console.error('Failed to determine userId from all sources');
        alert('Ошибка: не удалось определить ID пользователя. Попробуйте перезагрузить страницу.');
        return;
    }
    
    console.log('Final userId for role assignment:', userId);
    
    // Отправляем запрос на назначение роли (гибкий endpoint с email)
    try {
        const response = await fetch(`/api/profiles/assign-role`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ jobRoleId: roleId, email: finalEmail })
        });
        
        if (response.ok) {
            const result = await response.json();
            console.log('Job role assigned successfully:', result);
            alert(`✅ Роль "${roleTitle}" успешно назначена!`);
            
            // Закрываем модальное окно
            const modal = bootstrap.Modal.getInstance(document.getElementById('jobRolesModal'));
            if (modal) {
                modal.hide();
            }
            
            // Перезагружаем страницу для обновления данных
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            const error = await response.text();
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error assigning job role:', error);
        alert('❌ Ошибка назначения роли: ' + error.message);
    }
};

/**
 * Экранирование HTML для безопасности
 */
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}
