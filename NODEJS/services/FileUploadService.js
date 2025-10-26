const multer = require('multer');
const path = require('path');
const fs = require('fs').promises;
const { v4: uuidv4 } = require('uuid');
const fileUploadRepository = require('../repositories/FileUploadRepository');
const FileUpload = require('../models/FileUpload');

class FileUploadService {
  constructor() {
    this.uploadDir = process.env.UPLOAD_DIR || 'uploads';
    this.maxFileSize = parseInt(process.env.MAX_FILE_SIZE) || 10 * 1024 * 1024; // 10MB default
    
    // Ensure upload directory exists
    this.ensureUploadDir();
  }

  // Ensure upload directory exists
  async ensureUploadDir() {
    try {
      await fs.access(this.uploadDir);
    } catch (error) {
      await fs.mkdir(this.uploadDir, { recursive: true });
    }
  }

  // Configure multer storage
  getStorage() {
    return multer.diskStorage({
      destination: async (req, file, cb) => {
        const userId = req.user?.userId || 'anonymous';
        const userDir = path.join(this.uploadDir, userId);
        
        try {
          await fs.mkdir(userDir, { recursive: true });
          cb(null, userDir);
        } catch (error) {
          cb(error);
        }
      },
      filename: (req, file, cb) => {
        const timestamp = Date.now();
        const randomId = uuidv4().substring(0, 8);
        const extension = path.extname(file.originalname);
        const filename = `${timestamp}_${randomId}${extension}`;
        cb(null, filename);
      }
    });
  }

  // Configure multer
  getMulterConfig() {
    return multer({
      storage: this.getStorage(),
      limits: {
        fileSize: this.maxFileSize,
        files: 10 // Maximum 10 files per request
      },
      fileFilter: (req, file, cb) => {
        // Allow common document types
        const allowedTypes = [
          'application/pdf',
          'application/msword',
          'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
          'text/plain',
          'image/jpeg',
          'image/png',
          'image/gif'
        ];

        if (allowedTypes.includes(file.mimetype)) {
          cb(null, true);
        } else {
          cb(new Error(`File type ${file.mimetype} not allowed`), false);
        }
      }
    });
  }

  // Upload files
  async uploadFiles(userId, files) {
    try {
      const uploadedFiles = [];

      for (const file of files) {
        const fileUpload = new FileUpload({
          userId,
          fileName: file.filename,
          originalName: file.originalname,
          fileSize: file.size,
          mimeType: file.mimetype,
          filePath: file.path
        });

        const savedFile = await fileUploadRepository.save(fileUpload);
        uploadedFiles.push(savedFile.toDto());
      }

      return uploadedFiles;
    } catch (error) {
      console.error('Error uploading files:', error);
      throw error;
    }
  }

  // Get user files
  async getUserFiles(userId) {
    try {
      const files = await fileUploadRepository.findByUserId(userId);
      return files.map(file => file.toDto());
    } catch (error) {
      console.error('Error getting user files:', error);
      throw error;
    }
  }

  // Delete file
  async deleteFile(fileId, userId) {
    try {
      const file = await fileUploadRepository.findById(fileId);
      if (!file) {
        throw new Error('File not found');
      }

      if (file.userId !== userId) {
        throw new Error('Unauthorized to delete this file');
      }

      // Delete physical file
      try {
        await fs.unlink(file.filePath);
      } catch (error) {
        console.warn('Could not delete physical file:', error.message);
      }

      // Delete database record
      await fileUploadRepository.deleteById(fileId);

      return { message: 'File deleted successfully' };
    } catch (error) {
      console.error('Error deleting file:', error);
      throw error;
    }
  }

  // Get file info
  async getFileInfo(fileId, userId) {
    try {
      const file = await fileUploadRepository.findById(fileId);
      if (!file) {
        throw new Error('File not found');
      }

      if (file.userId !== userId) {
        throw new Error('Unauthorized to access this file');
      }

      return file.toDto();
    } catch (error) {
      console.error('Error getting file info:', error);
      throw error;
    }
  }

  // Get user storage stats
  async getUserStorageStats(userId) {
    try {
      const fileCount = await fileUploadRepository.countByUserId(userId);
      const totalSize = await fileUploadRepository.getTotalSizeByUserId(userId);

      return {
        fileCount,
        totalSize,
        totalSizeMB: Math.round(totalSize / (1024 * 1024) * 100) / 100
      };
    } catch (error) {
      console.error('Error getting user storage stats:', error);
      throw error;
    }
  }

  // Clean up old files
  async cleanupOldFiles(daysOld = 30) {
    try {
      const cutoffDate = new Date();
      cutoffDate.setDate(cutoffDate.getDate() - daysOld);

      const allFiles = await fileUploadRepository.findAll();
      const oldFiles = allFiles.filter(file => 
        new Date(file.uploadedAt) < cutoffDate
      );

      let deletedCount = 0;
      for (const file of oldFiles) {
        try {
          await fs.unlink(file.filePath);
          await fileUploadRepository.deleteById(file.id);
          deletedCount++;
        } catch (error) {
          console.warn(`Could not delete old file ${file.id}:`, error.message);
        }
      }

      return {
        message: `Cleaned up ${deletedCount} old files`,
        deletedCount,
        totalOldFiles: oldFiles.length
      };
    } catch (error) {
      console.error('Error cleaning up old files:', error);
      throw error;
    }
  }
}

module.exports = new FileUploadService();
