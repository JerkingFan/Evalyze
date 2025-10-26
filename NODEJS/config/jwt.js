const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key';
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '24h';

// Generate JWT token
const generateToken = (payload) => {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: JWT_EXPIRES_IN });
};

// Verify JWT token
const verifyToken = (token) => {
  try {
    return jwt.verify(token, JWT_SECRET);
  } catch (error) {
    throw new Error('Invalid token');
  }
};

// Extract token from Authorization header
const extractToken = (req) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return null;
  }
  return authHeader.substring(7);
};

// Middleware to verify JWT token
const authenticateToken = (req, res, next) => {
  const token = extractToken(req);
  
  if (!token) {
    return res.status(401).json({ error: 'Access token required' });
  }
  
  try {
    const decoded = verifyToken(token);
    req.user = decoded;
    next();
  } catch (error) {
    return res.status(403).json({ error: 'Invalid or expired token' });
  }
};

// Optional authentication middleware
const optionalAuth = (req, res, next) => {
  const token = extractToken(req);
  
  if (token) {
    try {
      const decoded = verifyToken(token);
      req.user = decoded;
    } catch (error) {
      // Token is invalid, but we continue without user
      req.user = null;
    }
  } else {
    req.user = null;
  }
  
  next();
};

module.exports = {
  generateToken,
  verifyToken,
  extractToken,
  authenticateToken,
  optionalAuth
};
