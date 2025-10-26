const { supabase, supabaseAdmin } = require('../config/supabase');
const Profile = require('../models/Profile');

class ProfileRepository {
  // Find profile by user ID
  async findByUserId(userId) {
    try {
      const { data, error } = await supabase
        .from('profiles')
        .select('*')
        .eq('user_id', userId)
        .single();

      if (error) {
        if (error.code === 'PGRST116') {
          return null; // No rows found
        }
        throw error;
      }

      return Profile.fromSupabase(data);
    } catch (error) {
      console.error('Error finding profile by user ID:', error);
      throw error;
    }
  }

  // Find profiles by company ID
  async findByUserCompanyId(companyId) {
    try {
      const { data, error } = await supabase
        .from('profiles')
        .select('*')
        .eq('company_id', companyId);

      if (error) {
        throw error;
      }

      return data.map(profile => Profile.fromSupabase(profile));
    } catch (error) {
      console.error('Error finding profiles by company ID:', error);
      throw error;
    }
  }

  // Get all profiles
  async findAll() {
    try {
      const { data, error } = await supabase
        .from('profiles')
        .select('*');

      if (error) {
        throw error;
      }

      return data.map(profile => Profile.fromSupabase(profile));
    } catch (error) {
      console.error('Error finding all profiles:', error);
      throw error;
    }
  }

  // Save profile (insert or update)
  async save(profile) {
    try {
      const existing = await this.findByUserId(profile.userId);
      
      if (existing) {
        return await this.update(profile);
      } else {
        return await this.insert(profile);
      }
    } catch (error) {
      console.error('Error saving profile:', error);
      throw error;
    }
  }

  // Insert new profile
  async insert(profile) {
    try {
      const { data, error } = await supabase
        .from('profiles')
        .insert([profile.toSupabase()])
        .select()
        .single();

      if (error) {
        throw error;
      }

      return Profile.fromSupabase(data);
    } catch (error) {
      console.error('Error inserting profile:', error);
      throw error;
    }
  }

  // Update existing profile
  async update(profile) {
    try {
      const { data, error } = await supabase
        .from('profiles')
        .update(profile.toSupabase())
        .eq('user_id', profile.userId)
        .select()
        .single();

      if (error) {
        throw error;
      }

      return Profile.fromSupabase(data);
    } catch (error) {
      console.error('Error updating profile:', error);
      throw error;
    }
  }

  // Delete profile by user ID
  async deleteByUserId(userId) {
    try {
      const { error } = await supabase
        .from('profiles')
        .delete()
        .eq('user_id', userId);

      if (error) {
        throw error;
      }

      return true;
    } catch (error) {
      console.error('Error deleting profile:', error);
      throw error;
    }
  }

  // Count profiles
  async count() {
    try {
      const { count, error } = await supabase
        .from('profiles')
        .select('*', { count: 'exact', head: true });

      if (error) {
        throw error;
      }

      return count || 0;
    } catch (error) {
      console.error('Error counting profiles:', error);
      throw error;
    }
  }
}

module.exports = new ProfileRepository();
