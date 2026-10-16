package com.smartquote.repository;

import com.smartquote.entity.Company;
import com.smartquote.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByCompanyOrderByNameAsc(Company company);
    Page<Customer> findByCompany(Company company, Pageable pageable);
    Optional<Customer> findByIdAndCompany(Long id, Company company);
    boolean existsByCompanyAndNameIgnoreCase(Company company, String name);
}
