// IndexedDB для хранения выбранных файлов
class FileStorageManager {
    constructor() {
        this.dbName = 'EvalyzeFileStorage';
        this.dbVersion = 1;
        this.storeName = 'selectedFiles';
        this.db = null;
        this.initDB();
    }

    async initDB() {
        return new Promise((resolve, reject) => {
            const request = indexedDB.open(this.dbName, this.dbVersion);

            request.onerror = () => {
                console.error('IndexedDB error:', request.error);
                reject(request.error);
            };

            request.onsuccess = () => {
                this.db = request.result;
                console.log('IndexedDB initialized successfully');
                resolve(this.db);
            };

            request.onupgradeneeded = (event) => {
                const db = event.target.result;
                
                // Создаем хранилище для файлов
                if (!db.objectStoreNames.contains(this.storeName)) {
                    const objectStore = db.createObjectStore(this.storeName, { keyPath: 'id', autoIncrement: true });
                    objectStore.createIndex('inputId', 'inputId', { unique: false });
                    objectStore.createIndex('timestamp', 'timestamp', { unique: false });
                }
            };
        });
    }

    async saveFiles(inputId, files) {
        console.log('💾 Saving files to IndexedDB:', inputId, files.length);
        
        if (!this.db) {
            console.log('🔄 Initializing DB...');
            await this.initDB();
        }

        const transaction = this.db.transaction([this.storeName], 'readwrite');
        const objectStore = transaction.objectStore(this.storeName);

        // Очищаем старые файлы для этого input
        await this.clearFiles(inputId);

        // Сохраняем новые файлы
        const savePromises = [];
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            console.log(`  📄 Saving file ${i + 1}/${files.length}:`, file.name, file.size, 'bytes');
            
            // Читаем файл как ArrayBuffer
            const arrayBuffer = await this.fileToArrayBuffer(file);
            
            const fileData = {
                inputId: inputId,
                name: file.name,
                type: file.type,
                size: file.size,
                lastModified: file.lastModified,
                data: arrayBuffer,
                timestamp: Date.now()
            };

            const request = objectStore.add(fileData);
            savePromises.push(new Promise((resolve, reject) => {
                request.onsuccess = () => {
                    console.log(`    ✅ Saved: ${file.name}`);
                    resolve(request.result);
                };
                request.onerror = () => {
                    console.error(`    ❌ Failed to save: ${file.name}`, request.error);
                    reject(request.error);
                };
            }));
        }

        const results = await Promise.all(savePromises);
        console.log('✅ All files saved successfully:', results.length);
        return results;
    }

    async getFiles(inputId) {
        console.log('📂 Getting files from IndexedDB:', inputId);
        
        if (!this.db) {
            console.log('🔄 Initializing DB...');
            await this.initDB();
        }

        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.storeName], 'readonly');
            const objectStore = transaction.objectStore(this.storeName);
            const index = objectStore.index('inputId');
            const request = index.getAll(inputId);

            request.onsuccess = () => {
                const fileDataArray = request.result;
                console.log('📦 Found files in IndexedDB:', fileDataArray.length);
                
                // Конвертируем обратно в File объекты
                const files = fileDataArray.map(fileData => {
                    console.log(`  📄 Restoring: ${fileData.name} (${fileData.size} bytes)`);
                    return new File(
                        [fileData.data],
                        fileData.name,
                        {
                            type: fileData.type,
                            lastModified: fileData.lastModified
                        }
                    );
                });

                console.log('✅ Files converted successfully:', files.length);
                resolve(files);
            };

            request.onerror = () => {
                console.error('❌ Error getting files:', request.error);
                reject(request.error);
            };
        });
    }

    async clearFiles(inputId) {
        if (!this.db) {
            await this.initDB();
        }

        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.storeName], 'readwrite');
            const objectStore = transaction.objectStore(this.storeName);
            const index = objectStore.index('inputId');
            const request = index.openCursor(inputId);

            request.onsuccess = (event) => {
                const cursor = event.target.result;
                if (cursor) {
                    objectStore.delete(cursor.primaryKey);
                    cursor.continue();
                } else {
                    resolve();
                }
            };

            request.onerror = () => {
                console.error('Error clearing files:', request.error);
                reject(request.error);
            };
        });
    }

    async clearAllFiles() {
        if (!this.db) {
            await this.initDB();
        }

        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.storeName], 'readwrite');
            const objectStore = transaction.objectStore(this.storeName);
            const request = objectStore.clear();

            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    fileToArrayBuffer(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = () => reject(reader.error);
            reader.readAsArrayBuffer(file);
        });
    }

    // Очистка старых файлов (старше 7 дней)
    async cleanupOldFiles() {
        if (!this.db) {
            await this.initDB();
        }

        const sevenDaysAgo = Date.now() - (7 * 24 * 60 * 60 * 1000);

        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.storeName], 'readwrite');
            const objectStore = transaction.objectStore(this.storeName);
            const index = objectStore.index('timestamp');
            const request = index.openCursor();

            request.onsuccess = (event) => {
                const cursor = event.target.result;
                if (cursor) {
                    if (cursor.value.timestamp < sevenDaysAgo) {
                        objectStore.delete(cursor.primaryKey);
                    }
                    cursor.continue();
                } else {
                    resolve();
                }
            };

            request.onerror = () => {
                console.error('Error cleaning up old files:', request.error);
                reject(request.error);
            };
        });
    }
}

// Глобальный экземпляр
const fileStorageManager = new FileStorageManager();

// Очистка старых файлов при загрузке
fileStorageManager.cleanupOldFiles().catch(err => console.error('Cleanup error:', err));

