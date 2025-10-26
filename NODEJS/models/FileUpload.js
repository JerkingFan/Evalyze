const { v4: uuidv4 } = require('uuid');

class FileUpload {
  constructor(data = {}) {
    this.id = data.id || null;
    this.userId = data.user_id || data.userId || null;
    this.fileName = data.file_name || data.fileName || '';
    this.originalName = data.original_name || data.originalName || '';
    this.fileSize = data.file_size || data.fileSize || 0;
    this.mimeType = data.mime_type || data.mimeType || '';
    this.filePath = data.file_path || data.filePath || '';
    this.uploadedAt = data.uploaded_at || data.uploadedAt || new Date().toISOString();
  }

  // Convert to Supabase format
  toSupabase() {
    return {
      user_id: this.userId,
      file_name: this.fileName,
      original_name: this.originalName,
      file_size: this.fileSize,
      mime_type: this.mimeType,
      file_path: this.filePath,
      uploaded_at: this.uploadedAt
    };
  }

  // Create from Supabase data
  static fromSupabase(data) {
    const fileUpload = new FileUpload();
    fileUpload.id = data.id;
    fileUpload.userId = data.user_id;
    fileUpload.fileName = data.file_name;
    fileUpload.originalName = data.original_name;
    fileUpload.fileSize = data.file_size;
    fileUpload.mimeType = data.mime_type;
    fileUpload.filePath = data.file_path;
    fileUpload.uploadedAt = data.uploaded_at;
    return fileUpload;
  }

  // Generate unique file name
  generateFileName(originalName) {
    const timestamp = Date.now();
    const randomId = uuidv4().substring(0, 8);
    const extension = originalName.split('.').pop();
    this.fileName = `${timestamp}_${randomId}.${extension}`;
    return this.fileName;
  }

  // Convert to DTO
  toDto() {
    return {
      id: this.id,
      userId: this.userId,
      fileName: this.fileName,
      originalName: this.originalName,
      fileSize: this.fileSize,
      mimeType: this.mimeType,
      filePath: this.filePath,
      uploadedAt: this.uploadedAt
    };
  }
}

module.exports = FileUpload;
