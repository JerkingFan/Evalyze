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
    
    // Handle activation code form
    const activationCodeForm = document.getElementById('activationCodeForm');
    if (activationCodeForm) {
        activationCodeForm.addEventListener('submit', handleActivationCodeLogin);
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
    console.log('showLoginModal called');
    const modal = document.getElementById('loginModal');
    if (modal) {
        // Простое отображение модального окна без Bootstrap
        modal.style.display = 'block';
        modal.classList.add('show');
        document.body.classList.add('modal-open');
        
        // Добавляем backdrop
        const backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop fade show';
        backdrop.id = 'loginModalBackdrop';
        backdrop.onclick = hideLoginModal;
        document.body.appendChild(backdrop);
        
        console.log('Login modal shown');
    } else {
        console.error('Login modal not found');
    }
}

function showRegisterModal() {
    console.log('showRegisterModal called');
    const modal = document.getElementById('registerModal');
    if (modal) {
        // Простое отображение модального окна без Bootstrap
        modal.style.display = 'block';
        modal.classList.add('show');
        document.body.classList.add('modal-open');
        
        // Добавляем backdrop
        const backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop fade show';
        backdrop.id = 'registerModalBackdrop';
        backdrop.onclick = hideRegisterModal;
        document.body.appendChild(backdrop);
        
        console.log('Register modal shown');
    } else {
        console.error('Register modal not found');
    }
}

// Функции для закрытия модальных окон
function hideLoginModal() {
    const modal = document.getElementById('loginModal');
    const backdrop = document.getElementById('loginModalBackdrop');
    if (modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
        document.body.classList.remove('modal-open');
    }
    if (backdrop) {
        backdrop.remove();
    }
}

function hideRegisterModal() {
    const modal = document.getElementById('registerModal');
    const backdrop = document.getElementById('registerModalBackdrop');
    if (modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
        document.body.classList.remove('modal-open');
    }
    if (backdrop) {
        backdrop.remove();
    }
}

function hideUploadModal() {
    const modal = document.getElementById('employeeUploadModal');
    if (modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
        document.body.classList.remove('modal-open');
    }
    // Удаляем все backdrop'ы
    const backdrops = document.querySelectorAll('.modal-backdrop');
    backdrops.forEach(backdrop => backdrop.remove());
}

function openCreateProfileModal() {
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap не загружен! Попробуйте перезагрузить страницу.');
        return;
    }
    const modal = new bootstrap.Modal(document.getElementById('createProfileModal'));
    modal.show();
}

// Auth functions

function handleRoleChange() {
    const roleSelect = document.getElementById('registerRole');
    const companyGroup = document.getElementById('companyNameGroup');
    
    if (!roleSelect || !companyGroup) {
        return; // Elements don't exist in email auth version
    }
    
    const role = roleSelect.value;
    
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
            // Store token and user data
            localStorage.setItem('token', result.token);
            localStorage.setItem('user', JSON.stringify(result.user));
            localStorage.setItem('userRole', result.user.role || 'EMPLOYEE');
            localStorage.setItem('userEmail', result.user.email);
            
            // Close modal
            hideRegisterModal();
            
            // Redirect based on role
            if (result.user.role === 'COMPANY') {
                window.location.href = '/company';
            } else {
                window.location.href = '/profile';
            }
        } else {
            console.log('Registration failed:', result);
            console.log('Response status:', response.status);
            alert('Ошибка регистрации: ' + (result.message || result.error || 'Неизвестная ошибка'));
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Ошибка регистрации');
    }
}

async function handleActivationCodeLogin(e) {
    e.preventDefault();
    
    const activationCode = document.getElementById('activationCode').value;
    
    try {
        console.log('Logging in with activation code:', activationCode);
        
        const response = await fetch('/api/auth/login/activation-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ activationCode: activationCode })
        });
    
        const result = await response.json();
        
        if (response.ok) {
            console.log('Login by activation code successful:', result);
            // Store token and user data
            localStorage.setItem('token', result.token);
            localStorage.setItem('user', JSON.stringify(result.user));
            localStorage.setItem('userRole', result.user.role || 'EMPLOYEE');
            localStorage.setItem('userEmail', result.user.email);
            
            // Close modal
            const modal = document.getElementById('loginModal');
            if (modal) {
                modal.style.display = 'none';
                modal.classList.remove('show');
                document.body.classList.remove('modal-open');
            }
            
            // Redirect to profile (сотрудники всегда идут в профиль)
            window.location.href = '/profile';
        } else {
            console.error('Login by activation code failed:', result);
            alert('Ошибка входа: ' + (result.message || 'Неверный код активации'));
        }
    } catch (error) {
        console.error('Login by activation code error:', error);
        alert('Ошибка входа: ' + error.message);
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
            // Store token and user data
            localStorage.setItem('token', result.token);
            localStorage.setItem('user', JSON.stringify(result.user));
            localStorage.setItem('userRole', result.user.role || 'EMPLOYEE');
            localStorage.setItem('userEmail', result.user.email);
            
            // Close modal
            hideLoginModal();
            
            // Redirect based on role
            if (result.user.role === 'COMPANY') {
                window.location.href = '/company';
            } else {
                window.location.href = '/profile';
            }
        } else {
            console.log('Login failed:', result);
            alert('Ошибка входа: ' + (result.error || result.message || 'Неизвестная ошибка'));
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Ошибка входа');
    }
    }
    
    // Google OAuth function
    function googleLogin() {
        console.log('🔥 Google login clicked!');
        
        try {
            const clientId = '340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com';
            const redirectUri = encodeURIComponent('https://24beface.ru/oauth-bridge.html');
            const scope = encodeURIComponent('openid profile email https://www.googleapis.com/auth/drive.readonly');
            
            const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?` +
                `client_id=${clientId}&` +
                `redirect_uri=${redirectUri}&` +
                `response_type=code&` +
                `scope=${scope}&` +
                `access_type=offline&` +
                `prompt=consent&` +
                `state=${Date.now()}`;
            
            console.log('Redirecting to Google OAuth:', googleAuthUrl);
            window.location.href = googleAuthUrl;
        } catch (error) {
            console.error('Google login error:', error);
            alert('Ошибка при входе через Google: ' + error.message);
        }
    }
    
    // Quick file upload functions
    async function quickUploadAndAnalyze() {
        const fileInput = document.getElementById('quickFileInput');
        const files = fileInput.files;
        const description = document.getElementById('quickFileDescription').value;
        const tags = document.getElementById('quickFileTags').value;

        if (files.length === 0) {
            showQuickMessage('Выберите файлы для загрузки', 'warning');
            return;
        }

        // Проверяем авторизацию
        const token = localStorage.getItem('token');
        if (!token) {
            showQuickMessage('Необходимо войти в систему для загрузки файлов', 'error');
            setTimeout(() => {
                showLoginModal();
            }, 2000);
            return;
        }

        try {
            // Показываем прогресс
            showQuickProgress();
            updateQuickProgress(0, 'Подготовка к загрузке...');

            // Валидация файлов
            const validationResult = validateQuickFiles(files);
            if (!validationResult.valid) {
                showQuickMessage(validationResult.message, 'error');
                hideQuickProgress();
                return;
            }

            // Загружаем файлы
            updateQuickProgress(20, 'Загрузка файлов...');
            const uploadedFiles = await uploadQuickFiles(files, description, tags, token);
            
            if (uploadedFiles.length === 0) {
                showQuickMessage('Ошибка загрузки файлов', 'error');
                hideQuickProgress();
                return;
            }

            // Сохраняем файлы в localStorage для персистентности
            const existingFiles = JSON.parse(localStorage.getItem('uploadedFiles') || '[]');
            const allFiles = [...existingFiles, ...uploadedFiles];
            localStorage.setItem('uploadedFiles', JSON.stringify(allFiles));

            // Анализируем файлы
            updateQuickProgress(50, 'Анализ файлов...');
            const analysisResult = await analyzeQuickFiles(uploadedFiles, token);
            
            // Показываем результаты
            updateQuickProgress(100, 'Анализ завершен!');
            showQuickResults(analysisResult);
            
        } catch (error) {
            console.error('Quick upload error:', error);
            showQuickMessage('Ошибка загрузки и анализа файлов', 'error');
            hideQuickProgress();
        }
    }

    function validateQuickFiles(files) {
        const maxFileSize = 50 * 1024 * 1024; // 50MB
        const allowedTypes = [
            'image/jpeg', 'image/png', 'image/gif', 'image/webp',
            'application/pdf', 'text/plain', 'text/csv',
            'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        ];
        
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            
            if (file.size > maxFileSize) {
                return {
                    valid: false,
                    message: `Файл "${file.name}" слишком большой. Максимальный размер: 50MB`
                };
            }
            
            if (!allowedTypes.includes(file.type)) {
                return {
                    valid: false,
                    message: `Файл "${file.name}" имеет неподдерживаемый тип`
                };
            }
        }
        
        return { valid: true };
    }

    async function uploadQuickFiles(files, description, tags, token) {
        const uploadedFiles = [];
        const totalFiles = files.length;
        
        // Загружаем файлы по одному для лучшего контроля
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const progress = Math.round(((i + 1) / totalFiles) * 100);
            updateQuickProgress(progress, `Загрузка файла ${i + 1} из ${totalFiles}: ${file.name}`);
            
            const formData = new FormData();
            formData.append('files', file);
            if (description) {
                formData.append('description', description);
            }
            if (tags) {
                formData.append('tags', tags);
            }

            const response = await fetch('/api/files/upload', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (response.ok) {
                const result = await response.json();
                uploadedFiles.push(...result);
            } else {
                const error = await response.json();
                console.error(`Error uploading file ${file.name}:`, error);
            }
        }

        if (uploadedFiles.length === 0) {
            throw new Error('Не удалось загрузить ни одного файла');
        }

        return uploadedFiles;
    }

    async function analyzeQuickFiles(uploadedFiles, token) {
        const fileIds = uploadedFiles.map(file => file.id);
        
        const response = await fetch('/api/files/analyze', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ fileIds: fileIds })
        });

        if (response.ok) {
            return await response.json();
        } else {
            const error = await response.json();
            throw new Error(error.message || 'Ошибка анализа файлов');
        }
    }

    function showQuickProgress() {
        const progressDiv = document.getElementById('quickUploadProgress');
        if (progressDiv) {
            progressDiv.style.display = 'block';
        }
    }

    function updateQuickProgress(percentage, status) {
        const progressBar = document.querySelector('#quickUploadProgress .progress-bar');
        const statusText = document.getElementById('quickUploadStatus');
        
        if (progressBar) {
            progressBar.style.width = percentage + '%';
        }
        if (statusText) {
            statusText.textContent = status;
        }
    }

    function hideQuickProgress() {
        const progressDiv = document.getElementById('quickUploadProgress');
        if (progressDiv) {
            progressDiv.style.display = 'none';
        }
    }

    function showQuickResults(result) {
        const resultsDiv = document.getElementById('quickUploadResults');
        const contentDiv = document.getElementById('quickAnalysisContent');
        const messageDiv = document.getElementById('quickUploadMessage');
        
        if (resultsDiv) {
            resultsDiv.style.display = 'block';
        }
        
        if (contentDiv) {
            contentDiv.textContent = result.analysis || 'Анализ завершен успешно!';
        }
        
        if (messageDiv) {
            messageDiv.textContent = 'Файлы успешно загружены и проанализированы!';
        }

        hideQuickProgress();
        showQuickMessage('Файлы успешно загружены и проанализированы!', 'success');
        
        // Обновляем список файлов
        if (typeof refreshFiles === 'function') {
            refreshFiles();
        }
    }

    function showQuickMessage(message, type) {
        // Создаем временное уведомление
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type === 'success' ? 'success' : type === 'error' ? 'danger' : type === 'warning' ? 'warning' : 'info'} alert-dismissible fade show`;
        alertDiv.style.position = 'fixed';
        alertDiv.style.top = '20px';
        alertDiv.style.right = '20px';
        alertDiv.style.zIndex = '9999';
        alertDiv.style.minWidth = '300px';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(alertDiv);
        
        // Автоматически скрываем через 5 секунд
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }


    // Employee upload functions for modal
    async function employeeUploadAndAnalyze() {
        const fileInput = document.getElementById('employeeFileInput');
        const files = fileInput.files;
        const description = document.getElementById('employeeFileDescription').value;
        const tags = document.getElementById('employeeFileTags').value;

        if (files.length === 0) {
            showEmployeeMessage('Выберите файлы для загрузки', 'warning');
            return;
        }

        // Проверяем авторизацию
        const token = localStorage.getItem('token');
        if (!token) {
            showEmployeeMessage('Необходимо войти в систему для загрузки файлов', 'error');
            setTimeout(() => {
                const modal = bootstrap.Modal.getInstance(document.getElementById('employeeUploadModal'));
                modal.hide();
                showLoginModal();
            }, 2000);
            return;
        }

        try {
            // Показываем прогресс
            showEmployeeProgress();
            updateEmployeeProgress(0, 'Подготовка к загрузке...');

            // Валидация файлов
            const validationResult = validateQuickFiles(files);
            if (!validationResult.valid) {
                showEmployeeMessage(validationResult.message, 'error');
                hideEmployeeProgress();
                return;
            }

            // Загружаем файлы
            updateEmployeeProgress(20, 'Загрузка файлов...');
            const uploadedFiles = await uploadEmployeeFiles(files, description, tags, token);
            
            if (uploadedFiles.length === 0) {
                showEmployeeMessage('Ошибка загрузки файлов', 'error');
                hideEmployeeProgress();
                return;
            }

            // Сохраняем файлы в localStorage для персистентности
            const existingFiles = JSON.parse(localStorage.getItem('uploadedFiles') || '[]');
            const allFiles = [...existingFiles, ...uploadedFiles];
            localStorage.setItem('uploadedFiles', JSON.stringify(allFiles));

            // Анализируем файлы
            updateEmployeeProgress(50, 'Анализ файлов...');
            const analysisResult = await analyzeQuickFiles(uploadedFiles, token);
            
            // Показываем результаты
            updateEmployeeProgress(100, 'Анализ завершен!');
            showEmployeeResults(analysisResult);
            
        } catch (error) {
            console.error('Employee upload error:', error);
            showEmployeeMessage('Ошибка загрузки и анализа файлов', 'error');
            hideEmployeeProgress();
        }
    }

    async function uploadEmployeeFiles(files, description, tags, token) {
        const uploadedFiles = [];
        const totalFiles = files.length;
        
        // Загружаем файлы по одному для лучшего контроля
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const progress = Math.round(((i + 1) / totalFiles) * 100);
            updateEmployeeProgress(progress, `Загрузка файла ${i + 1} из ${totalFiles}: ${file.name}`);
            
            const formData = new FormData();
            formData.append('files', file);  // 'files' должен быть массивом, но backend принимает и одиночный файл
            if (description) {
                formData.append('description', description);
            }
            if (tags) {
                formData.append('tags', tags);
            }

            console.log(`Uploading file ${i + 1}: ${file.name}, size: ${file.size} bytes`);

            const response = await fetch('/api/files/upload', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            console.log(`Response status for ${file.name}:`, response.status);

            if (response.ok) {
                const result = await response.json();
                console.log(`Successfully uploaded ${file.name}:`, result);
                uploadedFiles.push(...result);
            } else {
                const errorText = await response.text();
                console.error(`Error uploading file ${file.name}:`, errorText);
                try {
                    const error = JSON.parse(errorText);
                    console.error(`Parsed error:`, error);
                } catch (e) {
                    console.error(`Raw error response:`, errorText);
                }
            }
        }

        if (uploadedFiles.length === 0) {
            throw new Error('Не удалось загрузить ни одного файла');
        }

        return uploadedFiles;
    }

    function showEmployeeProgress() {
        const progressDiv = document.getElementById('employeeUploadProgress');
        if (progressDiv) {
            progressDiv.style.display = 'block';
        }
    }

    function updateEmployeeProgress(percentage, status) {
        const progressBar = document.querySelector('#employeeUploadProgress .progress-bar');
        const statusText = document.getElementById('employeeUploadStatus');
        
        if (progressBar) {
            progressBar.style.width = percentage + '%';
        }
        if (statusText) {
            statusText.textContent = status;
        }
    }

    function hideEmployeeProgress() {
        const progressDiv = document.getElementById('employeeUploadProgress');
        if (progressDiv) {
            progressDiv.style.display = 'none';
        }
    }

    function showEmployeeResults(result) {
        const resultsDiv = document.getElementById('employeeUploadResults');
        const contentDiv = document.getElementById('employeeAnalysisContent');
        const messageDiv = document.getElementById('employeeUploadMessage');
        
        if (resultsDiv) {
            resultsDiv.style.display = 'block';
        }
        
        if (contentDiv) {
            contentDiv.textContent = result.analysis || 'Анализ завершен успешно!';
        }
        
        if (messageDiv) {
            messageDiv.textContent = 'Файлы успешно загружены и проанализированы!';
        }

        hideEmployeeProgress();
        showEmployeeMessage('Файлы успешно загружены и проанализированы!', 'success');
    }

    function showEmployeeMessage(message, type) {
        // Создаем временное уведомление
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type === 'success' ? 'success' : type === 'error' ? 'danger' : type === 'warning' ? 'warning' : 'info'} alert-dismissible fade show`;
        alertDiv.style.position = 'fixed';
        alertDiv.style.top = '20px';
        alertDiv.style.right = '20px';
        alertDiv.style.zIndex = '9999';
        alertDiv.style.minWidth = '300px';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(alertDiv);
        
        // Автоматически скрываем через 5 секунд
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }

    function goToEmployeeProfile() {
        window.location.href = '/profile';
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
        <button class="btn btn-outline-evalyze" id="logoutBtn">
            <i class="fas fa-sign-out-alt me-2"></i>Выйти
        </button>
    `;
    
    authContainer.appendChild(userInfo);
    
    // Добавляем обработчик события для кнопки выхода
    setTimeout(() => {
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', function(e) {
                e.preventDefault();
                logout();
            });
            console.log('✅ Logout button handler attached');
        }
    }, 100);
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
    // Очищаем все данные из localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
    
    // Перенаправляем на главную страницу
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
