package com.stockly.auth;

import com.stockly.auth.dto.AdminCreateUserRequest;
import com.stockly.auth.dto.ResetPasswordRequest;
import com.stockly.auth.dto.UpdateUserRequest;
import com.stockly.auth.dto.UserResponse;
import com.stockly.common.exception.BusinessException;
import com.stockly.common.exception.ConflictException;
import com.stockly.common.exception.ForbiddenException;
import com.stockly.common.exception.NotFoundException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(AuthService::toResponse).toList();
    }

    @Transactional
    public UserResponse create(AdminCreateUserRequest request) {
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
        user.setRole(request.role());
        user.setEnabled(true);
        return AuthService.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, UserAccount actor) {
        UserAccount user = getUser(id);
        if (request.role() != null && request.role() != user.getRole()) {
            ensureNotLastEnabledAdmin(user, actor, "Cannot change the role of the last active administrator");
            user.setRole(request.role());
        }
        if (request.enabled() != null && request.enabled() != user.isEnabled()) {
            if (user.getId().equals(actor.getId())) {
                throw new ForbiddenException("You cannot disable your own account");
            }
            if (!request.enabled()) {
                ensureNotLastEnabledAdmin(user, actor, "Cannot disable the last active administrator");
            }
            user.setEnabled(request.enabled());
        }
        return AuthService.toResponse(user);
    }

    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        UserAccount user = getUser(id);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
    }

    private UserAccount getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    private void ensureNotLastEnabledAdmin(UserAccount target, UserAccount actor, String message) {
        if (target.getRole() != Role.ADMIN || !target.isEnabled()) {
            return;
        }
        if (userRepository.countByRoleAndEnabledTrue(Role.ADMIN) <= 1) {
            throw new BusinessException(message);
        }
        if (target.getId().equals(actor.getId()) && actor.getRole() == Role.ADMIN) {
            throw new ForbiddenException("You cannot remove your own administrator access");
        }
    }
}
