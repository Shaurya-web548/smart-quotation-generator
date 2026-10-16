package com.smartquote.service;

import com.smartquote.dto.AuthRequest;
import com.smartquote.dto.AuthResponse;
import com.smartquote.dto.CompanyDto;
import com.smartquote.dto.UserDto;
import com.smartquote.entity.Company;
import com.smartquote.entity.Role;
import com.smartquote.entity.User;
import com.smartquote.exception.BadRequestException;
import com.smartquote.repository.CompanyRepository;
import com.smartquote.repository.UserRepository;
import com.smartquote.security.JwtUtil;
import com.smartquote.security.SecurityUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SecurityUtils securityUtils;

    public AuthService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            SecurityUtils securityUtils
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public AuthResponse register(AuthRequest.Register request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User(
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                Role.USER
        );
        User savedUser = userRepository.save(user);

        Company company = new Company(savedUser, request.getCompanyName().trim());
        company.setEmail(savedUser.getEmail());
        Company savedCompany = companyRepository.save(company);
        savedUser.setCompany(savedCompany);

        String token = jwtUtil.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedCompany.getId(),
                savedUser.getRole().name()
        );

        return new AuthResponse(token, UserDto.fromEntity(savedUser), CompanyDto.fromEntity(savedCompany));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest.Login request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Company profile not found for user"));

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                company.getId(),
                user.getRole().name()
        );

        return new AuthResponse(token, UserDto.fromEntity(user), CompanyDto.fromEntity(company));
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser() {
        User user = securityUtils.getCurrentUser();
        Company company = securityUtils.getCurrentCompany();

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                company.getId(),
                user.getRole().name()
        );

        return new AuthResponse(token, UserDto.fromEntity(user), CompanyDto.fromEntity(company));
    }
}

