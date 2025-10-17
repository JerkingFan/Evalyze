package org.example.new_new_mvp.service;

import org.example.new_new_mvp.dto.CompanyDto;
import org.example.new_new_mvp.model.Company;
import org.example.new_new_mvp.repository.CompanyRepository;
import org.example.new_new_mvp.repository.InvitationRepository;
import org.example.new_new_mvp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InvitationRepository invitationRepository;
    
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<CompanyDto> getCompanyById(UUID id) {
        return companyRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public CompanyDto createCompany(String name) {
        if (companyRepository.existsByName(name)) {
            throw new RuntimeException("Company with name " + name + " already exists");
        }
        
        Company company = new Company();
        company.setName(name);
        
        Company savedCompany = companyRepository.save(company);
        return convertToDto(savedCompany);
    }
    
    public CompanyDto updateCompany(UUID id, String name) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        company.setName(name);
        Company savedCompany = companyRepository.save(company);
        return convertToDto(savedCompany);
    }
    
    public void deleteCompany(UUID id) {
        if (!companyRepository.existsById(id)) {
            throw new RuntimeException("Company not found");
        }
        
        companyRepository.deleteById(id);
    }
    
    private CompanyDto convertToDto(Company company) {
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setCreatedAt(company.getCreatedAt());
        
        // Count users and invitations
        long userCount = userRepository.countByCompanyId(company.getId());
        long invitationCount = invitationRepository.countByCompanyId(company.getId());
        
        dto.setUserCount(userCount);
        dto.setInvitationCount(invitationCount);
        
        return dto;
    }
}
