package com.authsystemjava.backend.service;

import com.authsystemjava.backend.dto.*;
import com.authsystemjava.backend.model.*;
import com.authsystemjava.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public CompanyDto createCompany(CreateCompanyRequest request, String userId) {
        // check user isn't already in a company
        if (companyMemberRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("You already belong to a company");
        }

        String slug = generateSlug(request.getName());

        Company company = Company.builder()
                .name(request.getName())
                .slug(slug)
                .build();

        companyRepository.save(company);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CompanyMember owner = CompanyMember.builder()
                .company(company)
                .user(user)
                .role(CompanyRole.OWNER)
                .build();

        companyMemberRepository.save(owner);
        log.info("Company created: {} by user: {}", company.getName(), user.getEmail());

        return CompanyDto.from(company);
    }

    @Transactional(readOnly = true)
    public CompanyDto getMyCompany(String userId) {
        CompanyMember membership = companyMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("You don't belong to a company"));

        Company company = membership.getCompany();
        List<CompanyMemberDto> members = companyMemberRepository
                .findAllByCompanyId(company.getId())
                .stream()
                .map(CompanyMemberDto::from)
                .toList();

        return CompanyDto.withMembers(company, members);
    }

    @Transactional
    public CompanyMemberDto inviteMember(String companyId,
                                         InviteMemberRequest request,
                                         String requesterId) {
        validateRole(companyId, requesterId, CompanyRole.ADMIN);

        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No user found with that email"));

        if (companyMemberRepository.existsByCompanyIdAndUserId(
                companyId, invitedUser.getId())) {
            throw new RuntimeException("User is already a member");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        CompanyRole role;
        try {
            role = CompanyRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            role = CompanyRole.MEMBER;
        }

        CompanyMember member = CompanyMember.builder()
                .company(company)
                .user(invitedUser)
                .role(role)
                .build();

        companyMemberRepository.save(member);
        log.info("User {} invited to company {}", invitedUser.getEmail(), company.getName());

        return CompanyMemberDto.from(member);
    }

    @Transactional
    public CompanyMemberDto updateMemberRole(String companyId,
                                              String memberId,
                                              UpdateMemberRoleRequest request,
                                              String requesterId) {
        validateRole(companyId, requesterId, CompanyRole.ADMIN);

        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (member.getRole() == CompanyRole.OWNER) {
            throw new RuntimeException("Cannot change the owner's role");
        }

        member.setRole(request.getRole());
        companyMemberRepository.save(member);

        return CompanyMemberDto.from(member);
    }

    @Transactional
    public void removeMember(String companyId, String memberId, String requesterId) {
        validateRole(companyId, requesterId, CompanyRole.ADMIN);

        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (member.getRole() == CompanyRole.OWNER) {
            throw new RuntimeException("Cannot remove the owner");
        }

        companyMemberRepository.delete(member);
        log.info("Member {} removed from company {}", memberId, companyId);
    }

    @Transactional
    public void leaveCompany(String userId) {
        CompanyMember member = companyMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("You don't belong to a company"));

        if (member.getRole() == CompanyRole.OWNER) {
            throw new RuntimeException("Owner cannot leave — transfer ownership first");
        }

        companyMemberRepository.delete(member);
    }

    // ── Helpers ────────────────────────────────────────────────

    private void validateRole(String companyId, String userId, CompanyRole minimumRole) {
        CompanyMember requester = companyMemberRepository
                .findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this company"));

        boolean hasPermission = switch (minimumRole) {
            case OWNER -> requester.getRole() == CompanyRole.OWNER;
            case ADMIN -> requester.getRole() == CompanyRole.OWNER ||
                          requester.getRole() == CompanyRole.ADMIN;
            case MEMBER -> true;
        };

        if (!hasPermission) {
            throw new RuntimeException("Insufficient permissions");
        }
    }

    private String generateSlug(String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        String slug = base;
        int count = 1;
        while (companyRepository.existsBySlug(slug)) {
            slug = base + "-" + count++;
        }
        return slug;
    }
}