package com.cloudlens.api.service;

import com.cloudlens.api.repository.FileMetadataRepository;
import com.cloudlens.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FileMetadataRepository fileMetadataRepository;
    @Mock
    private FileService fileService;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder, fileService, fileMetadataRepository);
    }

    @Test
    void signup_shortUsername_shouldThrow() {
        assertThatThrownBy(() -> userService.signup("ab", "Valid1pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username must be at least 3 characters");
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_shortPassword_shouldThrow() {
        assertThatThrownBy(() -> userService.signup("alice", "Ab1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters");
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_passwordWithoutUppercase_shouldThrow() {
        assertThatThrownBy(() -> userService.signup("alice", "lowercase1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_passwordWithoutLowercase_shouldThrow() {
        assertThatThrownBy(() -> userService.signup("alice", "UPPERCASE1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_passwordWithoutDigit_shouldThrow() {
        assertThatThrownBy(() -> userService.signup("alice", "ValidPass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_duplicateUsername_shouldThrow() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.signup("alice", "Valid1pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already taken");
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_validCredentials_shouldSucceed() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);

        userService.signup("alice", "Valid1pass");

        verify(userRepository).save(argThat(user ->
                "alice".equals(user.getUsername())
                && passwordEncoder.matches("Valid1pass", user.getPassword())
                && "USER".equals(user.getRole())
        ));
    }

    @Test
    void changePassword_shortNewPassword_shouldThrow() {
        assertThatThrownBy(() -> userService.changePassword("alice", "current", "Ab1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password must be at least 8 characters");
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_weakNewPassword_shouldThrow() {
        assertThatThrownBy(() -> userService.changePassword("alice", "current", "nouppercase1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password must contain at least one uppercase letter, one lowercase letter, and one digit");
        verify(userRepository, never()).save(any());
    }
}
