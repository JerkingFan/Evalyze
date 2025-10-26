// File Upload Management
class FileUploadManager {
    constructor() {
        this.uploadedFiles = [];
        this.loadFilesFromStorage();
        this.initializeEventListeners();
        this.loadUserFiles();
    }

    initializeEventListeners() {
        // Обработчик для изменения файлов
        const fileInput = document.getElementById('fileInput');
        if (fileInput) {
            fileInput.addEventListener('change', async (e) => {
                const files = e.target.files;
                console.log('Files selected:', files.length);
                this.handleFileSelection(files);
                
                // Сохраняем выбранные файлы в IndexedDB
                if (files.length > 0 && typeof fileStorageManager !== 'undefined') {
                    try {
                        await fileStorageManager.saveFiles('fileInput', files);
                        console.log('✅ Files saved to IndexedDB:', files.length);
                    } catch (error) {
                        console.error('❌ Error saving files to IndexedDB:', error);
                    }
                }
            });
            
            // Восстанавливаем файлы при загрузке страницы
            setTimeout(() => {
                this.restoreFiles('fileInput', fileInput);
            }, 500);
        }

        // Обработчик для drag & drop
        if (fileInput) {
            fileInput.addEventListener('dragover', (e) => {
                e.preventDefault();
            fileInput.classList.add('drag-over');
        });

        fileInput.addEventListener('dragleave', (e) => {
            e.preventDefault();
            fileInput.classList.remove('drag-over');
        });

        fileInput.addEventListener('drop', (e) => {
            e.preventDefault();
            fileInput.classList.remove('drag-over');
            this.handleFileSelection(e.dataTransfer.files);
        });
    }

    handleFileSelection(files) {
        const fileList = Array.from(files);
        console.log('Selected files:', fileList);
        
        // Показываем превью выбранных файлов
        this.showFilePreview(fileList);
    }

    showFilePreview(files) {
        const previewContainer = document.createElement('div');
        previewContainer.className = 'file-preview-container mt-3';
        previewContainer.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h6 class="mb-0">Выбранные файлы (${files.length})</h6>
                <button class="btn btn-sm btn-outline-secondary" onclick="fileUploadManager.clearAllPreviews()">
                    <i class="fas fa-trash me-1"></i>Очистить все
                </button>
            </div>
        `;

        // Группируем файлы по статусу
        const validFiles = [];
        const invalidFiles = [];

        files.forEach((file, index) => {
            const validation = this.validateSingleFile(file);
            if (validation.valid) {
                validFiles.push({ file, index });
            } else {
                invalidFiles.push({ file, index, error: validation.message });
            }
        });

        // Показываем валидные файлы
        if (validFiles.length > 0) {
            const validSection = document.createElement('div');
            validSection.innerHTML = `<h6 class="text-success mb-2"><i class="fas fa-check-circle me-1"></i>Готовы к загрузке (${validFiles.length})</h6>`;
            
            validFiles.forEach(({ file, index }) => {
                const fileItem = this.createFilePreviewItem(file, index, true);
                validSection.appendChild(fileItem);
            });
            
            previewContainer.appendChild(validSection);
        }

        // Показываем невалидные файлы
        if (invalidFiles.length > 0) {
            const invalidSection = document.createElement('div');
            invalidSection.innerHTML = `<h6 class="text-danger mb-2"><i class="fas fa-exclamation-triangle me-1"></i>Требуют внимания (${invalidFiles.length})</h6>`;
            
            invalidFiles.forEach(({ file, index, error }) => {
                const fileItem = this.createFilePreviewItem(file, index, false, error);
                invalidSection.appendChild(fileItem);
            });
            
            previewContainer.appendChild(invalidSection);
        }

        // Удаляем старый превью
        const oldPreview = document.querySelector('.file-preview-container');
        if (oldPreview) {
            oldPreview.remove();
        }

        // Добавляем новый превью
        document.getElementById('fileInput').parentNode.appendChild(previewContainer);
    }

    createFilePreviewItem(file, index, isValid, error = null) {
        const fileItem = document.createElement('div');
        fileItem.className = `file-preview-item d-flex align-items-center mb-2 p-3 rounded ${isValid ? 'bg-light' : 'bg-danger bg-opacity-10'}`;
        
        const fileIcon = this.getFileIcon(file.type);
        
        fileItem.innerHTML = `
            <div class="file-icon me-3">
                <i class="${fileIcon} fa-2x"></i>
            </div>
            <div class="file-info flex-grow-1">
                <div class="file-name fw-bold">${file.name}</div>
                <div class="file-details text-muted small">
                    ${this.formatFileSize(file.size)} • ${file.type}
                </div>
                ${!isValid ? `<div class="file-error text-danger small">${error}</div>` : ''}
            </div>
            <div class="file-actions">
                <button class="btn btn-sm btn-outline-danger" onclick="fileUploadManager.removeFilePreview(${index})">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;
        
        if (!isValid) {
            fileItem.style.border = '2px solid #dc3545';
        }
        
        return fileItem;
    }

    clearAllPreviews() {
        const fileInput = document.getElementById('fileInput');
        if (fileInput) {
            fileInput.value = '';
        }
        
        const previewContainer = document.querySelector('.file-preview-container');
        if (previewContainer) {
            previewContainer.remove();
        }
        
        // Очищаем IndexedDB
        if (typeof fileStorageManager !== 'undefined') {
            fileStorageManager.clearFiles('fileInput').catch(err => console.error('Error clearing IndexedDB:', err));
        }
    }
    
    async restoreFiles(inputId, fileInputElement) {
        console.log('🔄 Attempting to restore files for:', inputId);
        
        if (typeof fileStorageManager === 'undefined') {
            console.log('❌ fileStorageManager is undefined');
            return;
        }
        
        try {
            const files = await fileStorageManager.getFiles(inputId);
            console.log('📦 Files retrieved from IndexedDB:', files.length);
            
            if (files.length > 0) {
                // Создаем DataTransfer для установки файлов в input
                const dataTransfer = new DataTransfer();
                files.forEach(file => {
                    console.log('  - Restoring file:', file.name, file.size, 'bytes');
                    dataTransfer.items.add(file);
                });
                fileInputElement.files = dataTransfer.files;
                
                // Показываем превью восстановленных файлов
                this.handleFileSelection(Array.from(files));
                
                this.showMessage(`✅ Восстановлено ${files.length} выбранных файлов`, 'success');
                console.log(`✅ Successfully restored ${files.length} files from IndexedDB`);
            } else {
                console.log('ℹ️ No files to restore');
            }
        } catch (error) {
            console.error('❌ Error restoring files:', error);
        }
    }
    
    getFileIcon(mimeType) {
        if (mimeType.startsWith('image/')) {
            return 'fas fa-image text-primary';
        } else if (mimeType === 'application/pdf') {
            return 'fas fa-file-pdf text-danger';
        } else if (mimeType.includes('word') || mimeType.includes('document')) {
            return 'fas fa-file-word text-primary';
        } else if (mimeType.startsWith('text/')) {
            return 'fas fa-file-alt text-info';
        } else {
            return 'fas fa-file text-secondary';
        }
    }
    
    validateSingleFile(file) {
        const maxFileSize = 50 * 1024 * 1024; // 50MB
        const allowedTypes = [
            'image/jpeg', 'image/png', 'image/gif', 'image/webp',
            'application/pdf', 'text/plain', 'text/csv',
            'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        ];
        
        if (file.size > maxFileSize) {
            return {
                valid: false,
                message: 'Файл слишком большой (макс. 50MB)'
            };
        }
        
        if (!allowedTypes.includes(file.type)) {
            return {
                valid: false,
                message: 'Неподдерживаемый тип файла'
            };
        }
        
        return { valid: true };
    }

    removeFilePreview(index) {
        // Удаляем файл из превью (в реальном приложении нужно обновить input)
        const previewItems = document.querySelectorAll('.file-preview-item');
        if (previewItems[index]) {
            previewItems[index].remove();
        }
    }

    async uploadFiles() {
        const fileInput = document.getElementById('fileInput');
        const files = fileInput.files;
        const description = document.getElementById('fileDescription').value;
        const tags = document.getElementById('fileTags').value;

        if (files.length === 0) {
            this.showMessage('Выберите файлы для загрузки', 'warning');
            return;
        }

        // Валидация файлов
        const validationResult = this.validateFiles(files);
        if (!validationResult.valid) {
            this.showMessage(validationResult.message, 'error');
            return;
        }

        try {
            // Показываем прогресс бар
            this.showProgressBar();
            
            // Загружаем файлы по одному для лучшего контроля
            const uploadedFiles = [];
            const totalFiles = files.length;
            
            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                const progress = Math.round(((i + 1) / totalFiles) * 100);
                this.updateProgressBar(progress, `Загрузка файла ${i + 1} из ${totalFiles}: ${file.name}`);
                
                const formData = new FormData();
                formData.append('files', file);
                if (description) {
                    formData.append('description', description);
                }
                if (tags) {
                    formData.append('tags', tags);
                }

                const token = localStorage.getItem('token');
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

            if (uploadedFiles.length > 0) {
                this.uploadedFiles = [...this.uploadedFiles, ...uploadedFiles];
                this.saveFilesToStorage();
                this.showMessage(`Успешно загружено ${uploadedFiles.length} из ${totalFiles} файлов`, 'success');
                this.clearForm();
                this.loadUserFiles();
            } else {
                this.showMessage('Не удалось загрузить ни одного файла', 'error');
            }
        } catch (error) {
            console.error('Upload error:', error);
            this.showMessage('Ошибка загрузки файлов', 'error');
        } finally {
            this.hideProgressBar();
        }
    }

    async loadUserFiles() {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                console.log('No token found, skipping server file load');
                return;
            }

            const response = await fetch('/api/files', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const files = await response.json();
                this.uploadedFiles = files;
                this.saveFilesToStorage();
                this.displayFiles(files);
                this.showMessage(`Загружено ${files.length} файлов с сервера`, 'success');
            } else {
                console.error('Failed to load files from server');
                // Показываем файлы из localStorage если сервер недоступен
                if (this.uploadedFiles.length > 0) {
                    this.displayFiles(this.uploadedFiles);
                    this.showMessage('Показаны файлы из локального хранилища (сервер недоступен)', 'warning');
                }
            }
        } catch (error) {
            console.error('Error loading files:', error);
            // Показываем файлы из localStorage при ошибке
            if (this.uploadedFiles.length > 0) {
                this.displayFiles(this.uploadedFiles);
                this.showMessage('Показаны файлы из локального хранилища', 'info');
            }
        }
    }

    displayFiles(files) {
        const filesList = document.getElementById('uploadedFilesList');
        const filesSection = document.getElementById('uploadedFilesSection');
        const analyzeBtn = document.getElementById('analyzeBtn');
        
        if (files.length === 0) {
            filesSection.style.display = 'none';
            if (analyzeBtn) analyzeBtn.style.display = 'none';
            return;
        }

        filesSection.style.display = 'block';
        if (analyzeBtn) analyzeBtn.style.display = 'inline-block';
        filesList.innerHTML = '';

        files.forEach(file => {
            const fileItem = document.createElement('div');
            fileItem.className = 'file-item d-flex align-items-center justify-content-between p-3 mb-3 bg-light rounded shadow-sm';
            
            // Определяем иконку по типу файла
            const fileIcon = this.getFileIcon(file.mimeType);
            
            fileItem.innerHTML = `
                <div class="d-flex align-items-center">
                    <div class="file-icon me-3">
                        <i class="${fileIcon} fa-2x"></i>
                    </div>
                    <div class="file-info">
                        <h6 class="mb-1 fw-bold">${file.originalFileName}</h6>
                        <div class="file-meta text-muted small mb-2">
                            <i class="fas fa-weight-hanging me-1"></i>${this.formatFileSize(file.fileSize)}
                            <span class="mx-2">•</span>
                            <i class="fas fa-calendar me-1"></i>${new Date(file.uploadedAt).toLocaleDateString('ru-RU')}
                            <span class="mx-2">•</span>
                            <i class="fas fa-tag me-1"></i>${file.mimeType}
                        </div>
                        ${file.description ? `
                            <div class="file-description mb-2">
                                <small class="text-dark">
                                    <i class="fas fa-align-left me-1"></i>${file.description}
                                </small>
                            </div>
                        ` : ''}
                        ${file.tags ? `
                            <div class="file-tags">
                                <small class="text-primary">
                                    <i class="fas fa-tags me-1"></i>${file.tags}
                                </small>
                            </div>
                        ` : ''}
                    </div>
                </div>
                <div class="file-actions d-flex gap-2">
                    <button class="btn btn-sm btn-outline-primary" onclick="fileUploadManager.downloadFile('${file.id}')" title="Скачать">
                        <i class="fas fa-download"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="fileUploadManager.deleteFile('${file.id}')" title="Удалить">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            `;
            filesList.appendChild(fileItem);
        });
    }

    async downloadFile(fileId) {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`/api/files/download/${fileId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = response.headers.get('Content-Disposition')?.split('filename=')[1]?.replace(/"/g, '') || 'file';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            } else {
                this.showMessage('Ошибка скачивания файла', 'error');
            }
        } catch (error) {
            console.error('Download error:', error);
            this.showMessage('Ошибка скачивания файла', 'error');
        }
    }

    async deleteFile(fileId) {
        if (!confirm('Вы уверены, что хотите удалить этот файл?')) {
            return;
        }

        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`/api/files/${fileId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                this.showMessage('Файл удален', 'success');
                this.loadUserFiles(); // Обновляем список
            } else {
                this.showMessage('Ошибка удаления файла', 'error');
            }
        } catch (error) {
            console.error('Delete error:', error);
            this.showMessage('Ошибка удаления файла', 'error');
        }
    }

    clearForm() {
        document.getElementById('fileInput').value = '';
        document.getElementById('fileDescription').value = '';
        document.getElementById('fileTags').value = '';
        
        // Удаляем превью
        const preview = document.querySelector('.file-preview-container');
        if (preview) {
            preview.remove();
        }
    }

    saveFilesToStorage() {
        localStorage.setItem('uploadedFiles', JSON.stringify(this.uploadedFiles));
    }

    loadFilesFromStorage() {
        const stored = localStorage.getItem('uploadedFiles');
        if (stored) {
            try {
                this.uploadedFiles = JSON.parse(stored);
                if (this.uploadedFiles.length > 0) {
                    this.showMessage(`Восстановлено ${this.uploadedFiles.length} файлов из локального хранилища`, 'info');
                }
            } catch (error) {
                console.error('Error parsing stored files:', error);
                this.uploadedFiles = [];
            }
        }
    }

    validateFiles(files) {
        const maxFileSize = 50 * 1024 * 1024; // 50MB
        const allowedTypes = [
            'image/jpeg', 'image/png', 'image/gif', 'image/webp',
            'application/pdf', 'text/plain', 'text/csv',
            'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        ];
        
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            
            // Проверка размера
            if (file.size > maxFileSize) {
                return {
                    valid: false,
                    message: `Файл "${file.name}" слишком большой. Максимальный размер: 50MB`
                };
            }
            
            // Проверка типа
            if (!allowedTypes.includes(file.type)) {
                return {
                    valid: false,
                    message: `Файл "${file.name}" имеет неподдерживаемый тип. Разрешены: изображения, PDF, текстовые файлы, Word документы`
                };
            }
        }
        
        return { valid: true };
    }
    
    showProgressBar() {
        // Создаем прогресс бар
        const progressContainer = document.createElement('div');
        progressContainer.id = 'uploadProgressContainer';
        progressContainer.className = 'upload-progress-container';
        progressContainer.innerHTML = `
            <div class="upload-progress">
                <div class="progress-bar">
                    <div class="progress-fill"></div>
                </div>
                <div class="progress-text">Загрузка файлов...</div>
            </div>
        `;
        
        // Добавляем стили
        const style = document.createElement('style');
        style.textContent = `
            .upload-progress-container {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 9999;
            }
            .upload-progress {
                background: white;
                padding: 20px;
                border-radius: 8px;
                min-width: 300px;
                text-align: center;
            }
            .progress-bar {
                width: 100%;
                height: 20px;
                background: #f0f0f0;
                border-radius: 10px;
                overflow: hidden;
                margin-bottom: 10px;
            }
            .progress-fill {
                height: 100%;
                background: linear-gradient(90deg, #007bff, #0056b3);
                width: 0%;
                transition: width 0.3s ease;
                animation: progress-animation 2s infinite;
            }
            @keyframes progress-animation {
                0% { width: 0%; }
                50% { width: 70%; }
                100% { width: 100%; }
            }
            .progress-text {
                font-weight: 500;
                color: #333;
            }
        `;
        document.head.appendChild(style);
        document.body.appendChild(progressContainer);
    }
    
    updateProgressBar(percentage, status) {
        const progressBar = document.querySelector('#uploadProgressContainer .progress-fill');
        const statusText = document.querySelector('#uploadProgressContainer .progress-text');
        
        if (progressBar) {
            progressBar.style.width = percentage + '%';
        }
        if (statusText) {
            statusText.textContent = status;
        }
    }
    
    hideProgressBar() {
        const progressContainer = document.getElementById('uploadProgressContainer');
        if (progressContainer) {
            progressContainer.remove();
        }
    }
    
    async sendFilesForAnalysis() {
        if (this.uploadedFiles.length === 0) {
            this.showMessage('Нет файлов для анализа', 'warning');
            return;
        }

        try {
            // Показываем секцию анализа
            const analysisSection = document.getElementById('fileAnalysisSection');
            if (analysisSection) {
                analysisSection.style.display = 'block';
            }

            // Показываем прогресс
            this.updateAnalysisProgress(0, 'Подготовка к анализу...');

            // Собираем ID файлов для анализа
            const fileIds = this.uploadedFiles.map(file => file.id);

            const token = localStorage.getItem('token');
            const response = await fetch('/api/files/analyze', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ fileIds: fileIds })
            });

            if (response.ok) {
                this.updateAnalysisProgress(50, 'Анализ файлов...');
                
                // Симулируем процесс анализа
                await this.simulateAnalysis();
                
                const result = await response.json();
                this.showAnalysisResults(result);
            } else {
                const error = await response.json();
                this.showMessage(`Ошибка анализа: ${error.message || 'Неизвестная ошибка'}`, 'error');
            }
        } catch (error) {
            console.error('Analysis error:', error);
            this.showMessage('Ошибка анализа файлов', 'error');
        }
    }

    updateAnalysisProgress(percentage, status) {
        const progressBar = document.querySelector('#analysisProgress .progress-bar');
        const statusText = document.getElementById('analysisStatus');
        
        if (progressBar) {
            progressBar.style.width = percentage + '%';
        }
        if (statusText) {
            statusText.textContent = status;
        }
    }

    async simulateAnalysis() {
        const steps = [
            { progress: 20, status: 'Извлечение текста из документов...' },
            { progress: 40, status: 'Анализ изображений...' },
            { progress: 60, status: 'Обработка PDF файлов...' },
            { progress: 80, status: 'Генерация профиля...' },
            { progress: 100, status: 'Анализ завершен!' }
        ];

        for (const step of steps) {
            this.updateAnalysisProgress(step.progress, step.status);
            await new Promise(resolve => setTimeout(resolve, 1000));
        }
    }

    showAnalysisResults(result) {
        const resultsDiv = document.getElementById('analysisResults');
        const contentDiv = document.getElementById('analysisContent');
        
        if (resultsDiv) {
            resultsDiv.style.display = 'block';
        }
        
        if (contentDiv) {
            contentDiv.textContent = result.analysis || 'Анализ завершен успешно!';
        }

        // Скрываем кнопку анализа
        const analyzeBtn = document.getElementById('analyzeBtn');
        if (analyzeBtn) {
            analyzeBtn.style.display = 'none';
        }

        this.showMessage('Анализ файлов завершен!', 'success');
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    showMessage(message, type) {
        const alertClass = type === 'success' ? 'alert-success' : 
                          type === 'error' ? 'alert-danger' : 
                          type === 'warning' ? 'alert-warning' : 'alert-info';
        
        const alertHtml = `
            <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        
        // Добавляем сообщение в начало страницы
        const container = document.querySelector('.container');
        if (container) {
            container.insertAdjacentHTML('afterbegin', alertHtml);
        }
        
        // Автоматически скрываем через 5 секунд
        setTimeout(() => {
            const alert = document.querySelector('.alert');
            if (alert) {
                alert.remove();
            }
        }, 5000);
    }
}

// Глобальные функции для HTML
function uploadFiles() {
    fileUploadManager.uploadFiles();
}

function refreshFiles() {
    fileUploadManager.loadUserFiles();
}

function sendFilesForAnalysis() {
    fileUploadManager.sendFilesForAnalysis();
}

// Инициализация при загрузке страницы
let fileUploadManager;
document.addEventListener('DOMContentLoaded', function() {
    fileUploadManager = new FileUploadManager();
    
    // Загружаем файлы при загрузке страницы
    if (localStorage.getItem('token')) {
        fileUploadManager.loadUserFiles();
    }
});
