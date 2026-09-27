package com.stockly.auth.dto;

import com.stockly.auth.Role;
import java.time.Instant;

public record UserResponse(Long id, String username, String email, Role role, boolean enabled, Instant createdAt) {
}
