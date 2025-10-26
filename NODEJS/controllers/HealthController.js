const { testConnection } = require('../config/database');

class HealthController {
  // Health check endpoint
  async healthCheck(req, res) {
    try {
      const isDbConnected = await testConnection();
      
      const health = {
        status: 'ok',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        environment: process.env.NODE_ENV || 'development',
        version: '1.0.0',
        database: isDbConnected ? 'connected' : 'disconnected',
        memory: {
          used: Math.round(process.memoryUsage().heapUsed / 1024 / 1024),
          total: Math.round(process.memoryUsage().heapTotal / 1024 / 1024)
        }
      };

      const statusCode = isDbConnected ? 200 : 503;
      res.status(statusCode).json(health);
    } catch (error) {
      console.error('Health check error:', error);
      res.status(503).json({
        status: 'error',
        timestamp: new Date().toISOString(),
        error: error.message
      });
    }
  }

  // Database health check
  async databaseHealth(req, res) {
    try {
      const isConnected = await testConnection();
      
      res.json({
        status: isConnected ? 'healthy' : 'unhealthy',
        timestamp: new Date().toISOString(),
        connected: isConnected
      });
    } catch (error) {
      console.error('Database health check error:', error);
      res.status(503).json({
        status: 'error',
        timestamp: new Date().toISOString(),
        error: error.message
      });
    }
  }

  // System info
  async systemInfo(req, res) {
    try {
      const info = {
        nodeVersion: process.version,
        platform: process.platform,
        arch: process.arch,
        uptime: process.uptime(),
        memory: process.memoryUsage(),
        cpu: process.cpuUsage(),
        environment: process.env.NODE_ENV || 'development',
        timestamp: new Date().toISOString()
      };

      res.json(info);
    } catch (error) {
      console.error('System info error:', error);
      res.status(500).json({
        error: error.message
      });
    }
  }
}

module.exports = new HealthController();
