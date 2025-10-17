// IT-Profile AI Application JavaScript

class ITProfileApp {
    constructor() {
        this.apiBaseUrl = '/api';
        this.token = localStorage.getItem('token');
        this.currentUser = null;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.checkAuthStatus();
        this.setupRoleToggle();
    }

    setupEventListeners() {
        // Login form
        document.getElementById('loginForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleLogin();
        });

        // Register form
        document.getElementById('registerForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleRegister();
        });

        // Role toggle
        document.getElementById('registerRole')?.addEventListener('change', (e) => {
            this.toggleCompanyNameField(e.target.value);
        });
    }

    setupRoleToggle() {
        const roleSelect = document.getElementById('registerRole');
        if (roleSelect) {
            roleSelect.addEventListener('change', (e) => {
                this.toggleCompanyNameField(e.target.value);
            });
        }
    }

    toggleCompanyNameField(role) {
        const companyNameGroup = document.getElementById('companyNameGroup');
        const companyNameInput = document.getElementById('registerCompanyName');
        
        if (role === 'COMPANY') {
            companyNameGroup.style.display = 'block';
            companyNameInput.required = true;
        } else {
            companyNameGroup.style.display = 'none';
            companyNameInput.required = false;
            companyNameInput.value = '';
        }
    }

    async handleLogin() {
        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value;

        try {
            const response = await fetch(`${this.apiBaseUrl}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (response.ok) {
                this.token = data.token;
                this.currentUser = data;
                localStorage.setItem('token', this.token);
                localStorage.setItem('user', JSON.stringify(data));
                
                this.showAlert('Успешный вход в систему!', 'success');
                this.hideModal('loginModal');
                this.updateNavigation();
                
                // Redirect based on role
                if (data.role === 'COMPANY') {
                    window.location.href = '/company';
                } else {
                    window.location.href = '/profile';
                }
            } else {
                this.showAlert(data.message || 'Ошибка входа', 'danger');
            }
        } catch (error) {
            console.error('Login error:', error);
            this.showAlert('Ошибка соединения с сервером', 'danger');
        }
    }

    async handleRegister() {
        const role = document.getElementById('registerRole').value;
        const email = document.getElementById('registerEmail').value;
        const password = document.getElementById('registerPassword').value;
        const fullName = document.getElementById('registerFullName').value;
        const companyName = document.getElementById('registerCompanyName').value;

        const registerData = {
            email,
            password,
            fullName,
            role
        };

        if (role === 'COMPANY' && companyName) {
            registerData.companyName = companyName;
        }

        try {
            const response = await fetch(`${this.apiBaseUrl}/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(registerData)
            });

            const data = await response.json();

            if (response.ok) {
                this.token = data.token;
                this.currentUser = data;
                localStorage.setItem('token', this.token);
                localStorage.setItem('user', JSON.stringify(data));
                
                this.showAlert('Регистрация успешна!', 'success');
                this.hideModal('registerModal');
                this.updateNavigation();
                
                // Redirect based on role
                if (data.role === 'COMPANY') {
                    window.location.href = '/company';
                } else {
                    window.location.href = '/profile';
                }
            } else {
                this.showAlert(data.message || 'Ошибка регистрации', 'danger');
            }
        } catch (error) {
            console.error('Register error:', error);
            this.showAlert('Ошибка соединения с сервером', 'danger');
        }
    }

    checkAuthStatus() {
        const user = localStorage.getItem('user');
        if (user && this.token) {
            this.currentUser = JSON.parse(user);
            this.updateNavigation();
        }
    }

    updateNavigation() {
        const nav = document.querySelector('.navbar-nav');
        if (!nav) return;

        if (this.currentUser) {
            // Update navigation for logged in user
            nav.innerHTML = `
                <li class="nav-item">
                    <a class="nav-link active" href="/">Главная</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/company">Компания</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/profile">Профиль</a>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                        <i class="fas fa-user me-1"></i>${this.currentUser.fullName}
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#" onclick="app.logout()">
                            <i class="fas fa-sign-out-alt me-2"></i>Выйти
                        </a></li>
                    </ul>
                </li>
            `;
        }
    }

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        this.token = null;
        this.currentUser = null;
        this.updateNavigation();
        window.location.href = '/';
    }

    showModal(modalId) {
        const modal = new bootstrap.Modal(document.getElementById(modalId));
        modal.show();
    }

    hideModal(modalId) {
        const modal = bootstrap.Modal.getInstance(document.getElementById(modalId));
        if (modal) {
            modal.hide();
        }
    }

    showAlert(message, type = 'info') {
        // Remove existing alerts
        const existingAlerts = document.querySelectorAll('.alert');
        existingAlerts.forEach(alert => alert.remove());

        // Create new alert
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        alertDiv.style.cssText = 'top: 100px; right: 20px; z-index: 9999; min-width: 300px;';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        document.body.appendChild(alertDiv);

        // Auto remove after 5 seconds
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }

    async makeAuthenticatedRequest(url, options = {}) {
        if (!this.token) {
            throw new Error('No authentication token');
        }

        const defaultOptions = {
            headers: {
                'Authorization': `Bearer ${this.token}`,
                'Content-Type': 'application/json',
                ...options.headers
            }
        };

        return fetch(url, { ...options, ...defaultOptions });
    }

    scrollToFeatures() {
        document.getElementById('features')?.scrollIntoView({
            behavior: 'smooth'
        });
    }

    // Animation on scroll
    observeElements() {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('fade-in');
                }
            });
        });

        document.querySelectorAll('.feature-card, .step-card').forEach(el => {
            observer.observe(el);
        });
    }
}

// Global functions for modal handling
function showLoginModal() {
    app.showModal('loginModal');
}

function showRegisterModal() {
    app.showModal('registerModal');
}

function scrollToFeatures() {
    app.scrollToFeatures();
}

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.app = new ITProfileApp();
    window.app.observeElements();
});

// Add smooth scrolling for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});
