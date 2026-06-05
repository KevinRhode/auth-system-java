package com.authsystemjava.backend.repository;

import com.authsystemjava.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String> {
    Optional<Company> findBySlug(String slug);
    boolean existsBySlug(String slug);
}