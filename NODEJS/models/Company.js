const { v4: uuidv4 } = require('uuid');

class Company {
  constructor(data = {}) {
    this.id = data.id || null;
    this.name = data.name || '';
    this.description = data.description || '';
    this.createdAt = data.created_at || data.createdAt || new Date().toISOString();
    this.lastUpdated = data.last_updated || data.lastUpdated || new Date().toISOString();
  }

  // Convert to Supabase format
  toSupabase() {
    return {
      name: this.name,
      description: this.description,
      created_at: this.createdAt,
      last_updated: this.lastUpdated
    };
  }

  // Create from Supabase data
  static fromSupabase(data) {
    const company = new Company();
    company.id = data.id;
    company.name = data.name;
    company.description = data.description;
    company.createdAt = data.created_at;
    company.lastUpdated = data.last_updated;
    return company;
  }

  // Update company data
  updateData(newData) {
    this.name = newData.name || this.name;
    this.description = newData.description || this.description;
    this.lastUpdated = new Date().toISOString();
  }

  // Convert to DTO
  toDto() {
    return {
      id: this.id,
      name: this.name,
      description: this.description,
      createdAt: this.createdAt,
      lastUpdated: this.lastUpdated
    };
  }
}

module.exports = Company;
