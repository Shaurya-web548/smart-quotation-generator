package com.smartquote.service;

import com.smartquote.dto.CompanyDto;
import com.smartquote.dto.CompanyUpdateRequest;
import com.smartquote.entity.Company;
import com.smartquote.exception.BadRequestException;
import com.smartquote.repository.CompanyRepository;
import com.smartquote.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final SecurityUtils securityUtils;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/webp",
            "image/svg+xml"
    );
    private static final long MAX_LOGO_SIZE = 2 * 1024 * 1024; // 2MB

    public CompanyService(CompanyRepository companyRepository, SecurityUtils securityUtils) {
        this.companyRepository = companyRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public CompanyDto getCompanySettings() {
        Company company = securityUtils.getCurrentCompany();
        return CompanyDto.fromEntity(company);
    }

    @Transactional
    public CompanyDto updateCompanySettings(CompanyUpdateRequest request) {
        Company company = securityUtils.getCurrentCompany();

        company.setName(request.getName().trim());
        company.setAddress(request.getAddress());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());
        company.setTaxId(request.getTaxId());
        company.setCurrency(request.getCurrency().trim().toUpperCase());
        company.setCurrencySymbol(request.getCurrencySymbol().trim());
        company.setDefaultTaxRate(request.getDefaultTaxRate());
        company.setDefaultValidityDays(request.getDefaultValidityDays());
        company.setDefaultTerms(request.getDefaultTerms());

        if (request.getLogoUrl() != null) {
            company.setLogoUrl(request.getLogoUrl());
        }

        Company updated = companyRepository.save(company);
        return CompanyDto.fromEntity(updated);
    }

    @Transactional
    public CompanyDto uploadLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Logo file is required");
        }

        if (file.getSize() > MAX_LOGO_SIZE) {
            throw new BadRequestException("Logo file exceeds maximum size of 2MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported image format. Allowed: PNG, JPEG, WEBP, SVG");
        }

        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String dataUrl = "data:" + contentType + ";base64," + base64;

            Company company = securityUtils.getCurrentCompany();
            company.setLogoUrl(dataUrl);
            Company updated = companyRepository.save(company);
            return CompanyDto.fromEntity(updated);
        } catch (IOException e) {
            throw new BadRequestException("Failed to read uploaded logo file: " + e.getMessage());
        }
    }
}

