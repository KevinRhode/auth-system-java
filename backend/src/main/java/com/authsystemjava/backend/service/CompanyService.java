package com.authsystemjava.backend.service;

import com.authsystemjava.backend.dto.*;
import com.authsystemjava.backend.exception.ApiException;
import com.authsystemjava.backend.exception.ErrorCode;
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
            throw new ApiException(ErrorCode.ALREADY_IN_COMPANY);
        }

        String slug = generateSlug(request.getName());

        Company company = Company.builder()
                .name(request.getName())
                .slug(slug)
                .build();

        companyRepository.save(company);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

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
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_A_MEMBER));

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
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (companyMemberRepository.existsByCompanyIdAndUserId(
                companyId, invitedUser.getId())) {
            throw new ApiException(ErrorCode.ALREADY_IN_COMPANY);
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMPANY_NOT_FOUND));

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

        CompanyMember member = companyMemberRepository.findByIdAndCompanyId(memberId, companyId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRole() == CompanyRole.OWNER) {
            throw new ApiException(ErrorCode.CANNOT_MODIFY_OWNER);
        }

        member.setRole(request.getRole());
        companyMemberRepository.save(member);

        return CompanyMemberDto.from(member);
    }

    @Transactional
    public void removeMember(String companyId, String memberId, String requesterId) {
        validateRole(companyId, requesterId, CompanyRole.ADMIN);

        CompanyMember member = companyMemberRepository.findByIdAndCompanyId(memberId, companyId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRole() == CompanyRole.OWNER) {
            throw new ApiException(ErrorCode.CANNOT_MODIFY_OWNER);
        }

        companyMemberRepository.delete(member);
        log.info("Member {} removed from company {}", memberId, companyId);
    }

    @Transactional
    public void leaveCompany(String userId) {
        CompanyMember member = companyMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_A_MEMBER));

        if (member.getRole() == CompanyRole.OWNER) {
            throw new ApiException(ErrorCode.CANNOT_MODIFY_OWNER);
        }

        companyMemberRepository.delete(member);
    }

    // ── Helpers ────────────────────────────────────────────────

    private void validateRole(String companyId, String userId, CompanyRole minimumRole) {
        CompanyMember requester = companyMemberRepository
                .findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_A_MEMBER));

        boolean hasPermission = switch (minimumRole) {
            case OWNER -> requester.getRole() == CompanyRole.OWNER;
            case ADMIN -> requester.getRole() == CompanyRole.OWNER ||
                          requester.getRole() == CompanyRole.ADMIN;
            case MEMBER -> true;
        };

        if (!hasPermission) {
            throw new ApiException(ErrorCode.INSUFFICIENT_PERMISSIONS);
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