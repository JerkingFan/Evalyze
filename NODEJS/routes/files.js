const express = require('express');
const router = express.Router();
const fileController = require('../controllers/FileController');
const fileUploadService = require('../services/FileUploadService');
const { authenticateToken } = require('../config/jwt');

// Configure multer for file uploads
const upload = fileUploadService.getMulterConfig();

// Protected routes
router.post('/upload', upload.array('files', 10), fileController.uploadFiles);
router.get('/my-files', authenticateToken, fileController.getUserFiles);
router.get('/storage-stats', authenticateToken, fileController.getUserStorageStats);
router.get('/:fileId', authenticateToken, fileController.getFileInfo);
router.delete('/:fileId', authenticateToken, fileController.deleteFile);

// Admin routes
router.post('/cleanup', authenticateToken, fileController.cleanupOldFiles);

module.exports = router;
