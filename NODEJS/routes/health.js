const express = require('express');
const router = express.Router();
const healthController = require('../controllers/HealthController');

// Public routes
router.get('/', healthController.healthCheck);
router.get('/database', healthController.databaseHealth);
router.get('/system', healthController.systemInfo);

module.exports = router;
