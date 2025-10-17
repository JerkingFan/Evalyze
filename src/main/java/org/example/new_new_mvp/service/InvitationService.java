package org.example.new_new_mvp.service;

import org.example.new_new_mvp.dto.CreateInvitationRequest;
import org.example.new_new_mvp.dto.InvitationDto;
import org.example.new_new_mvp.model.Company;
import org.example.new_new_mvp.model.Invitation;
import org.example.new_new_mvp.model.InvitationStatus;
import org.example.new_new_mvp.repository.CompanyRepository;
import org.example.new_new_mvp.repository.InvitationRepository;
import org.example.new_new_mvp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvitationService {
    
    @Autowired
    private InvitationRepository invitationRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    public InvitationDto createInvitation(CreateInvitationRequest request) {
        UUID companyId = UUID.fromString(request.getCompanyId());
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }
        
        // Check if invitation already exists
        if (invitationRepository.existsByEmailAndCompanyId(request.getEmail(), companyId)) {
            throw new RuntimeException("Invitation for email " + request.getEmail() + " already exists");
        }
        
        Invitation invitation = new Invitation();
        invitation.setCompany(company);
        invitation.setEmail(request.getEmail());
        invitation.setInvitationCode(generateInvitationCode());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        
        Invitation savedInvitation = invitationRepository.save(invitation);
        
        // Send invitation email
        emailService.sendInvitationEmail(request.getEmail(), savedInvitation.getInvitationCode(), company.getName());
        
        return convertToDto(savedInvitation);
    }
    
    public List<InvitationDto> getCompanyInvitations(UUID companyId) {
        return invitationRepository.findByCompanyId(companyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<InvitationDto> getPendingInvitations(UUID companyId) {
        return invitationRepository.findByCompanyIdAndStatus(companyId, InvitationStatus.PENDING).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<InvitationDto> getInvitationByCode(String invitationCode) {
        return invitationRepository.findByInvitationCode(invitationCode)
                .map(this::convertToDto);
    }
    
    public void acceptInvitation(String invitationCode, String fullName) {
        Invitation invitation = invitationRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new RuntimeException("Invalid invitation code"));
        
        if (!invitation.isValid()) {
            throw new RuntimeException("Invitation is expired or already used");
        }
        
        // Create user
        org.example.new_new_mvp.model.User user = new org.example.new_new_mvp.model.User();
        user.setCompany(invitation.getCompany());
        user.setEmail(invitation.getEmail());
        user.setFullName(fullName);
        user.setRole(org.example.new_new_mvp.model.UserRole.EMPLOYEE);
        
        userRepository.save(user);
        
        // Update invitation status
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
    }
    
    public void deleteInvitation(UUID invitationId) {
        if (!invitationRepository.existsById(invitationId)) {
            throw new RuntimeException("Invitation not found");
        }
        
        invitationRepository.deleteById(invitationId);
    }
    
    private String generateInvitationCode() {
        return "INVITE" + System.currentTimeMillis();
    }
    
    private InvitationDto convertToDto(Invitation invitation) {
        InvitationDto dto = new InvitationDto();
        dto.setId(invitation.getId());
        dto.setCompanyId(invitation.getCompany().getId());
        dto.setCompanyName(invitation.getCompany().getName());
        dto.setEmail(invitation.getEmail());
        dto.setInvitationCode(invitation.getInvitationCode());
        dto.setStatus(invitation.getStatus());
        dto.setExpiresAt(invitation.getExpiresAt());
        dto.setExpired(invitation.isExpired());
        
        return dto;
    }
}
