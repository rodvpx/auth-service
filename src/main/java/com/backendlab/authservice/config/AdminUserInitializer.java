package com.backendlab.authservice.config;

import com.backendlab.authservice.entity.Role;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Cria o administrador inicial a partir de ADMIN_EMAIL e ADMIN_PASSWORD.
 *
 * A migração V2 antiga inseria um admin com hash fixo no repositório. A V3 remove
 * esse registro e este runner passa a ser a única forma de criar o admin inicial.
 */
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final boolean required;

    public AdminUserInitializer(UserRepository repository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.admin.email:}") String adminEmail,
                                @Value("${app.admin.password:}") String adminPassword,
                                @Value("${app.admin.required:false}") boolean required) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.required = required;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean hasEmail = StringUtils.hasText(adminEmail);
        boolean hasPassword = StringUtils.hasText(adminPassword);

        if (!hasEmail && !hasPassword) {
            if (required) {
                throw new IllegalStateException(
                        "ADMIN_EMAIL e ADMIN_PASSWORD são obrigatórios no perfil de produção.");
            }
            log.info("ADMIN_EMAIL e ADMIN_PASSWORD não configurados; admin inicial não será criado.");
            return;
        }

        if (!hasEmail || !hasPassword) {
            throw new IllegalStateException(
                    "ADMIN_EMAIL e ADMIN_PASSWORD devem ser definidos juntos.");
        }

        if (repository.existsByEmail(adminEmail)) {
            log.info("Usuário com e-mail {} já existe; admin inicial não será criado.", adminEmail);
            return;
        }

        User admin = User.builder()
                .name("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(admin);
        log.info("Usuário admin inicial criado para {}.", adminEmail);
    }
}
