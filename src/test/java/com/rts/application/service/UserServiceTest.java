package com.rts.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rts.application.entity.User;
import com.rts.application.repository.UserRepository;
import com.rts.application.service.UserService.InvalidCredentialsException;
import com.rts.application.service.UserService.UsernameAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void signUpHashesPasswordAndSavesUser() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.signUp("alice", "plain-password");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(result).isSameAs(saved);
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getPassword()).isEqualTo("hashed-password");
        assertThat(saved.getCreationDate()).isEqualTo(LocalDate.now());
        verify(passwordEncoder).encode("plain-password");
    }

    @Test
    void signUpRejectsExistingUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.signUp("alice", "plain-password"))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username already exists: alice");
    }

    @Test
    void loginReturnsUserForMatchingPassword() {
        User user = new User("alice", "hashed-password", LocalDate.now());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);

        assertThat(userService.login("alice", "plain-password")).isSameAs(user);
        verify(passwordEncoder).matches("plain-password", "hashed-password");
    }

    @Test
    void loginRejectsUnknownUserOrWrongPassword() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.login("missing", "password"))
                .isInstanceOf(InvalidCredentialsException.class);

        User user = new User("alice", "hashed-password", LocalDate.now());
        when(userRepository.findByUsername(eq("alice"))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);
        assertThatThrownBy(() -> userService.login("alice", "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
