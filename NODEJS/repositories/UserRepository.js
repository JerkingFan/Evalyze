const { supabase, supabaseAdmin } = require('../config/supabase');
const User = require('../models/User');

class UserRepository {
  // Find user by email
  async findByEmail(email) {
    try {
      const { data, error } = await supabase
        .from('users')
        .select('*')
        .eq('email', email)
        .single();

      if (error) {
        if (error.code === 'PGRST116') {
          return null; // No rows found
        }
        throw error;
      }

      return data ? User.fromSupabase(data) : null;
    } catch (error) {
      console.error('Error finding user by email:', error);
      throw error;
    }
  }

  // Find user by activation code
  async findByActivationCode(activationCode) {
    try {
      const { data, error } = await supabase
        .from('users')
        .select('*')
        .eq('activation_code', activationCode)
        .single();

      if (error) {
        if (error.code === 'PGRST116') {
          return null; // No rows found
        }
        throw error;
      }

      return User.fromSupabase(data);
    } catch (error) {
      console.error('Error finding user by activation code:', error);
      throw error;
    }
  }

  // Find user by ID
  async findById(id) {
    try {
      const { data, error } = await supabase
        .from('users')
        .select('*')
        .eq('id', id)
        .single();

      if (error) {
        if (error.code === 'PGRST116') {
          return null; // No rows found
        }
        throw error;
      }

      return User.fromSupabase(data);
    } catch (error) {
      console.error('Error finding user by ID:', error);
      throw error;
    }
  }

  // Check if user exists by email
  async existsByEmail(email) {
    try {
      const { data, error } = await supabase
        .from('users')
        .select('id')
        .eq('email', email)
        .single();

      return !error && data;
    } catch (error) {
      return false;
    }
  }

  // Check if user exists by activation code
  async existsByActivationCode(activationCode) {
    try {
      const { data, error } = await supabase
        .from('users')
        .select('id')
        .eq('activation_code', activationCode)
        .single();

      return !error && data;
    } catch (error) {
      return false;
    }
  }

  // Save user (insert or update)
  async save(user) {
    try {
      const exists = await this.existsByEmail(user.email);
      
      if (exists) {
        return await this.update(user);
      } else {
        return await this.insert(user);
      }
    } catch (error) {
      console.error('Error saving user:', error);
      throw error;
    }
  }

  // Insert new user
  async insert(user) {
    try {
      const { data, error } = await supabase
        .from('users')
        .insert([user.toSupabase()])
        .select()
        .single();

      if (error) {
        throw error;
      }

      return User.fromSupabase(data);
    } catch (error) {
      console.error('Error inserting user:', error);
      throw error;
    }
  }

  // Update existing user
  async update(user) {
    try {
      const { data, error } = await supabase
        .from('users')
        .update(user.toSupabase())
        .eq('email', user.email)
        .select()
        .single();

      if (error) {
        throw error;
      }

      return User.fromSupabase(data);
    } catch (error) {
      console.error('Error updating user:', error);
      throw error;
    }
  }

  // Update user role by email
  async updateUserRoleByEmail(email, roleId) {
    try {
      const { data, error } = await supabase
        .from('users')
        .update({ Role: roleId })
        .eq('email', email)
        .select()
        .single();

      if (error) {
        throw error;
      }

      return data;
    } catch (error) {
      console.error('Error updating user role by email:', error);
      throw error;
    }
  }

  // Update user role by activation code
  async updateUserRoleByActivationCode(activationCode, roleId) {
    try {
      const { data, error } = await supabase
        .from('users')
        .update({ Role: roleId })
        .eq('activation_code', activationCode)
        .select()
        .single();

      if (error) {
        throw error;
      }

      return data;
    } catch (error) {
      console.error('Error updating user role by activation code:', error);
      throw error;
    }
  }

  // Get all users
  async findAll() {
    try {
      const { data, error } = await supabase
        .from('users')
        .select('*');

      if (error) {
        throw error;
      }

      return data.map(user => User.fromSupabase(user));
    } catch (error) {
      console.error('Error finding all users:', error);
      throw error;
    }
  }

  // Delete user by ID
  async deleteById(id) {
    try {
      const { error } = await supabase
        .from('users')
        .delete()
        .eq('id', id);

      if (error) {
        throw error;
      }

      return true;
    } catch (error) {
      console.error('Error deleting user:', error);
      throw error;
    }
  }
}

module.exports = new UserRepository();
