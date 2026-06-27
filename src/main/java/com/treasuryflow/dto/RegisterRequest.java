package com.treasuryflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RegisterRequest - DTO for user registration endpoint
 *
 * VALIDATION ANNOTATIONS:
 * - @NotBlank: Field must not be null, empty, or whitespace only
 * - @Size: String length constraints (min, max)
 * - @Email: Valid email format validation
 *
 * These work with @Valid in controller + spring-boot-starter-validation
 * Spring automatically returns 400 Bad Request with error details if validation fails
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;
}