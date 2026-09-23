package com.stockly.auth.dto;

import com.stockly.auth.Role;

public record UserResponse(Long id, String email, Role role) {
}
