package com.authsystemjava.backend.repository;

import com.authsystemjava.backend.model.CompanyMember;
import com.authsystemjava.backend.model.CompanyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, String> {
    List<CompanyMember> findAllByCompanyId(String companyId);
    Optional<CompanyMember> findByCompanyIdAndUserId(String companyId, String userId);
    Optional<CompanyMember> findByUserId(String userId);
    boolean existsByCompanyIdAndUserId(String companyId, String userId);

    @Transactional
    void deleteByCompanyIdAndUserId(String companyId, String userId);
}