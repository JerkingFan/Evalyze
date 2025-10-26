const { supabase, supabaseAdmin } = require('../config/supabase');
const FileUpload = require('../models/FileUpload');

class FileUploadRepository {
  // Find file upload by ID
  async findById(id) {
    try {
      const { data, error } = await supabase
        .from('file_uploads')
        .select('*')
        .eq('id', id)
        .single();

      if (error) {
        if (error.code === 'PGRST116') {
          return null; // No rows found
        }
        throw error;
      }

      return FileUpload.fromSupabase(data);
    } catch (error) {
      console.error('Error finding file upload by ID:', error);
      throw error;
    }
  }

  // Find file uploads by user ID
  async findByUserId(userId) {
    try {
      const { data, error } = await supabase
        .from('file_uploads')
        .select('*')
        .eq('user_id', userId)
        .order('uploaded_at', { ascending: false });

      if (error) {
        throw error;
      }

      return data.map(fileUpload => FileUpload.fromSupabase(fileUpload));
    } catch (error) {
      console.error('Error finding file uploads by user ID:', error);
      throw error;
    }
  }

  // Get all file uploads
  async findAll() {
    try {
      const { data, error } = await supabase
        .from('file_uploads')
        .select('*')
        .order('uploaded_at', { ascending: false });

      if (error) {
        throw error;
      }

      return data.map(fileUpload => FileUpload.fromSupabase(fileUpload));
    } catch (error) {
      console.error('Error finding all file uploads:', error);
      throw error;
    }
  }

  // Save file upload
  async save(fileUpload) {
    try {
      const { data, error } = await supabase
        .from('file_uploads')
        .insert([fileUpload.toSupabase()])
        .select()
        .single();

      if (error) {
        throw error;
      }

      return FileUpload.fromSupabase(data);
    } catch (error) {
      console.error('Error saving file upload:', error);
      throw error;
    }
  }

  // Delete file upload by ID
  async deleteById(id) {
    try {
      const { error } = await supabase
        .from('file_uploads')
        .delete()
        .eq('id', id);

      if (error) {
        throw error;
      }

      return true;
    } catch (error) {
      console.error('Error deleting file upload:', error);
      throw error;
    }
  }

  // Delete file uploads by user ID
  async deleteByUserId(userId) {
    try {
      const { error } = await supabase
        .from('file_uploads')
        .delete()
        .eq('user_id', userId);

      if (error) {
        throw error;
      }

      return true;
    } catch (error) {
      console.error('Error deleting file uploads by user ID:', error);
      throw error;
    }
  }

  // Count file uploads by user
  async countByUserId(userId) {
    try {
      const { count, error } = await supabase
        .from('file_uploads')
        .select('*', { count: 'exact', head: true })
        .eq('user_id', userId);

      if (error) {
        throw error;
      }

      return count || 0;
    } catch (error) {
      console.error('Error counting file uploads by user:', error);
      throw error;
    }
  }

  // Get total file size by user
  async getTotalSizeByUserId(userId) {
    try {
      const { data, error } = await supabase
        .from('file_uploads')
        .select('file_size')
        .eq('user_id', userId);

      if (error) {
        throw error;
      }

      return data.reduce((total, file) => total + (file.file_size || 0), 0);
    } catch (error) {
      console.error('Error getting total file size by user:', error);
      throw error;
    }
  }
}

module.exports = new FileUploadRepository();
