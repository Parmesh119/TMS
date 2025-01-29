package com.Tms.TMS.service

import org.keycloak.representations.idm.ClientRepresentation
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class GenerateAccessToken {

    fun getAccessTokenFromOpenID(): String {
        val restTemplate = RestTemplate()

        // Prepare form data
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("grant_type", "client_credentials")
        map.add("client_id", "Employee")
        map.add("client_secret", "uw797lsoQ1AKXenThPpDrQ1ImwfUXEL5")

        // Set headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        // Wrap data in HttpEntity
        val entity: HttpEntity<MultiValueMap<String, String>> = HttpEntity(map, headers)

        // Send POST request
        val response = restTemplate.exchange(
            "http://localhost:8080/realms/TMS/protocol/openid-connect/token",  // Replace with the actual URL
            HttpMethod.POST,  // HTTP Method
            entity,  // Request entity with data and headers
            Map::class.java
        )
        return response.body?.get("access_token") as? String
            ?: throw RuntimeException("Failed to fetch access token")
    }

    fun createHeaders(token: String): HttpHeaders {
        return HttpHeaders().apply {
            set("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }
    }

    fun getClientByClientId(headers: HttpHeaders, clientId: String): ClientRepresentation? {
        val restTemplate = RestTemplate()
        val adminBaseUrl = "http://localhost:8080/admin/realms/TMS"

        val responseEntity = restTemplate.exchange(
            "$adminBaseUrl/clients?clientId=$clientId",
            HttpMethod.GET,
            HttpEntity<Void>(headers),
            Array<ClientRepresentation>::class.java
        )

        return responseEntity.body?.firstOrNull()
    }
}