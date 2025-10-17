// Evalyze - Premium Animations and Interactions

document.addEventListener('DOMContentLoaded', function() {
    initAnimations();
    initParticles();
    initCounters();
    initScrollEffects();
    initNavbar();
    
    // Auth functions
    // Handle register form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
    
    // Handle login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    // Handle role selection
    const roleSelect = document.getElementById('registerRole');
    if (roleSelect) {
        roleSelect.addEventListener('change', handleRoleChange);
    }
    
    // Handle profile search form
    const profileSearchForm = document.getElementById('profileSearchForm');
    if (profileSearchForm) {
        profileSearchForm.addEventListener('submit', handleProfileSearch);
    }
    
    // Check user role and redirect
    checkUserRole();
});

// Fade in animations on load
function initAnimations() {
    const loadingElements = document.querySelectorAll('.loading');
    
    setTimeout(() => {
        loadingElements.forEach((element, index) => {
            setTimeout(() => {
                element.classList.add('loaded');
            }, index * 100);
        });
    }, 300);
}

// Create particle background
function initParticles() {
    const particlesContainer = document.getElementById('particles');
    if (!particlesContainer) return;
    
    const particleCount = 30;
    
    for (let i = 0; i < particleCount; i++) {
        const particle = document.createElement('div');
        particle.className = 'particle';
        
        // Random position
        particle.style.left = Math.random() * 100 + '%';
        particle.style.top = Math.random() * 100 + '%';
        
        // Random animation delay
        particle.style.animationDelay = Math.random() * 15 + 's';
        particle.style.animationDuration = (15 + Math.random() * 10) + 's';
        
        particlesContainer.appendChild(particle);
    }
}

// Animated counters
function initCounters() {
    const counters = document.querySelectorAll('.stat-number');
    
    const observerOptions = {
        threshold: 0.5
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const counter = entry.target;
                const target = parseInt(counter.getAttribute('data-target'));
                animateCounter(counter, target);
                observer.unobserve(counter);
            }
        });
    }, observerOptions);
    
    counters.forEach(counter => observer.observe(counter));
}

function animateCounter(element, target) {
    const duration = 2000;
    const steps = 60;
    const increment = target / steps;
    let current = 0;
    
    const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
            element.textContent = target.toLocaleString();
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(current).toLocaleString();
        }
    }, duration / steps);
}

// Scroll effects
function initScrollEffects() {
    const elements = document.querySelectorAll('.feature-card, .process-step');
    
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -100px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    elements.forEach(element => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        element.style.transition = 'all 0.6s ease-out';
        observer.observe(element);
    });
}

// Navbar scroll effect
function initNavbar() {
    const navbar = document.querySelector('.navbar-evalyze');
    
    window.addEventListener('scroll', () => {
        if (window.scrollY > 100) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
}

// Smooth scroll
function scrollToFeatures() {
    const featuresSection = document.getElementById('features');
    if (featuresSection) {
        featuresSection.scrollIntoView({ behavior: 'smooth' });
    }
}

function scrollToSearch() {
    const searchSection = document.getElementById('profileSearch');
    if (searchSection) {
        searchSection.scrollIntoView({ behavior: 'smooth' });
    }
}

// Modal functions
function showLoginModal() {
    const modal = new bootstrap.Modal(document.getElementById('loginModal'));
    modal.show();
}

function showRegisterModal() {
    const modal = new bootstrap.Modal(document.getElementById('registerModal'));
    modal.show();
}

function openCreateProfileModal() {
    const modal = new bootstrap.Modal(document.getElementById('createProfileModal'));
    modal.show();
}

// Auth functions

function handleRoleChange() {
    const role = document.getElementById('registerRole').value;
    const companyGroup = document.getElementById('companyNameGroup');
    
    if (role === 'COMPANY') {
        companyGroup.style.display = 'block';
    } else {
        companyGroup.style.display = 'none';
    }
}

async function handleRegister(e) {
    e.preventDefault();
    
    const role = document.getElementById('registerRole').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    const fullName = document.getElementById('registerFullName').value;
    const companyName = document.getElementById('registerCompanyName').value;
    
        try {
            const registerData = {
                role: role,
                email: email,
                password: password,
                fullName: fullName,
                companyName: role === 'COMPANY' ? companyName : null
            };
            console.log('Registering with data:', registerData);
            
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(registerData)
            });
        
        const result = await response.json();
        
        if (response.ok) {
            console.log('Registration successful:', result);
            // Store token
            localStorage.setItem('token', result.token);
            localStorage.setItem('userRole', result.role);
            localStorage.setItem('userEmail', result.email);
            
            // Close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
            modal.hide();
            
            // Redirect based on role
            if (result.role === 'EMPLOYEE') {
                window.location.href = '/profile';
            } else if (result.role === 'COMPANY') {
                window.location.href = '/company';
            }
        } else {
            console.log('Registration failed:', result);
            alert('Ошибка регистрации: ' + result.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Ошибка регистрации');
    }
}

async function handleLogin(e) {
    e.preventDefault();
    
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    
        try {
            const loginData = {
                email: email,
                password: password
            };
            console.log('Logging in with data:', loginData);
            
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(loginData)
            });
        
        const result = await response.json();
        
        if (response.ok) {
            console.log('Login successful:', result);
            // Store token
            localStorage.setItem('token', result.token);
            localStorage.setItem('userRole', result.role);
            localStorage.setItem('userEmail', result.email);
            
            // Close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
            modal.hide();
            
            // Redirect based on role
            if (result.role === 'EMPLOYEE') {
                window.location.href = '/profile';
            } else if (result.role === 'COMPANY') {
                window.location.href = '/company';
            }
        } else {
            console.log('Login failed:', result);
            alert('Ошибка входа: ' + result.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Ошибка входа');
    }
}

    function checkUserRole() {
        const token = localStorage.getItem('token');
        const userRole = localStorage.getItem('userRole');
        const userEmail = localStorage.getItem('userEmail');
        
        if (token && userRole) {
            // Update navigation based on role
            updateNavigation(userRole);
            
            // Hide/show sections based on role
            updatePageContent(userRole);
            
            // Update auth buttons
            updateAuthButtons(userRole, userEmail);
        }
    }

function updateNavigation(userRole) {
    const navLinks = document.querySelectorAll('.nav-link-evalyze');
    
    navLinks.forEach(link => {
        if (userRole === 'EMPLOYEE') {
            // Employees see profile link, not company
            if (link.getAttribute('href') === '/company') {
                link.style.display = 'none';
            }
        } else if (userRole === 'COMPANY') {
            // Companies see company link, not profile search
            if (link.getAttribute('href') === '/profile') {
                link.style.display = 'none';
            }
        }
    });
}

function updateAuthButtons(userRole, userEmail) {
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
            <div class="text-white-50 small">${getRoleDisplayName(userRole)}</div>
        </div>
        <button class="btn btn-outline-evalyze" onclick="logout()">
            <i class="fas fa-sign-out-alt me-2"></i>Выйти
        </button>
    `;
    
    authContainer.appendChild(userInfo);
}

function getRoleDisplayName(role) {
    switch(role) {
        case 'EMPLOYEE': return 'Сотрудник';
        case 'COMPANY': return 'Компания';
        case 'ADMIN': return 'Администратор';
        default: return role;
    }
}

function logout() {
    // Clear localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
    
    // Redirect to home page
    window.location.href = '/';
}

function updatePageContent(userRole) {
    if (window.location.pathname === '/profile') {
        if (userRole === 'COMPANY') {
            // Companies shouldn't see profile search
            const searchSection = document.getElementById('profileSearch');
            if (searchSection) {
                searchSection.innerHTML = `
                    <div class="alert alert-warning text-center">
                        <i class="fas fa-exclamation-triangle fa-2x mb-3"></i>
                        <h4>Доступ запрещен</h4>
                        <p>Эта страница предназначена только для сотрудников.</p>
                        <a href="/company" class="btn btn-primary-evalyze">Перейти в панель компании</a>
                    </div>
                `;
            }
        }
    } else if (window.location.pathname === '/company') {
        if (userRole === 'EMPLOYEE') {
            // Employees shouldn't see company dashboard
            document.body.innerHTML = `
                <div class="container mt-5 pt-5">
                    <div class="row justify-content-center">
                        <div class="col-lg-6">
                            <div class="alert alert-warning text-center">
                                <i class="fas fa-exclamation-triangle fa-2x mb-3"></i>
                                <h4>Доступ запрещен</h4>
                                <p>Эта страница предназначена только для компаний.</p>
                                <a href="/profile" class="btn btn-primary-evalyze">Перейти к профилю</a>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }
    }
}

async function handleProfileSearch(e) {
    e.preventDefault();
    
    const email = document.getElementById('searchEmail').value;
    
    if (!email) {
        alert('Введите email для поиска');
        return;
    }
    
    try {
        // Show loading state
        const searchBtn = e.target.querySelector('button[type="submit"]');
        const originalText = searchBtn.innerHTML;
        searchBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Поиск...';
        searchBtn.disabled = true;
        
        const response = await fetch(`/api/profiles/search?email=${encodeURIComponent(email)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });
        
        console.log('Search response status:', response.status);
        const result = await response.json();
        console.log('Search result:', result);
        
        // Reset button state
        searchBtn.innerHTML = originalText;
        searchBtn.disabled = false;
        
        if (response.ok && result) {
            // Profile found - show profile content
            displayProfile(result);
            showProfileContent();
        } else {
            // Profile not found
            alert('Профиль с таким email не найден. Убедитесь, что компания создала профиль для этого email.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Ошибка поиска профиля');
        
        // Reset button state
        const searchBtn = e.target.querySelector('button[type="submit"]');
        searchBtn.innerHTML = '<i class="fas fa-search me-2"></i>Найти профиль';
        searchBtn.disabled = false;
    }
}

function displayProfile(profile) {
    console.log('Displaying profile:', profile);
    
    // Update profile info
    const profileNameEl = document.getElementById('profileName');
    const profileEmailEl = document.getElementById('profileEmail');
    const profileAvatarEl = document.getElementById('profileAvatar');
    
    if (profileNameEl) profileNameEl.textContent = profile.employeeName || 'Не указано';
    if (profileEmailEl) profileEmailEl.textContent = profile.employeeEmail || '';
    
    // Set avatar initial
    const name = profile.employeeName || profile.employeeEmail || '?';
    if (profileAvatarEl) profileAvatarEl.textContent = name.charAt(0).toUpperCase();
    
    // Update verification status
    const verificationBadge = document.getElementById('verificationBadge');
    const buildBtn = document.getElementById('buildProfileBtn');
    
    if (verificationBadge) {
        verificationBadge.className = 'badge bg-success';
        verificationBadge.textContent = 'Готов к использованию';
    }
    if (buildBtn) {
        buildBtn.disabled = false; // Кнопка всегда активна
    }
    
    // Parse profile data if it's a string
    let profileData = profile.profileData;
    if (typeof profileData === 'string') {
        try {
            profileData = JSON.parse(profileData);
        } catch (e) {
            console.error('Error parsing profile data:', e);
            profileData = {};
        }
    }
    
    // Update current position
    const currentPositionEl = document.getElementById('currentPosition');
    const currentSkillsEl = document.getElementById('currentSkills');
    const currentResponsibilitiesEl = document.getElementById('currentResponsibilities');
    
    if (currentPositionEl) currentPositionEl.textContent = profileData.currentPosition || 'Не указано';
    if (currentSkillsEl) currentSkillsEl.textContent = profileData.currentSkills || 'Не указано';
    if (currentResponsibilitiesEl) currentResponsibilitiesEl.textContent = profileData.currentResponsibilities || 'Не указано';
    
    // Update career goals
    const desiredPositionEl = document.getElementById('desiredPosition');
    const desiredSkillsEl = document.getElementById('desiredSkills');
    const careerGoalsEl = document.getElementById('careerGoals');
    
    if (desiredPositionEl) desiredPositionEl.textContent = profileData.desiredPosition || 'Не указано';
    if (desiredSkillsEl) desiredSkillsEl.textContent = profileData.desiredSkills || 'Не указано';
    if (careerGoalsEl) careerGoalsEl.textContent = profileData.careerGoals || 'Не указано';
    
    // Store profile data for AI generation
    window.currentProfile = profile;
}

function showProfileContent() {
    // Hide search section
    const searchSection = document.getElementById('profileSearch');
    if (searchSection) {
        searchSection.style.display = 'none';
    }
    
    // Show profile content section
    const profileContent = document.getElementById('profileContent');
    if (profileContent) {
        profileContent.style.display = 'block';
        profileContent.scrollIntoView({ behavior: 'smooth' });
    }
}

function buildAIProfile() {
    if (!window.currentProfile) {
        alert('Профиль не найден');
        return;
    }
    
    const buildBtn = document.getElementById('buildProfileBtn');
    const originalText = buildBtn.innerHTML;
    buildBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Генерация...';
    buildBtn.disabled = true;
    
    // Simulate AI profile generation (replace with actual API call)
    setTimeout(() => {
        const aiProfile = generateAIProfile(window.currentProfile);
        displayAIProfile(aiProfile);
        
        buildBtn.innerHTML = '<i class="fas fa-check me-2"></i>Профиль готов';
        buildBtn.disabled = false;
    }, 3000);
}

function generateAIProfile(profile) {
    // Parse profile data
    let profileData = profile.profileData;
    if (typeof profileData === 'string') {
        try {
            profileData = JSON.parse(profileData);
        } catch (e) {
            profileData = {};
        }
    }
    
    const aiProfile = `
AI-ПРОФИЛЬ РАЗВИТИЯ
====================

Имя: ${profile.employeeName || 'Не указано'}
Email: ${profile.employeeEmail || 'Не указано'}

ТЕКУЩЕЕ ПОЛОЖЕНИЕ:
==================
Должность: ${profileData.currentPosition || 'Не указано'}
Навыки: ${profileData.currentSkills || 'Не указано'}
Обязанности: ${profileData.currentResponsibilities || 'Не указано'}

КАРЬЕРНЫЕ ЦЕЛИ:
===============
Желаемая позиция: ${profileData.desiredPosition || 'Не указано'}
Необходимые навыки: ${profileData.desiredSkills || 'Не указано'}
Карьерные цели: ${profileData.careerGoals || 'Не указано'}

РЕКОМЕНДАЦИИ ПО РАЗВИТИЮ:
=========================

1. ТЕХНИЧЕСКИЕ НАВЫКИ:
   - Изучите современные фреймворки и библиотеки
   - Практикуйтесь в решении алгоритмических задач
   - Изучайте архитектурные паттерны и принципы

2. SOFT SKILLS:
   - Развивайте навыки коммуникации и презентации
   - Изучайте принципы agile-методологий
   - Практикуйтесь в менторстве и обучении коллег

3. КАРЬЕРНЫЙ РОСТ:
   - Участвуйте в профессиональных конференциях
   - Ведите технический блог или YouTube канал
   - Получайте сертификации в вашей области

4. ПРАКТИЧЕСКИЕ ШАГИ:
   - Создайте портфолио проектов
   - Участвуйте в open-source проектах
   - Наладьте связи в профессиональном сообществе

Сгенерировано: ${new Date().toLocaleString('ru-RU')}
    `;
    
    return aiProfile;
}

function displayAIProfile(aiProfile) {
    document.getElementById('aiProfileContent').textContent = aiProfile;
    document.getElementById('aiProfileCard').style.display = 'block';
    
    // Scroll to AI profile
    document.getElementById('aiProfileCard').scrollIntoView({ behavior: 'smooth' });
}

function downloadProfile() {
    const aiProfile = document.getElementById('aiProfileContent').textContent;
    const blob = new Blob([aiProfile], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ai-profile-${window.currentProfile?.employeeEmail || 'profile'}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
}

function printProfile() {
    const aiProfile = document.getElementById('aiProfileContent').textContent;
    const printWindow = window.open('', '_blank');
    printWindow.document.write(`
        <html>
            <head>
                <title>AI-Профиль</title>
                <style>
                    body { font-family: 'Segoe UI', sans-serif; margin: 20px; }
                    pre { white-space: pre-wrap; }
                </style>
            </head>
            <body>
                <pre>${aiProfile}</pre>
            </body>
        </html>
    `);
    printWindow.document.close();
    printWindow.print();
}

// Smooth scroll for all anchor links
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

// Add ripple effect to buttons
document.querySelectorAll('.btn').forEach(button => {
    button.addEventListener('click', function(e) {
        const ripple = document.createElement('span');
        const rect = this.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;
        
        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = x + 'px';
        ripple.style.top = y + 'px';
        ripple.classList.add('ripple');
        
        this.appendChild(ripple);
        
        setTimeout(() => {
            ripple.remove();
        }, 600);
    });
});

// Add cursor trail effect (optional)
let mouseX = 0;
let mouseY = 0;
let cursorX = 0;
let cursorY = 0;

document.addEventListener('mousemove', (e) => {
    mouseX = e.clientX;
    mouseY = e.clientY;
});

function animateCursor() {
    cursorX += (mouseX - cursorX) * 0.1;
    cursorY += (mouseY - cursorY) * 0.1;
    
    requestAnimationFrame(animateCursor);
}

animateCursor();

// Parallax effect for hero section
window.addEventListener('scroll', () => {
    const scrolled = window.pageYOffset;
    const parallax = document.querySelector('.hero-evalyze');
    
    if (parallax) {
        parallax.style.transform = `translateY(${scrolled * 0.5}px)`;
    }
});

// Add loading progress bar
window.addEventListener('load', () => {
    const progressBar = document.createElement('div');
    progressBar.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 0;
        height: 3px;
        background: var(--gradient-yellow);
        z-index: 9999;
        transition: width 0.3s ease;
    `;
    document.body.appendChild(progressBar);
    
    window.addEventListener('scroll', () => {
        const winScroll = document.body.scrollTop || document.documentElement.scrollTop;
        const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
        const scrolled = (winScroll / height) * 100;
        progressBar.style.width = scrolled + '%';
    });
});
