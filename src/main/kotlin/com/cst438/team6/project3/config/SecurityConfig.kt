package com.cst438.team6.project3.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    companion object {
        private const val ROLES_CLAIM = "roles"
        private const val ROLE_PREFIX = "ROLE_"
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        corsConfigurationSource: CorsConfigurationSource
    ): SecurityFilterChain {
        http
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource)
            }
            .csrf { csrf ->
                csrf.disable()
            }
            .sessionManagement { sessions ->
                sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                    .requestMatchers(
                        "/errpr",
                        "/actuator/health",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(
        properties: AppCorsProperties
    ): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = properties.allowedOrigins
        configuration.allowedMethods = listOf(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        )
        configuration.allowedHeaders = listOf(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE
        )

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }

    private fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val authoritiesConverter = JwtGrantedAuthoritiesConverter()

        authoritiesConverter.setAuthoritiesClaimName(ROLES_CLAIM)
        authoritiesConverter.setAuthorityPrefix(ROLE_PREFIX)

        val authenticationConverter = JwtAuthenticationConverter()
        authenticationConverter.setJwtGrantedAuthoritiesConverter(
            authoritiesConverter
        )

        return authenticationConverter
    }
}