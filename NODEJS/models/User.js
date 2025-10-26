const { v4: uuidv4 } = require('uuid');

class User {
  constructor(data = {}) {
    this.id = data.id || null;
    this.email = data.email || '';
    this.fullName = data.full_name || data.fullName || '';
    this.password = data.password || '';
    this.telegramChatId = data.telegram_chat_id || data.telegramChatId || '';
    this.activationCode = data.activation_code || data.activationCode || '';
    this.status = data.status || 'invited';
    this.role = data.role || null; // UUID of job role
    this.createdAt = data.created_at || data.createdAt || new Date().toISOString();
    this.lastUpdated = data.last_updated || data.lastUpdated || new Date().toISOString();
    this.company = data.company || null;
  }

  // Convert to Supabase format
  toSupabase() {
    return {
      email: this.email,
      full_name: this.fullName,
      password: this.password,
      telegram_chat_id: this.telegramChatId,
      activation_code: this.activationCode,
      status: this.status,
      Role: this.role,
      created_at: this.createdAt,
      last_updated: this.lastUpdated
    };
  }

  // Create from Supabase data
  static fromSupabase(data) {
    const user = new User();
    user.id = data.id;
    user.email = data.email;
    user.fullName = data.full_name;
    user.password = data.password;
    user.telegramChatId = data.telegram_chat_id;
    user.activationCode = data.activation_code;
    user.status = data.status;
    user.role = data.Role;
    user.createdAt = data.created_at;
    user.lastUpdated = data.last_updated;
    return user;
  }

  // Generate activation code
  generateActivationCode() {
    this.activationCode = uuidv4();
    return this.activationCode;
  }

  // Check if user is active
  isActive() {
    return this.status === 'active' || this.status === 'company';
  }

  // Check if user is company
  isCompany() {
    return this.status === 'company';
  }

  // Check if user is employee
  isEmployee() {
    return this.status === 'employee' || this.status === 'invited';
  }

  // Get user role enum
  getUserRole() {
    if (this.isCompany()) {
      return 'COMPANY';
    }
    return 'EMPLOYEE';
  }

  // Convert to safe object (without password)
  toSafeObject() {
    const safe = { ...this };
    delete safe.password;
    return safe;
  }
}

module.exports = User;
