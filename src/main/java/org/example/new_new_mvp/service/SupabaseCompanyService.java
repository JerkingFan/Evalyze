package org.example.new_new_mvp.service;

import org.example.new_new_mvp.dto.CompanyDto;
import org.example.new_new_mvp.model.Company;
import org.example.new_new_mvp.repository.SupabaseCompanyRepository;
import org.example.new_new_mvp.repository.SupabaseUserRepository;
import org.example.new_new_mvp.repository.SupabaseInvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SupabaseCompanyService {
    
    @Autowired
    private SupabaseCompanyRepository companyRepository;
    
    @Autowired
    private SupabaseUserRepository userRepository;
    
    @Autowired
    private SupabaseInvitationRepository invitationRepository;
    
    public Mono<List<CompanyDto>> getAllCompanies() {
        return companyRepository.findAll()
                .map(companies -> companies.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()));
    }
    
    public Mono<CompanyDto> getCompanyById(UUID id) {
        return companyRepository.findById(id)
                .map(optionalCompany -> optionalCompany.map(this::convertToDto).orElse(null));
    }
    
    public Mono<CompanyDto> createCompany(CompanyDto companyDto) {
        Company company = convertToEntity(companyDto);
        return companyRepository.save(company)
                .map(this::convertToDto);
    }
    
    public Mono<CompanyDto> updateCompany(UUID id, CompanyDto companyDto) {
        return companyRepository.findById(id)
                .flatMap(optionalCompany -> {
                    if (optionalCompany.isPresent()) {
                        Company company = optionalCompany.get();
                        company.setName(companyDto.getName());
                        return companyRepository.update(company)
                                .map(this::convertToDto);
                    } else {
                        return Mono.empty();
                    }
                });
    }
    
    public Mono<Void> deleteCompany(UUID id) {
        return companyRepository.deleteById(id);
    }
    
    public Mono<Long> getCompanyUserCount(UUID companyId) {
        return userRepository.countByCompanyId(companyId);
    }
    
    public Mono<Long> getCompanyInvitationCount(UUID companyId) {
        return invitationRepository.countByCompanyId(companyId);
    }
    
    private CompanyDto convertToDto(Company company) {
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setCreatedAt(company.getCreatedAt());
        return dto;
    }
    
    private Company convertToEntity(CompanyDto dto) {
        Company company = new Company();
        if (dto.getId() != null) {
            company.setId(UUID.fromString(dto.getId()));
        }
        company.setName(dto.getName());
        return company;
    }
}

