const { supabase, supabaseAdmin } = require('../config/supabase');
const Company = require('../models/Company');

class CompanyRepository {
  // Find company by ID
  async findById(id) {
    try {
      const { data, error } = await supabase
        .from('companies')
        .select('*')
        .eq('id', id)
        .single();

      if (error) {
        if (error.code === 'PGRST116') {
          return null; // No rows found
        }
        throw error;
      }

      return Company.fromSupabase(data);
    } catch (error) {
      console.error('Error finding company by ID:', error);
      throw error;
    }
  }

  // Find company by name
  async findByName(name) {
    try {
      const { data, error } = await supabase
        .from('companies')
        .select('*')
        .eq('name', name)
        .single();

      if (error) {
        if (error.code === 'PGRST116') {
          return null; // No rows found
        }
        throw error;
      }

      return Company.fromSupabase(data);
    } catch (error) {
      console.error('Error finding company by name:', error);
      throw error;
    }
  }

  // Get all companies
  async findAll() {
    try {
      const { data, error } = await supabase
        .from('companies')
        .select('*');

      if (error) {
        throw error;
      }

      return data.map(company => Company.fromSupabase(company));
    } catch (error) {
      console.error('Error finding all companies:', error);
      throw error;
    }
  }

  // Save company (insert or update)
  async save(company) {
    try {
      if (company.id) {
        return await this.update(company);
      } else {
        return await this.insert(company);
      }
    } catch (error) {
      console.error('Error saving company:', error);
      throw error;
    }
  }

  // Insert new company
  async insert(company) {
    try {
      const { data, error } = await supabase
        .from('companies')
        .insert([company.toSupabase()])
        .select()
        .single();

      if (error) {
        throw error;
      }

      return Company.fromSupabase(data);
    } catch (error) {
      console.error('Error inserting company:', error);
      throw error;
    }
  }

  // Update existing company
  async update(company) {
    try {
      const { data, error } = await supabase
        .from('companies')
        .update(company.toSupabase())
        .eq('id', company.id)
        .select()
        .single();

      if (error) {
        throw error;
      }

      return Company.fromSupabase(data);
    } catch (error) {
      console.error('Error updating company:', error);
      throw error;
    }
  }

  // Delete company by ID
  async deleteById(id) {
    try {
      const { error } = await supabase
        .from('companies')
        .delete()
        .eq('id', id);

      if (error) {
        throw error;
      }

      return true;
    } catch (error) {
      console.error('Error deleting company:', error);
      throw error;
    }
  }

  // Count companies
  async count() {
    try {
      const { count, error } = await supabase
        .from('companies')
        .select('*', { count: 'exact', head: true });

      if (error) {
        throw error;
      }

      return count || 0;
    } catch (error) {
      console.error('Error counting companies:', error);
      throw error;
    }
  }
}

module.exports = new CompanyRepository();
