package com.tonip.security;

import com.tonip.security.domain.Role;
import com.tonip.security.domain.User;
import com.tonip.security.domain.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedIfMissing("Admin", "admin123", Role.ADMIN);
        seedIfMissing("Super", "super123", Role.SUPER);
        seedIfMissing("User", "user123", Role.USER);
    }

    private void seedIfMissing(String username, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        userRepository.save(new User(username, passwordEncoder.encode(rawPassword), role));
    }
}