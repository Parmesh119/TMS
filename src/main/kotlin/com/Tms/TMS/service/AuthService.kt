package com.Tms.TMS.service

import com.Tms.TMS.config.JwtUtil
import com.Tms.TMS.model.AuthResponse
import com.Tms.TMS.model.RegisterRequest
import com.Tms.TMS.model.User
import com.Tms.TMS.repository.AuthRepository
import org.apache.http.HttpEntity
import org.apache.http.HttpHeaders
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.security.SecureRandom


@Service
class AuthService (
    private val authRepository: AuthRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    private val realm = "master"

    @Value("\${keycloak.auth-server-url}")
    private val authServerUrl: String? = null

    @Value("\${keycloak.resource}")
    private val clientId: String? = null

    @Value("\${keycloak.credentials.secret}")
    private val clientSecret: String? = null
    fun register(registerRequest: RegisterRequest): User {
        // Hash the password
        val passwordHash = passwordEncoder.encode(registerRequest.password)

        // Create a new user
        val user = User(
            username = registerRequest.username,
            email = registerRequest.email,
            passwordHash = passwordHash,
            role = registerRequest.role
        )

        // Save user to the repository
        return authRepository.save(user)
    }

    fun login(username: String, password: String): AuthResponse {
        // Handles user login and JWT generation.
        try {
            val restTemplate = RestTemplate()
            val tokenUrl = "$authServerUrl/realms/$realm/protocol/openid-connect/token"
            val headers = org.springframework.http.HttpHeaders()
            headers["Content-Type"] = "application/x-www-form-urlencoded"

            val body = "grant_type=password" +
                    "&client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&username=" + username +
                    "&password=" + password

            val entity = org.springframework.http.HttpEntity(body, headers)

            val response: ResponseEntity<MutableMap<String, Any>> = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                object: ParameterizedTypeReference<MutableMap<String, Any>>() {}
            )

//            val user = authRepository.findByUsername(username) ?: throw UsernameNotFoundException("user not found $username")
//            authRepository.update(user)
            println(response)
            val responseBody = response.body
            if (responseBody == null || !responseBody.containsKey("access_token")) {
                throw RuntimeException("Failed to get access token")
            }

            return AuthResponse(responseBody["access_token"].toString(), responseBody["refresh_token"].toString())
        } catch (e: Exception) {
            throw RuntimeException("Login failed", e)
        }
    }
//    fun refresh(refreshToken: String): AuthResponse? {
//        val username = jwtUtil.extractUsername(refreshToken)     //the jwt refresh token in Header.Payload.Signature format so from the payload the username is extracted
//        val userDetails = userDetailsService.loadUserByUsername(username)
//        val user = authRepository.findByUsername(username) ?: throw UsernameNotFoundException("user not found: $username")
//        if (user.refreshToken == refreshToken ) { //matches the refresher token and assignes new tokens
//            val newAccessToken = jwtUtil.generateAccessToken(userDetails)
//            val newRefreshToken = jwtUtil.generateRefreshToken(userDetails)
//            user.refreshToken = newRefreshToken
//            authRepository.save(user)
//            return AuthResponse(accessToken = newAccessToken, refreshToken = newRefreshToken)
//        }
//        return null
//    }

    fun generateRandomPassword(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#\$%^&*()-_=+<>?/"
        val random = SecureRandom()
        val password = StringBuilder()

        for (i in 0 until length) {
            val randomIndex = random.nextInt(chars.length)
            password.append(chars[randomIndex])
        }
        return password.toString()
    }
}