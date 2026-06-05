package com.authsystemjava.backend.controller;

import com.authsystemjava.backend.dto.*;
import com.authsystemjava.backend.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyDto> create(
            @Valid @RequestBody CreateCompanyRequest request,
            Authentication auth) {
        return ResponseEntity.ok(companyService.createCompany(request, auth.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<CompanyDto> getMyCompany(Authentication auth) {
        return ResponseEntity.ok(companyService.getMyCompany(auth.getName()));
    }

    @PostMapping("/{companyId}/members")
    public ResponseEntity<CompanyMemberDto> inviteMember(
            @PathVariable String companyId,
            @Valid @RequestBody InviteMemberRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
            companyService.inviteMember(companyId, request, auth.getName()));
    }

    @PutMapping("/{companyId}/members/{memberId}/role")
    public ResponseEntity<CompanyMemberDto> updateMemberRole(
            @PathVariable String companyId,
            @PathVariable String memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
            companyService.updateMemberRole(companyId, memberId, request, auth.getName()));
    }

    @DeleteMapping("/{companyId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String companyId,
            @PathVariable String memberId,
            Authentication auth) {
        companyService.removeMember(companyId, memberId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/leave")
    public ResponseEntity<Void> leaveCompany(Authentication auth) {
        companyService.leaveCompany(auth.getName());
        return ResponseEntity.noContent().build();
    }
}