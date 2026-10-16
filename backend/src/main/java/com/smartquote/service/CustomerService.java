package com.smartquote.service;

import com.smartquote.dto.CustomerCreateRequest;
import com.smartquote.dto.CustomerDto;
import com.smartquote.entity.Company;
import com.smartquote.entity.Customer;
import com.smartquote.exception.BadRequestException;
import com.smartquote.exception.ResourceNotFoundException;
import com.smartquote.repository.CustomerRepository;
import com.smartquote.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SecurityUtils securityUtils;

    public CustomerService(CustomerRepository customerRepository, SecurityUtils securityUtils) {
        this.customerRepository = customerRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> listCustomers() {
        Company company = securityUtils.getCurrentCompany();
        return customerRepository.findByCompanyOrderByNameAsc(company)
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerDto createCustomer(CustomerCreateRequest request) {
        Company company = securityUtils.getCurrentCompany();

        Customer customer = new Customer(
                company,
                request.getName().trim(),
                request.getEmail() != null ? request.getEmail().trim() : null,
                request.getPhone() != null ? request.getPhone().trim() : null,
                request.getAddress(),
                request.getTaxId() != null ? request.getTaxId().trim() : null
        );

        Customer saved = customerRepository.save(customer);
        return CustomerDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerEntity(Long id, Company company) {
        return customerRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }
}

