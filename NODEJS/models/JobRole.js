const { v4: uuidv4 } = require('uuid');

class JobRole {
  constructor(data = {}) {
    this.id = data.id || null;
    this.title = data.title || '';
    this.description = data.description || '';
    this.requirements = data.requirements || {};
    this.companyId = data.company_id || data.companyId || null;
    this.createdAt = data.created_at || data.createdAt || new Date().toISOString();
    this.lastUpdated = data.last_updated || data.lastUpdated || new Date().toISOString();
  }

  // Convert to Supabase format
  toSupabase() {
    return {
      title: this.title,
      description: this.description,
      requirements: this.requirements,
      company_id: this.companyId,
      created_at: this.createdAt,
      last_updated: this.lastUpdated
    };
  }

  // Create from Supabase data
  static fromSupabase(data) {
    const jobRole = new JobRole();
    jobRole.id = data.id;
    jobRole.title = data.title;
    jobRole.description = data.description;
    jobRole.requirements = data.requirements;
    jobRole.companyId = data.company_id;
    jobRole.createdAt = data.created_at;
    jobRole.lastUpdated = data.last_updated;
    return jobRole;
  }

  // Update job role data
  updateData(newData) {
    this.title = newData.title || this.title;
    this.description = newData.description || this.description;
    this.requirements = newData.requirements || this.requirements;
    this.lastUpdated = new Date().toISOString();
  }

  // Convert to DTO
  toDto() {
    return {
      id: this.id,
      title: this.title,
      description: this.description,
      requirements: this.requirements,
      companyId: this.companyId,
      createdAt: this.createdAt,
      lastUpdated: this.lastUpdated
    };
  }
}

module.exports = JobRole;
