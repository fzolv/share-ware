package com.fzolv.shareware.hull.main;

import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.entities.UserRole;
import com.fzolv.shareware.data.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdminRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final BootstrapAdminProperties properties;
    private final UserRepository userRepository;

    public BootstrapAdminRunner(BootstrapAdminProperties properties, UserRepository userRepository) {
        this.properties = properties;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (properties.getEmails() == null || properties.getEmails().isEmpty()) {
            return;
        }
        for (String email : properties.getEmails()) {
            if (email == null || email.isBlank()) {
                continue;
            }
            userRepository.findByEmail(email).ifPresentOrElse(user -> {
                if (user.getRole() != UserRole.ADMIN) {
                    user.setRole(UserRole.ADMIN);
                    userRepository.save(user);
                    log.info("Promoted existing user to ADMIN: {}", email);
                }
            }, () -> {
                UserEntity user = new UserEntity();
                user.setEmail(email);
                user.setName(email);
                user.setRole(UserRole.ADMIN);
                userRepository.save(user);
                log.info("Created bootstrap ADMIN user: {}", email);
            });
        }
    }
}


