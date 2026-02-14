package com.routemaster.config;

import com.routemaster.model.User;
import com.routemaster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("Initializing demo users...");

                // Create admin user
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@routemaster.com");
                admin.setFullName("Admin User");
                admin.setRoles(Set.of(User.Role.ROLE_ADMIN));
                admin.setEnabled(true);
                userRepository.save(admin);
                log.info("Created admin user: admin/admin123");

                // Create driver user
                User driver = new User();
                driver.setUsername("driver");
                driver.setPassword(passwordEncoder.encode("driver123"));
                driver.setEmail("driver@routemaster.com");
                driver.setFullName("Driver User");
                driver.setRoles(Set.of(User.Role.ROLE_DRIVER));
                driver.setEnabled(true);
                userRepository.save(driver);
                log.info("Created driver user: driver/driver123");

                // Create viewer user
                User viewer = new User();
                viewer.setUsername("viewer");
                viewer.setPassword(passwordEncoder.encode("viewer123"));
                viewer.setEmail("viewer@routemaster.com");
                viewer.setFullName("Viewer User");
                viewer.setRoles(Set.of(User.Role.ROLE_VIEWER));
                viewer.setEnabled(true);
                userRepository.save(viewer);
                log.info("Created viewer user: viewer/viewer123");

                log.info("Demo users initialized successfully!");
            } else {
                log.info("Users already exist in database. Skipping initialization.");
            }
        };
    }
}
