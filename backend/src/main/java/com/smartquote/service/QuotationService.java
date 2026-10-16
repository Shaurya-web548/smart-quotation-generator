package com.smartquote.service;

import com.smartquote.dto.*;
import com.smartquote.entity.*;
import com.smartquote.exception.BadRequestException;
import com.smartquote.exception.ResourceNotFoundException;
import com.smartquote.repository.CustomerRepository;
import com.smartquote.repository.QuotationRepository;
import com.smartquote.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final QuotationCalculationService calculationService;
    private final SecurityUtils securityUtils;

    public QuotationService(
            QuotationRepository quotationRepository,
            CustomerRepository customerRepository,
            QuotationCalculationService calculationService,
            SecurityUtils securityUtils
    ) {
        this.quotationRepository = quotationRepository;
        this.customerRepository = customerRepository;
        this.calculationService = calculationService;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public PageResponse<QuotationResponseDto> getQuotations(
            String search,
            QuotationStatus status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Company company = securityUtils.getCurrentCompany();

        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy != null && !sortBy.isEmpty() ? sortBy : "createdAt"
        );
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        Page<Quotation> quotationsPage = quotationRepository.findByFilters(
                company,
                search,
                status,
                startDate,
                endDate,
                pageable
        );

        return PageResponse.fromPage(quotationsPage.map(QuotationResponseDto::fromEntity));
    }

    @Transactional(readOnly = true)
    public QuotationResponseDto getQuotationById(Long id) {
        Quotation quotation = getAuthorizedQuotation(id);
        return QuotationResponseDto.fromEntity(quotation);
    }

    @Transactional
    public QuotationResponseDto createQuotation(QuotationCreateRequest request) {
        Company company = securityUtils.getCurrentCompany();

        // 1. Calculate totals on the backend using BigDecimal
        CalculationResult calc = calculationService.calculate(
                request.getItems(),
                request.getDiscountPercentage(),
                request.getTaxPercentage() != null ? request.getTaxPercentage() : company.getDefaultTaxRate()
        );

        // 2. Generate unique quotation number safely
        String quotationNumber = generateNextQuotationNumber(company);

        // 3. Resolve customer if provided
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByIdAndCompany(request.getCustomerId(), company)
                    .orElse(null);
        }

        Quotation quotation = new Quotation();
        quotation.setCompany(company);
        quotation.setQuotationNumber(quotationNumber);
        quotation.setCustomer(customer);
        quotation.setCustomerName(request.getCustomerName().trim());
        quotation.setCustomerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail().trim() : null);
        quotation.setCustomerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone().trim() : null);
        quotation.setCustomerAddress(request.getCustomerAddress());
        quotation.setStatus(request.getStatus() != null ? request.getStatus() : QuotationStatus.DRAFT);
        quotation.setIssueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now());
        quotation.setExpiryDate(request.getExpiryDate() != null ? request.getExpiryDate() : LocalDate.now().plusDays(company.getDefaultValidityDays()));

        quotation.setSubtotal(calc.getSubtotal());
        quotation.setDiscountPercentage(calc.getDiscountPercentage());
        quotation.setDiscountAmount(calc.getDiscountAmount());
        quotation.setTaxableAmount(calc.getTaxableAmount());
        quotation.setTaxPercentage(calc.getTaxPercentage());
        quotation.setTaxAmount(calc.getTaxAmount());
        quotation.setTotalAmount(calc.getTotalAmount());
        quotation.setNotes(request.getNotes());
        quotation.setTerms(request.getTerms() != null ? request.getTerms() : company.getDefaultTerms());

        for (CalculationResult.CalculatedItem it : calc.getItems()) {
            QuotationItem item = new QuotationItem(
                    it.getDescription(),
                    it.getQuantity(),
                    it.getUnitPrice(),
                    it.getAmount(),
                    it.getSortOrder()
            );
            quotation.addItem(item);
        }

        Quotation saved = quotationRepository.save(quotation);
        return QuotationResponseDto.fromEntity(saved);
    }

    @Transactional
    public QuotationResponseDto updateQuotation(Long id, QuotationCreateRequest request) {
        Quotation quotation = getAuthorizedQuotation(id);

        if (quotation.getStatus() != QuotationStatus.DRAFT && quotation.getStatus() != QuotationStatus.SENT) {
            throw new BadRequestException("Only DRAFT or SENT quotations can be modified");
        }

        CalculationResult calc = calculationService.calculate(
                request.getItems(),
                request.getDiscountPercentage(),
                request.getTaxPercentage()
        );

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findByIdAndCompany(request.getCustomerId(), quotation.getCompany())
                    .orElse(null);
            quotation.setCustomer(customer);
        }

        quotation.setCustomerName(request.getCustomerName().trim());
        quotation.setCustomerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail().trim() : null);
        quotation.setCustomerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone().trim() : null);
        quotation.setCustomerAddress(request.getCustomerAddress());
        quotation.setIssueDate(request.getIssueDate());
        quotation.setExpiryDate(request.getExpiryDate());

        quotation.setSubtotal(calc.getSubtotal());
        quotation.setDiscountPercentage(calc.getDiscountPercentage());
        quotation.setDiscountAmount(calc.getDiscountAmount());
        quotation.setTaxableAmount(calc.getTaxableAmount());
        quotation.setTaxPercentage(calc.getTaxPercentage());
        quotation.setTaxAmount(calc.getTaxAmount());
        quotation.setTotalAmount(calc.getTotalAmount());
        quotation.setNotes(request.getNotes());
        quotation.setTerms(request.getTerms());

        quotation.clearItems();
        for (CalculationResult.CalculatedItem it : calc.getItems()) {
            QuotationItem item = new QuotationItem(
                    it.getDescription(),
                    it.getQuantity(),
                    it.getUnitPrice(),
                    it.getAmount(),
                    it.getSortOrder()
            );
            quotation.addItem(item);
        }

        Quotation updated = quotationRepository.save(quotation);
        return QuotationResponseDto.fromEntity(updated);
    }

    @Transactional
    public QuotationResponseDto updateStatus(Long id, QuotationStatus newStatus) {
        Quotation quotation = getAuthorizedQuotation(id);

        if (!quotation.getStatus().canTransitionTo(newStatus)) {
            throw new BadRequestException("Cannot transition quotation status from " +
                    quotation.getStatus() + " to " + newStatus);
        }

        quotation.setStatus(newStatus);
        Quotation updated = quotationRepository.save(quotation);
        return QuotationResponseDto.fromEntity(updated);
    }

    @Transactional
    public QuotationResponseDto duplicateQuotation(Long id) {
        Quotation original = getAuthorizedQuotation(id);
        Company company = original.getCompany();

        String nextNumber = generateNextQuotationNumber(company);

        Quotation duplicate = new Quotation();
        duplicate.setCompany(company);
        duplicate.setQuotationNumber(nextNumber);
        duplicate.setCustomer(original.getCustomer());
        duplicate.setCustomerName(original.getCustomerName());
        duplicate.setCustomerEmail(original.getCustomerEmail());
        duplicate.setCustomerPhone(original.getCustomerPhone());
        duplicate.setCustomerAddress(original.getCustomerAddress());
        duplicate.setStatus(QuotationStatus.DRAFT);
        duplicate.setIssueDate(LocalDate.now());
        duplicate.setExpiryDate(LocalDate.now().plusDays(company.getDefaultValidityDays()));

        duplicate.setSubtotal(original.getSubtotal());
        duplicate.setDiscountPercentage(original.getDiscountPercentage());
        duplicate.setDiscountAmount(original.getDiscountAmount());
        duplicate.setTaxableAmount(original.getTaxableAmount());
        duplicate.setTaxPercentage(original.getTaxPercentage());
        duplicate.setTaxAmount(original.getTaxAmount());
        duplicate.setTotalAmount(original.getTotalAmount());
        duplicate.setNotes(original.getNotes());
        duplicate.setTerms(original.getTerms());

        for (QuotationItem it : original.getItems()) {
            QuotationItem newItem = new QuotationItem(
                    it.getDescription(),
                    it.getQuantity(),
                    it.getUnitPrice(),
                    it.getAmount(),
                    it.getSortOrder()
            );
            duplicate.addItem(newItem);
        }

        Quotation saved = quotationRepository.save(duplicate);
        return QuotationResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Quotation getAuthorizedQuotation(Long id) {
        Company company = securityUtils.getCurrentCompany();
        return quotationRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));
    }

    private synchronized String generateNextQuotationNumber(Company company) {
        List<String> latest = quotationRepository.findLatestQuotationNumbers(company, PageRequest.of(0, 10));
        int maxSeq = 1000;
        for (String num : latest) {
            if (num != null && num.startsWith("Q-")) {
                try {
                    int seq = Integer.parseInt(num.substring(2));
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "Q-" + (maxSeq + 1);
    }
}
