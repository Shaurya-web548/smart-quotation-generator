package com.smartquote.repository;

import com.smartquote.entity.Company;
import com.smartquote.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUser(User user);
    Optional<Company> findByUserId(Long userId);
}
