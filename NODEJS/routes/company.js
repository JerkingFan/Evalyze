const express = require('express');
const router = express.Router();
const companyRepository = require('../repositories/CompanyRepository');
const jobRoleRepository = require('../repositories/JobRoleRepository');
const { authenticateToken } = require('../config/jwt');

// Get all companies
router.get('/', authenticateToken, async (req, res) => {
  try {
    const companies = await companyRepository.findAll();
    res.json({ companies });
  } catch (error) {
    console.error('Get companies error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get company by ID
router.get('/:id', authenticateToken, async (req, res) => {
  try {
    const company = await companyRepository.findById(req.params.id);
    if (!company) {
      return res.status(404).json({ error: 'Company not found' });
    }
    res.json({ company });
  } catch (error) {
    console.error('Get company error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Create company
router.post('/', authenticateToken, async (req, res) => {
  try {
    const { name, description } = req.body;
    
    if (!name) {
      return res.status(400).json({ error: 'Company name is required' });
    }

    const company = await companyRepository.save({
      name,
      description: description || ''
    });

    res.status(201).json({ company });
  } catch (error) {
    console.error('Create company error:', error);
    res.status(400).json({ error: error.message });
  }
});

// Update company
router.put('/:id', authenticateToken, async (req, res) => {
  try {
    const { name, description } = req.body;
    
    const company = await companyRepository.findById(req.params.id);
    if (!company) {
      return res.status(404).json({ error: 'Company not found' });
    }

    company.updateData({ name, description });
    const updatedCompany = await companyRepository.save(company);

    res.json({ company: updatedCompany });
  } catch (error) {
    console.error('Update company error:', error);
    res.status(400).json({ error: error.message });
  }
});

// Delete company
router.delete('/:id', authenticateToken, async (req, res) => {
  try {
    const company = await companyRepository.findById(req.params.id);
    if (!company) {
      return res.status(404).json({ error: 'Company not found' });
    }

    await companyRepository.deleteById(req.params.id);
    res.json({ message: 'Company deleted successfully' });
  } catch (error) {
    console.error('Delete company error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get job roles for company
router.get('/:id/job-roles', authenticateToken, async (req, res) => {
  try {
    const jobRoles = await jobRoleRepository.findByCompanyId(req.params.id);
    res.json({ jobRoles });
  } catch (error) {
    console.error('Get company job roles error:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
