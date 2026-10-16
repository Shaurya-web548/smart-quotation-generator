package com.smartquote.controller;

import com.smartquote.dto.ApiResponse;
import com.smartquote.dto.CustomerCreateRequest;
import com.smartquote.dto.CustomerDto;
import com.smartquote.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer address book and directory for quotations")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "List all customers belonging to the tenant business")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> listCustomers() {
        return ResponseEntity.ok(ApiResponse.success(customerService.listCustomers()));
    }

    @PostMapping
    @Operation(summary = "Create a new customer contact")
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        CustomerDto created = customerService.createCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("Customer created successfully", created));
    }
}
