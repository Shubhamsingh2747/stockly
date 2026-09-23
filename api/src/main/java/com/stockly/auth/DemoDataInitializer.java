package com.stockly.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailIgnoreCase("admin@stockly.local")) {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setEmail("admin@stockly.local");
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Seeded demo admin user admin@stockly.local / Admin@123");
    }
}
