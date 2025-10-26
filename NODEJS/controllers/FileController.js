const fileUploadService = require('../services/FileUploadService');
const { authenticateToken } = require('../config/jwt');

class FileController {
  // Upload files
  async uploadFiles(req, res) {
    try {
      const userId = req.user ? req.user.userId : 'mock-user-id';
      const files = req.files;

      if (!files || files.length === 0) {
        return res.status(400).json({
          error: 'No files uploaded'
        });
      }

      // В мок-режиме просто возвращаем успех
      const uploadedFiles = files.map(file => ({
        fileId: 'mock-file-' + Date.now(),
        fileName: file.originalname,
        fileSize: file.size,
        fileType: file.mimetype,
        uploadDate: new Date().toISOString(),
        userId: userId
      }));

      res.json({
        message: 'Files uploaded successfully',
        files: uploadedFiles
      });
    } catch (error) {
      console.error('Upload files error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Get user files
  async getUserFiles(req, res) {
    try {
      const userId = req.user ? req.user.userId : 'mock-user-id';
      
      // В мок-режиме возвращаем пустой список
      const files = [];
      res.json({ files });
    } catch (error) {
      console.error('Get user files error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Delete file
  async deleteFile(req, res) {
    try {
      const userId = req.user.userId;
      const { fileId } = req.params;

      const result = await fileUploadService.deleteFile(fileId, userId);
      res.json(result);
    } catch (error) {
      console.error('Delete file error:', error);
      res.status(400).json({
        error: error.message
      });
    }
  }

  // Get file info
  async getFileInfo(req, res) {
    try {
      const userId = req.user.userId;
      const { fileId } = req.params;

      const file = await fileUploadService.getFileInfo(fileId, userId);
      res.json({ file });
    } catch (error) {
      console.error('Get file info error:', error);
      res.status(404).json({
        error: error.message
      });
    }
  }

  // Get user storage stats
  async getUserStorageStats(req, res) {
    try {
      const userId = req.user.userId;
      const stats = await fileUploadService.getUserStorageStats(userId);
      res.json({ stats });
    } catch (error) {
      console.error('Get user storage stats error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }

  // Clean up old files (admin only)
  async cleanupOldFiles(req, res) {
    try {
      const { daysOld } = req.query;
      const result = await fileUploadService.cleanupOldFiles(parseInt(daysOld) || 30);
      res.json(result);
    } catch (error) {
      console.error('Cleanup old files error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }
}

module.exports = new FileController();
