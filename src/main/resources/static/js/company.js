// Company Panel JavaScript

class CompanyPanel {
    constructor() {
        this.profiles = [];
        this.init();
    }

    init() {
        this.checkAuth();
        this.loadProfiles();
        this.updateStats();
    }

        checkAuth() {
            const token = localStorage.getItem('token');
            const userRole = localStorage.getItem('userRole');
            const userEmail = localStorage.getItem('userEmail');
            
            if (!token || userRole !== 'COMPANY') {
                window.location.href = '/';
                return;
            }

            // Update auth buttons for company page
            this.updateCompanyAuthButtons(userRole, userEmail);
        }
        
        updateCompanyAuthButtons(userRole, userEmail) {
            // Find auth buttons container
            const authContainer = document.getElementById('authButtons');
            if (!authContainer) return;
            
            // Clear existing buttons
            authContainer.innerHTML = '';
            
            // Create user info and logout button
            const userInfo = document.createElement('div');
            userInfo.className = 'd-flex align-items-center gap-3';
            userInfo.innerHTML = `
                <div class="text-end">
                    <div class="text-white fw-bold">${userEmail}</div>
                    <div class="text-white-50 small">Компания</div>
                </div>
                <button class="btn btn-outline-evalyze" onclick="logout()">
                    <i class="fas fa-sign-out-alt me-2"></i>Выйти
                </button>
            `;
            
            authContainer.appendChild(userInfo);
        }

    async loadProfiles() {
        try {
            const token = localStorage.getItem('token');
            console.log('Loading profiles for company...');
            
            // Get current user's company ID first
            const userResponse = await fetch('/api/auth/me', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (!userResponse.ok) {
                console.error('Error getting current user');
                return;
            }
            
            const user = await userResponse.json();
            console.log('Current user:', user);
            
            if (!user.companyId) {
                console.error('User has no company ID');
                return;
            }
            
            // Load profiles for this company
            const response = await fetch(`/api/profiles/company/${user.companyId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });
            
            console.log('Profiles response status:', response.status);
            
            if (response.ok) {
                this.profiles = await response.json();
                console.log('Loaded profiles:', this.profiles);
                this.renderProfilesTable();
                this.updateStats();
            } else {
                console.error('Error loading profiles:', response.status);
                // Try to load all profiles as fallback
                this.loadAllProfiles();
            }
        } catch (error) {
            console.error('Error loading profiles:', error);
            // Try to load all profiles as fallback
            this.loadAllProfiles();
        }
    }
    
    async loadAllProfiles() {
        try {
            console.log('Trying to load all profiles...');
            const token = localStorage.getItem('token');
            
            // Try to get all profiles (this might work if user has admin role)
            const response = await fetch('/api/profiles/all', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (response.ok) {
                const allProfiles = await response.json();
                // Filter profiles that belong to current user's company
                // For now, just show all profiles
                this.profiles = allProfiles;
                console.log('Loaded all profiles:', this.profiles);
                this.renderProfilesTable();
                this.updateStats();
            } else {
                console.error('Error loading all profiles:', response.status);
            }
        } catch (error) {
            console.error('Error loading all profiles:', error);
        }
    }

    renderProfilesTable() {
        const tbody = document.getElementById('profilesTable');
        
        if (this.profiles.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted py-4">
                        <i class="fas fa-users fa-3x mb-3"></i><br>
                        Профили сотрудников не найдены
                    </td>
                </tr>
            `;
            return;
        }

            tbody.innerHTML = this.profiles.map(profile => {
                // Parse profile data to get current position
                let currentPosition = 'Не указано';
                try {
                    if (profile.profileData) {
                        const data = JSON.parse(profile.profileData);
                        currentPosition = data.currentPosition || 'Не указано';
                    }
                } catch (e) {
                    console.log('Error parsing profile data:', e);
                }
                
                return `
                <tr>
                    <td>
                        <div class="d-flex align-items-center">
                            <div class="avatar bg-primary text-white rounded-circle me-3 d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;">
                                ${profile.employeeName ? profile.employeeName.charAt(0).toUpperCase() : profile.employeeEmail.charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <div class="fw-bold">${profile.employeeName || 'Не указано'}</div>
                            </div>
                        </div>
                    </td>
                    <td>${profile.employeeEmail}</td>
                    <td>${currentPosition}</td>
                    <td>
                        <span class="badge bg-success">
                            Готов к использованию
                        </span>
                    </td>
                    <td>
                        ${profile.aiProfileGenerated ? 
                            '<span class="badge bg-success"><i class="fas fa-check me-1"></i>Готов</span>' : 
                            '<span class="badge bg-secondary"><i class="fas fa-clock me-1"></i>Не создан</span>'
                        }
                    </td>
                    <td>
                        <div class="btn-group" role="group">
                            <button class="btn btn-sm btn-outline-primary" onclick="companyPanel.viewProfile('${profile.userId}')" title="Просмотр">
                                <i class="fas fa-eye"></i>
                            </button>
                            ${!profile.aiProfileGenerated ? 
                                `<button class="btn btn-sm btn-success" onclick="companyPanel.generateAIProfile('${profile.userId}')" title="Создать AI-профиль">
                                    <i class="fas fa-robot"></i>
                                </button>` : ''
                            }
                        </div>
                    </td>
                </tr>
            `;
            }).join('');
    }

    updateStats() {
        const total = this.profiles.length;
        const aiProfiles = this.profiles.filter(p => p.aiProfileGenerated).length;
        const pending = this.profiles.filter(p => !p.aiProfileGenerated).length;

        document.getElementById('totalEmployees').textContent = total;
        document.getElementById('verifiedEmployees').textContent = total; // Все профили готовы
        document.getElementById('aiProfiles').textContent = aiProfiles;
        document.getElementById('pendingEmployees').textContent = pending;
    }

    async createEmployeeProfile() {
        const formData = {
            employeeEmail: document.getElementById('employeeEmail').value,
            employeeName: document.getElementById('employeeName').value,
            currentPosition: document.getElementById('currentPosition').value,
            currentSkills: document.getElementById('currentSkills').value,
            currentResponsibilities: document.getElementById('currentResponsibilities').value,
            desiredPosition: document.getElementById('desiredPosition').value,
            desiredSkills: document.getElementById('desiredSkills').value,
            careerGoals: document.getElementById('careerGoals').value
        };

        try {
            const token = localStorage.getItem('token');
            console.log('Creating profile with data:', formData);
            const response = await fetch('/api/profiles/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(formData)
            });
            
            console.log('Create profile response status:', response.status);

            if (response.ok) {
                const result = await response.json();
                console.log('Profile creation result:', result);
                alert('Профиль сотрудника создан и готов к использованию!');
                const modal = bootstrap.Modal.getInstance(document.getElementById('createProfileModal'));
                if (modal) modal.hide();
                document.getElementById('createProfileForm').reset();
                this.loadProfiles();
            } else {
                let errorMessage = 'Ошибка создания профиля';
                try {
                    const error = await response.json();
                    console.log('Error response:', error);
                    errorMessage = error.message || errorMessage;
                } catch (e) {
                    console.log('Error parsing response:', e);
                    errorMessage = `HTTP ${response.status}: ${response.statusText}`;
                }
                alert(errorMessage);
            }
        } catch (error) {
            console.error('Error creating profile:', error);
            alert('Ошибка соединения с сервером: ' + error.message);
        }
    }

    viewProfile(profileId) {
        const profile = this.profiles.find(p => p.userId === profileId);
        if (!profile) return;

        const content = `
            <div class="row">
                <div class="col-md-6">
                    <h6 class="text-primary mb-3">Информация о сотруднике</h6>
                    <table class="table table-sm">
                        <tr><td><strong>Имя:</strong></td><td>${profile.employeeName || 'Не указано'}</td></tr>
                        <tr><td><strong>Email:</strong></td><td>${profile.employeeEmail}</td></tr>
                        <tr><td><strong>Статус:</strong></td><td>
                            <span class="badge ${profile.isVerified ? 'bg-success' : 'bg-warning'}">
                                ${profile.isVerified ? 'Подтвержден' : 'Ожидает подтверждения'}
                            </span>
                        </td></tr>
                        <tr><td><strong>Создан:</strong></td><td>${new Date(profile.createdAt).toLocaleDateString('ru-RU')}</td></tr>
                    </table>
                </div>
                <div class="col-md-6">
                    <h6 class="text-primary mb-3">Текущая позиция</h6>
                    <table class="table table-sm">
                        <tr><td><strong>Должность:</strong></td><td>${profile.currentPosition || 'Не указано'}</td></tr>
                        <tr><td><strong>Навыки:</strong></td><td>${profile.currentSkills || 'Не указано'}</td></tr>
                        <tr><td><strong>Обязанности:</strong></td><td>${profile.currentResponsibilities || 'Не указано'}</td></tr>
                    </table>
                </div>
            </div>
            <div class="row mt-4">
                <div class="col-md-6">
                    <h6 class="text-success mb-3">Желаемые цели</h6>
                    <table class="table table-sm">
                        <tr><td><strong>Позиция:</strong></td><td>${profile.desiredPosition || 'Не указано'}</td></tr>
                        <tr><td><strong>Навыки:</strong></td><td>${profile.desiredSkills || 'Не указано'}</td></tr>
                        <tr><td><strong>Цели:</strong></td><td>${profile.careerGoals || 'Не указано'}</td></tr>
                    </table>
                </div>
                <div class="col-md-6">
                    <h6 class="text-warning mb-3">AI-профиль</h6>
                    ${profile.aiProfileGenerated ? 
                        `<div class="alert alert-success">
                            <strong>Статус:</strong> Создан<br>
                            <strong>Дата:</strong> ${new Date(profile.updatedAt).toLocaleDateString('ru-RU')}
                        </div>
                        <button class="btn btn-primary btn-sm" onclick="companyPanel.showAIProfile('${profile.userId}')">
                            <i class="fas fa-eye me-1"></i>Просмотр AI-профиля
                        </button>` :
                        `<div class="alert alert-secondary">
                            <strong>Статус:</strong> Не создан<br>
                            ${profile.isVerified ? 
                                '<button class="btn btn-success btn-sm mt-2" onclick="companyPanel.generateAIProfile(\'' + profile.userId + '\')"><i class="fas fa-robot me-1"></i>Создать AI-профиль</button>' :
                                '<small class="text-muted">Ожидает подтверждения сотрудника</small>'
                            }
                        </div>`
                    }
                </div>
            </div>
        `;

        document.getElementById('profileDetailsContent').innerHTML = content;
        new bootstrap.Modal(document.getElementById('profileDetailsModal')).show();
    }

    async generateAIProfile(profileId) {
        console.log('generateAIProfile called with profileId:', profileId);
        
        if (!profileId) {
            alert('Ошибка: ID профиля не найден');
            return;
        }
        
        if (!confirm('Создать AI-профиль для этого сотрудника? Будет отправлен webhook в n8n.')) {
            return;
        }

        // Найти профиль в списке
        const profile = this.profiles.find(p => p.userId === profileId);
        if (!profile) {
            alert('Профиль не найден');
            return;
        }

        // Подготовить данные для webhook
        const webhookData = {
            profileId: profile.userId,
            userEmail: profile.employeeEmail,
            userName: profile.employeeName,
            profileData: profile.profileData || '{}',
            companyName: 'Company Name', // Можно добавить получение названия компании
            timestamp: new Date().toISOString()
        };

        console.log('Sending webhook directly to n8n:', webhookData);

        try {
            // Отправляем запрос через backend (избегаем CORS)
            console.log('Sending request to backend:', `/api/profiles/generate-ai/${profileId}`);
            const response = await fetch(`/api/profiles/generate-ai/${profileId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const result = await response.json();
                console.log('Webhook sent successfully via backend:', result);
                alert('✅ Webhook отправлен в n8n! AI-профиль будет создан автоматически.');
                this.loadProfiles();
            } else {
                console.error('Webhook failed:', response.status, response.statusText);
                alert('❌ Ошибка отправки webhook: ' + response.status + ' ' + response.statusText);
            }
        } catch (error) {
            console.error('Error sending webhook:', error);
            alert('❌ Ошибка соединения с сервером: ' + error.message);
        }
    }

        showAIProfile(profileId) {
            console.log('showAIProfile called with profileId:', profileId);
            const profile = this.profiles.find(p => p.userId === profileId);
            if (!profile || !profile.aiProfileGenerated) return;

        const content = `
            <div class="card">
                <div class="card-header">
                    <h5 class="mb-0"><i class="fas fa-robot me-2"></i>AI-профиль сотрудника</h5>
                </div>
                <div class="card-body">
                    <pre class="bg-light p-3 rounded" style="white-space: pre-wrap; font-family: 'Segoe UI', sans-serif;">${profile.aiProfileGenerated}</pre>
                </div>
                <div class="card-footer">
                    <button class="btn btn-primary" onclick="companyPanel.downloadProfile('${profile.userId}')">
                        <i class="fas fa-download me-2"></i>Скачать как PDF
                    </button>
                    <button class="btn btn-secondary" onclick="companyPanel.sendProfileByEmail('${profile.userId}')">
                        <i class="fas fa-envelope me-2"></i>Отправить на email
                    </button>
                </div>
            </div>
        `;

        document.getElementById('profileDetailsContent').innerHTML = content;
        new bootstrap.Modal(document.getElementById('profileDetailsModal')).show();
    }

        downloadProfile(profileId) {
            const profile = this.profiles.find(p => p.userId === profileId);
            if (!profile || !profile.aiProfileGenerated) return;

        // Simple text download (in real app, you'd generate PDF)
        const element = document.createElement('a');
        const file = new Blob([profile.aiProfileGenerated], {type: 'text/plain'});
        element.href = URL.createObjectURL(file);
        element.download = `ai-profile-${profile.employeeName || profile.employeeEmail}.txt`;
        document.body.appendChild(element);
        element.click();
        document.body.removeChild(element);
    }

        async sendProfileByEmail(profileId) {
            const profile = this.profiles.find(p => p.userId === profileId);
            if (!profile || !profile.aiProfileGenerated) return;

        try {
            // In a real app, you'd have an endpoint to send email
            alert('Функция отправки email будет реализована в следующих версиях');
        } catch (error) {
            console.error('Error sending email:', error);
            alert('Ошибка отправки email');
        }
    }
}

// Global functions
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
    window.location.href = '/';
}

function createEmployeeProfile() {
    if (window.companyPanel) {
        window.companyPanel.createEmployeeProfile();
    } else {
        console.error('Company panel not initialized');
        alert('Ошибка: панель компании не инициализирована');
    }
}

// Initialize company panel
document.addEventListener('DOMContentLoaded', () => {
    window.companyPanel = new CompanyPanel();
});
