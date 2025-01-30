package com.Tms.TMS.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun dataSource(): javax.sql.DataSource? {
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://localhost:5432/postgres?currentSchema=public")
            .username("postgres")
            .password("kavipam27")
            .driverClassName("org.postgresql.Driver")
            .build()
    }

    @Bean
    fun jdbcTemplate(dataSource: javax.sql.DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val jwtConverter = jwtAuthenticationConverter()

        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/public/**").permitAll()
                    .requestMatchers("/api/v1/users/**", "/api/v1/employees/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtConverter)
                }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val jwtConverter = JwtAuthenticationConverter()
        jwtConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            extractAuthorities(jwt)
        }
        return jwtConverter
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()
        val resourceAccess = jwt.claims["resource_access"] as? Map<String, Any>
        val clientRoles = resourceAccess?.get("Employee") as? Map<String, Any>
        val roles = clientRoles?.get("roles") as? List<String>
        roles?.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }
        return authorities
    }

    @Bean
    fun jwtDecoder(@Value("\${spring.security.oauth2.client.provider.keycloak.issuer-uri}") issuerUri: String): JwtDecoder {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build()
    }
}