package com.alijah.martial_arts_app.controllers;

import com.alijah.martial_arts_app.models.User;
import com.alijah.martial_arts_app.repositories.TechniqueRepository;
import com.alijah.martial_arts_app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Unit tests for the hardened login (BCrypt verification + 401 on bad/unknown credentials). */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserRepository userRepo;
    @Mock
    TechniqueRepository techRepo;
    @InjectMocks
    UserController userController;

    private User storedUser(String username, String rawPassword) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
        return u;
    }

    private User credentials(String username, String rawPassword) {
        User c = new User();
        c.setUsername(username);
        c.setPassword(rawPassword);
        return c;
    }

    @Test
    void login_returnsOk_forValidCredentials() {
        when(userRepo.findById("bob")).thenReturn(Optional.of(storedUser("bob", "secret")));
        ResponseEntity<User> resp = userController.login(credentials("bob", "secret"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void login_returnsUnauthorized_forWrongPassword() {
        when(userRepo.findById("bob")).thenReturn(Optional.of(storedUser("bob", "secret")));
        ResponseEntity<User> resp = userController.login(credentials("bob", "wrong"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_returnsUnauthorized_forUnknownUser() {
        when(userRepo.findById("nobody")).thenReturn(Optional.empty());
        ResponseEntity<User> resp = userController.login(credentials("nobody", "x"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
