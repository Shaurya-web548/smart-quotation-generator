package com.smartquote.service;

import com.smartquote.dto.CalculationResult;
import com.smartquote.dto.QuotationCreateRequest;
import com.smartquote.dto.QuotationItemDto;
import com.smartquote.dto.QuotationResponseDto;
import com.smartquote.entity.*;
import com.smartquote.exception.BadRequestException;
import com.smartquote.exception.ResourceNotFoundException;
import com.smartquote.repository.CustomerRepository;
import com.smartquote.repository.QuotationRepository;
import com.smartquote.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private QuotationCalculationService calculationService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private QuotationService quotationService;

    private Company companyA;
    private Company companyB;
    private Quotation quotationA;

    @BeforeEach
    void setUp() {
        User userA = new User("user_a@example.com", "hash", "User A", Role.USER);
        userA.setId(1L);
        companyA = new Company(userA, "Company Alpha");
        companyA.setId(10L);
        userA.setCompany(companyA);

        User userB = new User("user_b@example.com", "hash", "User B", Role.USER);
        userB.setId(2L);
        companyB = new Company(userB, "Company Beta");
        companyB.setId(20L);
        userB.setCompany(companyB);

        quotationA = new Quotation();
        quotationA.setId(100L);
        quotationA.setQuotationNumber("Q-1001");
        quotationA.setCompany(companyA);
        quotationA.setCustomerName("Acme Corp");
        quotationA.setStatus(QuotationStatus.DRAFT);
        quotationA.setIssueDate(LocalDate.now());
        quotationA.setExpiryDate(LocalDate.now().plusDays(30));
        quotationA.setSubtotal(new BigDecimal("100.00"));
        quotationA.setDiscountPercentage(BigDecimal.ZERO);
        quotationA.setDiscountAmount(BigDecimal.ZERO);
        quotationA.setTaxableAmount(new BigDecimal("100.00"));
        quotationA.setTaxPercentage(new BigDecimal("18.00"));
        quotationA.setTaxAmount(new BigDecimal("18.00"));
        quotationA.setTotalAmount(new BigDecimal("118.00"));
    }

    @Test
    @DisplayName("Tenant Isolation: Should throw ResourceNotFoundException when user from Company B requests quotation of Company A")
    void testTenantIsolationCrossAccessBlocked() {
        // Authenticated user belongs to Company B
        when(securityUtils.getCurrentCompany()).thenReturn(companyB);
        // Repository filters by companyB, which returns Optional.empty()
        when(quotationRepository.findByIdAndCompany(100L, companyB)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                quotationService.getQuotationById(100L)
        );
    }

    @Test
    @DisplayName("Lifecycle: Draft should transition to Sent, but Accepted should reject transition")
    void testStatusTransitions() {
        when(securityUtils.getCurrentCompany()).thenReturn(companyA);
        when(quotationRepository.findByIdAndCompany(100L, companyA)).thenReturn(Optional.of(quotationA));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> i.getArgument(0));

        // DRAFT -> SENT should succeed
        QuotationResponseDto sent = quotationService.updateStatus(100L, QuotationStatus.SENT);
        assertEquals(QuotationStatus.SENT, sent.getStatus());

        // SENT -> ACCEPTED should succeed
        quotationA.setStatus(QuotationStatus.SENT);
        QuotationResponseDto accepted = quotationService.updateStatus(100L, QuotationStatus.ACCEPTED);
        assertEquals(QuotationStatus.ACCEPTED, accepted.getStatus());

        // ACCEPTED -> DRAFT should fail (finalized state)
        quotationA.setStatus(QuotationStatus.ACCEPTED);
        assertThrows(BadRequestException.class, () ->
                quotationService.updateStatus(100L, QuotationStatus.DRAFT)
        );
    }

    @Test
    @DisplayName("Duplication: Should clone quotation with a new draft number and current date")
    void testDuplicateQuotation() {
        when(securityUtils.getCurrentCompany()).thenReturn(companyA);
        when(quotationRepository.findByIdAndCompany(100L, companyA)).thenReturn(Optional.of(quotationA));
        when(quotationRepository.findLatestQuotationNumbers(eq(companyA), any())).thenReturn(List.of("Q-1001"));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> {
            Quotation q = i.getArgument(0);
            q.setId(101L);
            return q;
        });

        QuotationResponseDto duplicate = quotationService.duplicateQuotation(100L);
        assertNotNull(duplicate);
        assertEquals("Q-1002", duplicate.getQuotationNumber());
        assertEquals(QuotationStatus.DRAFT, duplicate.getStatus());
        assertEquals(quotationA.getCustomerName(), duplicate.getCustomerName());
        assertEquals(quotationA.getTotalAmount(), duplicate.getTotalAmount());
    }
}

