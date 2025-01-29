package com.Tms.TMS.config

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class KeycloakAdminConfig {

    @Value("\${keycloak.auth-server-url}")
    private lateinit var keycloakAuthServerUrl: String

    @Value("\${keycloak.realm}")
    private lateinit var keycloakRealm: String

    @Value("\${keycloak.resource}")
    private lateinit var keycloakClientId: String

    @Value("\${keycloak.credentials.secret}")
    private lateinit var keycloakClientSecret: String

    @Value("\${keycloak.username}")
    private lateinit var keycloakUsername: String

    @Value("\${keycloak.password}")
    private lateinit var keycloakPassword: String

    @Bean
    fun keycloakAdmin(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakAuthServerUrl)
            .realm(keycloakRealm)
            .grantType(OAuth2Constants.PASSWORD)
            .clientId(keycloakClientId)  // Use your client ID
            .clientSecret(keycloakClientSecret)  // Use your client secret
            .username(keycloakUsername)
            .password(keycloakPassword)
            .build()
    }

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        // Add FormHttpMessageConverter
        restTemplate.messageConverters.add(FormHttpMessageConverter())
        return restTemplate
    }

    private fun simpleClientHttpRequestFactory(): ClientHttpRequestFactory {
        return SimpleClientHttpRequestFactory()
    }
}