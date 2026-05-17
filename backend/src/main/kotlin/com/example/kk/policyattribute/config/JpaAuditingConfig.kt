package com.example.kk.policyattribute.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.Optional

/**
 * JPA Auditing configuration.
 * Returns "SYSTEM" as the auditor — to be replaced with Spring Security
 * principal once RBAC is implemented.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
class JpaAuditingConfig {

    @Bean
    fun auditorAware(): AuditorAware<String> {
        // TODO: Replace with SecurityContextHolder.getContext().authentication for RBAC
        return AuditorAware { Optional.of("SYSTEM") }
    }
}
