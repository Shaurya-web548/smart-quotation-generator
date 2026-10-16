package com.smartquote.service;

import com.smartquote.dto.DashboardSummaryDto;
import com.smartquote.dto.QuotationResponseDto;
import com.smartquote.entity.Company;
import com.smartquote.entity.Quotation;
import com.smartquote.entity.QuotationStatus;
import com.smartquote.repository.QuotationRepository;
import com.smartquote.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final QuotationRepository quotationRepository;
    private final SecurityUtils securityUtils;

    public DashboardService(QuotationRepository quotationRepository, SecurityUtils securityUtils) {
        this.quotationRepository = quotationRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary() {
        Company company = securityUtils.getCurrentCompany();

        long total = quotationRepository.countByCompany(company);
        long drafts = quotationRepository.countByCompanyAndStatus(company, QuotationStatus.DRAFT);
        long sent = quotationRepository.countByCompanyAndStatus(company, QuotationStatus.SENT);
        long accepted = quotationRepository.countByCompanyAndStatus(company, QuotationStatus.ACCEPTED);
        long rejected = quotationRepository.countByCompanyAndStatus(company, QuotationStatus.REJECTED);
        long expired = quotationRepository.countByCompanyAndStatus(company, QuotationStatus.EXPIRED);

        List<Quotation> recent = quotationRepository.findTop5ByCompanyOrderByCreatedAtDesc(company);

        BigDecimal totalVal = BigDecimal.ZERO;
        BigDecimal acceptedVal = BigDecimal.ZERO;

        for (Quotation q : quotationRepository.findAll()) {
            if (q.getCompany().getId().equals(company.getId())) {
                totalVal = totalVal.add(q.getTotalAmount());
                if (q.getStatus() == QuotationStatus.ACCEPTED) {
                    acceptedVal = acceptedVal.add(q.getTotalAmount());
                }
            }
        }

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setTotalQuotations(total);
        dto.setDraftQuotations(drafts);
        dto.setSentQuotations(sent);
        dto.setAcceptedQuotations(accepted);
        dto.setRejectedQuotations(rejected);
        dto.setExpiredQuotations(expired);
        dto.setTotalValue(totalVal);
        dto.setAcceptedValue(acceptedVal);
        dto.setRecentQuotations(recent.stream().map(QuotationResponseDto::fromEntity).collect(Collectors.toList()));

        return dto;
    }
}
