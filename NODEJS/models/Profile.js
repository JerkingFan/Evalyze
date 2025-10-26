const { v4: uuidv4 } = require('uuid');

class Profile {
  constructor(data = {}) {
    this.userId = data.user_id || data.userId || null;
    this.profileData = data.profile_data || data.profileData || {};
    this.companyId = data.company_id || data.companyId || null;
    this.status = data.status || 'PENDING';
    this.lastUpdated = data.last_updated || data.lastUpdated || new Date().toISOString();
  }

  // Convert to Supabase format
  toSupabase() {
    return {
      user_id: this.userId,
      profile_data: this.profileData,
      company_id: this.companyId,
      last_updated: this.lastUpdated
    };
  }

  // Create from Supabase data
  static fromSupabase(data) {
    const profile = new Profile();
    profile.userId = data.user_id;
    profile.profileData = data.profile_data;
    profile.companyId = data.company_id;
    profile.lastUpdated = data.last_updated;
    return profile;
  }

  // Update profile data
  updateProfileData(newData) {
    this.profileData = { ...this.profileData, ...newData };
    this.lastUpdated = new Date().toISOString();
  }

  // Set status
  setStatus(status) {
    this.status = status;
    this.lastUpdated = new Date().toISOString();
  }

  // Check if profile is completed
  isCompleted() {
    return this.status === 'COMPLETED';
  }

  // Check if profile is pending
  isPending() {
    return this.status === 'PENDING';
  }

  // Get current position from profile data
  getCurrentPosition() {
    return this.profileData?.currentPosition || '';
  }

  // Get assigned role ID
  getAssignedRoleId() {
    return this.profileData?.assignedRoleId || null;
  }

  // Set job role data
  setJobRoleData(jobRole) {
    this.updateProfileData({
      currentPosition: jobRole.title,
      jobRoleData: jobRole.requirements || {},
      assignedRoleId: jobRole.id,
      description: jobRole.description || ''
    });
    this.setStatus('COMPLETED');
  }

  // Convert to DTO
  toDto() {
    return {
      userId: this.userId,
      profileData: this.profileData,
      companyId: this.companyId,
      status: this.status,
      lastUpdated: this.lastUpdated,
      isVerified: this.isCompleted(),
      aiProfileGenerated: false // For now, always false
    };
  }
}

module.exports = Profile;
