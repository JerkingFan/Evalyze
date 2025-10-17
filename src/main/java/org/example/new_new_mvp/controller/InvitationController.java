package org.example.new_new_mvp.controller;

import org.example.new_new_mvp.dto.AcceptInvitationRequest;
import org.example.new_new_mvp.dto.CreateInvitationRequest;
import org.example.new_new_mvp.dto.InvitationDto;
import org.example.new_new_mvp.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = "*")
public class InvitationController {
    
    @Autowired
    private InvitationService invitationService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitationDto> createInvitation(@Valid @RequestBody CreateInvitationRequest request) {
        try {
            InvitationDto invitation = invitationService.createInvitation(request);
            return ResponseEntity.ok(invitation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationDto>> getCompanyInvitations(@PathVariable UUID companyId) {
        List<InvitationDto> invitations = invitationService.getCompanyInvitations(companyId);
        return ResponseEntity.ok(invitations);
    }
    
    @GetMapping("/company/{companyId}/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationDto>> getPendingInvitations(@PathVariable UUID companyId) {
        List<InvitationDto> invitations = invitationService.getPendingInvitations(companyId);
        return ResponseEntity.ok(invitations);
    }
    
    @GetMapping("/code/{invitationCode}")
    public ResponseEntity<InvitationDto> getInvitationByCode(@PathVariable String invitationCode) {
        return invitationService.getInvitationByCode(invitationCode)
                .map(invitation -> ResponseEntity.ok(invitation))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/accept")
    public ResponseEntity<String> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        try {
            invitationService.acceptInvitation(request.getInvitationCode(), request.getFullName());
            return ResponseEntity.ok("Invitation accepted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{invitationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInvitation(@PathVariable UUID invitationId) {
        try {
            invitationService.deleteInvitation(invitationId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
