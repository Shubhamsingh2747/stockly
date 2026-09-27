package com.stockly.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stockly.auth.dto.AdminCreateUserRequest;
import com.stockly.auth.dto.UpdateUserRequest;
import com.stockly.common.exception.BusinessException;
import com.stockly.common.exception.ForbiddenException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAdminService userAdminService;

    private UserAccount admin;

    @BeforeEach
    void setUp() {
        admin = new UserAccount();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setEmail("admin@stockly.local");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
    }

    @Test
    void createPersistsEnabledUser() {
        when(userRepository.existsByEmailIgnoreCase("new@stockly.local")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("clerk")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount saved = invocation.getArgument(0);
            saved.setId(9L);
            return saved;
        });

        var response = userAdminService.create(
                new AdminCreateUserRequest("clerk", "new@stockly.local", "Secret123", Role.USER));

        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.enabled()).isTrue();
        verify(userRepository).save(any(UserAccount.class));
    }

    @Test
    void cannotDisableOwnAccount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        assertThatThrownBy(() -> userAdminService.update(1L, new UpdateUserRequest(null, false), admin))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("your own account");
    }

    @Test
    void cannotDisableLastAdmin() {
        UserAccount otherAdmin = new UserAccount();
        otherAdmin.setId(2L);
        otherAdmin.setUsername("boss");
        otherAdmin.setEmail("boss@stockly.local");
        otherAdmin.setRole(Role.ADMIN);
        otherAdmin.setEnabled(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherAdmin));
        when(userRepository.countByRoleAndEnabledTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userAdminService.update(2L, new UpdateUserRequest(null, false), admin))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("last active administrator");
    }

    @Test
    void canAssignViewerRole() {
        UserAccount clerk = new UserAccount();
        clerk.setId(3L);
        clerk.setUsername("clerk");
        clerk.setEmail("clerk@stockly.local");
        clerk.setRole(Role.USER);
        clerk.setEnabled(true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(clerk));

        var response = userAdminService.update(3L, new UpdateUserRequest(Role.VIEWER, null), admin);

        assertThat(response.role()).isEqualTo(Role.VIEWER);
    }
}
