package com.smartquote.security;

import com.smartquote.entity.Company;
import com.smartquote.entity.User;
import com.smartquote.exception.ResourceNotFoundException;
import com.smartquote.exception.UnauthorizedException;
import com.smartquote.repository.CompanyRepository;
import com.smartquote.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public SecurityUtils(UserRepository userRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return auth.getName();
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    public Company getCurrentCompany() {
        User user = getCurrentUser();
        return companyRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found for authenticated user"));
    }
}
