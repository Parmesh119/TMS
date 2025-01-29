package com.Tms.TMS.config


import com.auth0.jwk.JwkProviderBuilder
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.websocket.Decoder
import org.bouncycastle.asn1.pkcs.RSAPublicKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.lang.Exception
import java.net.URL
import java.security.Key
import java.security.Security
import java.util.*
import kotlin.collections.HashMap

@Component
class JwtUtil(
    @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private val jwkSetUri: String
) {
    private val restTemplate = RestTemplate()

    fun extractAllClaims(token: String): Claims {
        try {
            val publicKey = getPublicKey(token)
            return Jwts.parser()
                .setSigningKey(publicKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getPublicKey(token: String): Key {
        try {
            // Get the JWK Set from Keycloak
            val jwkSet = restTemplate.getForObject(jwkSetUri, String::class.java)
            val jwkProvider = JwkProviderBuilder(URL(jwkSetUri)).build()

            // Get the kid (Key ID) from the JWT header
            val decodedJWT = Jwts.parser().unsecured().build().parseSignedContent(token)
            val kid = decodedJWT.header["kid"] as String?

            // Get the public key from JWK
            val jwk = jwkProvider.get(kid)
            return jwk.publicKey
        } catch (e: Exception) {
            throw RuntimeException("Failed to get public key", e)
        }
    }

    fun extractUsername(token: String): String? {
        return try {
            extractAllClaims(token).subject
        } catch (e: Exception) {
            null
        }
    }

    fun extractRoles(token: String): List<String> {
        return try {
            val claims = extractAllClaims(token)
            val realmAccess = claims["realm_access"] as? Map<*, *>
            val roles = realmAccess?.get("roles") as? List<String>
            roles ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            extractAllClaims(token).expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }
}
