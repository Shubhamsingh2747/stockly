package com.stockly.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 32)
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9._-]*$", message = "must start with a letter and use only letters, digits, dots, underscores, or hyphens")
        String username,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 72) String password) {
}
