const { supabase, supabaseAdmin } = require('../config/supabase');
const JobRole = require('../models/JobRole');

class JobRoleRepository {
  // Find job role by ID
  async findById(id) {
    try {
      const { data, error } = await supabase
        .from('job_roles')
        .select('*')
        .eq('id', id)
        .single();

      if (error) {
        if (error.code === 'PGRST116') {
          return null; // No rows found
        }
        throw error;
      }

      return JobRole.fromSupabase(data);
    } catch (error) {
      console.error('Error finding job role by ID:', error);
      throw error;
    }
  }

  // Find job roles by company ID
  async findByCompanyId(companyId) {
    try {
      const { data, error } = await supabase
        .from('job_roles')
        .select('*')
        .eq('company_id', companyId);

      if (error) {
        throw error;
      }

      return data.map(jobRole => JobRole.fromSupabase(jobRole));
    } catch (error) {
      console.error('Error finding job roles by company ID:', error);
      throw error;
    }
  }

  // Get all job roles
  async findAll() {
    try {
      const { data, error } = await supabase
        .from('job_roles')
        .select('*');

      if (error) {
        throw error;
      }

      return data.map(jobRole => JobRole.fromSupabase(jobRole));
    } catch (error) {
      console.error('Error finding all job roles:', error);
      throw error;
    }
  }

  // Save job role (insert or update)
  async save(jobRole) {
    try {
      if (jobRole.id) {
        return await this.update(jobRole);
      } else {
        return await this.insert(jobRole);
      }
    } catch (error) {
      console.error('Error saving job role:', error);
      throw error;
    }
  }

  // Insert new job role
  async insert(jobRole) {
    try {
      const { data, error } = await supabase
        .from('job_roles')
        .insert([jobRole.toSupabase()])
        .select()
        .single();

      if (error) {
        throw error;
      }

      return JobRole.fromSupabase(data);
    } catch (error) {
      console.error('Error inserting job role:', error);
      throw error;
    }
  }

  // Update existing job role
  async update(jobRole) {
    try {
      const { data, error } = await supabase
        .from('job_roles')
        .update(jobRole.toSupabase())
        .eq('id', jobRole.id)
        .select()
        .single();

      if (error) {
        throw error;
      }

      return JobRole.fromSupabase(data);
    } catch (error) {
      console.error('Error updating job role:', error);
      throw error;
    }
  }

  // Delete job role by ID
  async deleteById(id) {
    try {
      const { error } = await supabase
        .from('job_roles')
        .delete()
        .eq('id', id);

      if (error) {
        throw error;
      }

      return true;
    } catch (error) {
      console.error('Error deleting job role:', error);
      throw error;
    }
  }

  // Count job roles
  async count() {
    try {
      const { count, error } = await supabase
        .from('job_roles')
        .select('*', { count: 'exact', head: true });

      if (error) {
        throw error;
      }

      return count || 0;
    } catch (error) {
      console.error('Error counting job roles:', error);
      throw error;
    }
  }

  // Count job roles by company
  async countByCompanyId(companyId) {
    try {
      const { count, error } = await supabase
        .from('job_roles')
        .select('*', { count: 'exact', head: true })
        .eq('company_id', companyId);

      if (error) {
        throw error;
      }

      return count || 0;
    } catch (error) {
      console.error('Error counting job roles by company:', error);
      throw error;
    }
  }
}

module.exports = new JobRoleRepository();
