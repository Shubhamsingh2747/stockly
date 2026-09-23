package com.stockly.auth;

import com.stockly.auth.dto.AuthResponse;
import com.stockly.auth.dto.LoginRequest;
import com.stockly.auth.dto.RegisterRequest;
import com.stockly.auth.dto.UserResponse;
import com.stockly.common.exception.ConflictException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ConflictException("Username is already taken");
        }
        UserAccount user = new UserAccount();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return AuthResponse.bearer(jwtService.createToken(user), toResponse(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("bad credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("bad credentials");
        }
        return AuthResponse.bearer(jwtService.createToken(user), toResponse(user));
    }

    public static UserResponse toResponse(UserAccount user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
