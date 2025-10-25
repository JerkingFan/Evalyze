package org.example.new_new_mvp.controller;

import org.example.new_new_mvp.dto.CompanyDto;
import org.example.new_new_mvp.service.SupabaseCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*")
public class CompanyController {
    
    @Autowired
    private SupabaseCompanyService companyService;
    
    @GetMapping
    public Mono<ResponseEntity<List<CompanyDto>>> getAllCompanies() {
        return companyService.getAllCompanies()
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CompanyDto>> getCompanyById(@PathVariable UUID id) {
        return companyService.getCompanyById(id)
                .map(company -> company != null ? ResponseEntity.ok(company) : ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<CompanyDto>> createCompany(@RequestBody String name) {
        CompanyDto companyDto = new CompanyDto();
        companyDto.setName(name);
        return companyService.createCompany(companyDto)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<CompanyDto>> updateCompany(@PathVariable UUID id, @RequestBody String name) {
        CompanyDto companyDto = new CompanyDto();
        companyDto.setName(name);
        return companyService.updateCompany(id, companyDto)
                .map(company -> company != null ? ResponseEntity.ok(company) : ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> deleteCompany(@PathVariable UUID id) {
        return companyService.deleteCompany(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.notFound().build());
    }
}