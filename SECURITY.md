# Security Architecture & Policies

This document details the security principles, threat mitigation strategies, and architectural boundaries implemented in **Smart Quotation Generator**.

---

## 1. Multi-Tenant Isolation & Authorization

Every business account is an isolated tenant.
- **Tenant Scope Enforcement**: Data models (`Customer`, `Quotation`, `QuotationItem`) are tied to a `Company`.
- **Identity Derivation**: The server never trusts client-supplied tenant identifiers (`companyId` or `userId` in path or body). The authenticated identity is extracted directly from the verified cryptographic JWT bearer token in `SecurityUtils`.
- **Ownership Verification**: All repository queries for modifying or reading resources explicitly enforce `findByIdAndCompany(id, company)`. Cross-tenant queries return a `404 Not Found` to prevent account enumeration.

---

## 2. Authentication & Credential Storage

- **Password Hashing**: Passwords are never stored in plaintext. Passwords are salted and hashed using Spring Security's `BCryptPasswordEncoder` with adaptive work factor.
- **Stateless JWTs**: Tokens are cryptographically signed using HMAC-SHA256 (`Keys.hmacShaKeyFor`) with a minimum 256-bit secret.
- **Session Termination**: The client discards the token upon sign-out. The backend responds with standard token clearance.

---

## 3. Input Validation & Calculation Integrity

- **Zero Client Trust**: Frontend calculation results are treated as non-authoritative display hints only. All subtotal, discount, taxable, GST, and final totals are calculated strictly on the backend using `BigDecimal` with explicit `RoundingMode.HALF_UP`.
- **Bean Validation**: All API request DTOs are validated using Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@DecimalMin`, `@DecimalMax`).
- **Boundary Checks**: Negative unit prices, non-positive quantities, and invalid percentages (>100% or <0%) are rejected with `400 Bad Request`.

---

## 4. Injection & Cross-Site Attack Defenses

- **SQL Injection**: Prevented through Hibernate / Spring Data JPA parameterized queries and typed query methods. No dynamic string concatenation is used in SQL or HQL queries.
- **CORS Protection**: Restricted to explicitly configured domains via `app.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS`). Wildcard origins (`*`) are disallowed in production.
- **CSRF**: As the API is strictly stateless and relies on `Authorization: Bearer <token>` headers (rather than ambient browser session cookies), standard cookie-based CSRF attacks are mitigated by design.

---

## 5. File Upload Security

- **Strict Content-Type Whitelisting**: Logo uploads accept only verified MIME types (`image/png`, `image/jpeg`, `image/webp`, `image/svg+xml`).
- **Size Limits**: Strict 2MB file size ceiling per image to prevent Denial-of-Service (DoS) and memory exhaustion.
- **No Executable Storage**: Uploaded images are stored as validated Base64 data URIs or in a documented cloud storage bucket. No raw executable binaries can be uploaded or executed on the host.

---

## 6. Information Disclosure & Error Handling

- **Global Exception Handler**: `GlobalExceptionHandler` intercepts exceptions and renders consistent, sanitize JSON error envelopes (`ApiResponse<T>`).
- **No Stack Traces**: Internal exception traces, SQL dialect details, and class hierarchies are logged server-side and never returned in HTTP responses.
