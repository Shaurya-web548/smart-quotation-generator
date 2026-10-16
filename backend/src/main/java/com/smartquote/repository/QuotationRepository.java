package com.smartquote.repository;

import com.smartquote.entity.Company;
import com.smartquote.entity.Quotation;
import com.smartquote.entity.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByIdAndCompany(Long id, Company company);

    @Query("SELECT q FROM Quotation q WHERE q.company = :company " +
           "AND (:search IS NULL OR :search = '' OR LOWER(q.quotationNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(q.customerName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR q.status = :status) " +
           "AND (:startDate IS NULL OR q.issueDate >= :startDate) " +
           "AND (:endDate IS NULL OR q.issueDate <= :endDate)")
    Page<Quotation> findByFilters(
            @Param("company") Company company,
            @Param("search") String search,
            @Param("status") QuotationStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    long countByCompany(Company company);
    long countByCompanyAndStatus(Company company, QuotationStatus status);

    List<Quotation> findTop5ByCompanyOrderByCreatedAtDesc(Company company);

    @Query("SELECT q.quotationNumber FROM Quotation q WHERE q.company = :company ORDER BY q.id DESC")
    List<String> findLatestQuotationNumbers(@Param("company") Company company, Pageable pageable);
}

