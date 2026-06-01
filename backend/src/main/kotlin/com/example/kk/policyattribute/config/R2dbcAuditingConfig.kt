package com.example.kk.policyattribute.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import reactor.core.publisher.Mono

/**
 * R2DBC Auditing configuration.
 * Returns "SYSTEM" as the auditor.
 */
@Configuration
@EnableR2dbcAuditing(auditorAwareRef = "auditorAware")
class R2dbcAuditingConfig {

    @Bean
    fun auditorAware(): ReactiveAuditorAware<String> {
        // TODO: Replace with reactive security context principal for RBAC
        return ReactiveAuditorAware { Mono.just("SYSTEM") }
    }
}
