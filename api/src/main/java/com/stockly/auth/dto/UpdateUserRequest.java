package com.stockly.auth.dto;

import com.stockly.auth.Role;

public record UpdateUserRequest(Role role, Boolean enabled) {
}
