package com.Tms.TMS.service

import com.Tms.TMS.config.JwtUtil
import com.Tms.TMS.model.AuthResponse
import com.Tms.TMS.model.RegisterRequest
import com.Tms.TMS.model.User
import com.Tms.TMS.repository.AuthRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class AuthService (
    private val authRepository: AuthRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsServiceImpl,
    private val authenticationManager: AuthenticationManager
) {
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

    fun login(username: String, password: String): AuthResponse {    // Handles user login and JWT generation.
        try {
            // does verification of username and password
            val authToken = UsernamePasswordAuthenticationToken(username, password)
            authenticationManager.authenticate(authToken)
            val userDetails = userDetailsService.loadUserByUsername(username)
            val accessToken = jwtUtil.generateAccessToken(userDetails)
            val refreshToken = jwtUtil.generateRefreshToken(userDetails)
            val user = authRepository.findByUsername(username) ?: throw UsernameNotFoundException("user not found $username")
            user.refreshToken = refreshToken
            authRepository.update(user)
            return AuthResponse(accessToken = accessToken, refreshToken = refreshToken)
        } catch (e: Exception) {
            throw e
        }
    }

    fun refresh(refreshToken: String): AuthResponse? {
        val username = jwtUtil.extractUsername(refreshToken)     //the jwt refresh token in Header.Payload.Signature format so from the payload the username is extracted
        val userDetails = userDetailsService.loadUserByUsername(username)
        val user = authRepository.findByUsername(username) ?: throw UsernameNotFoundException("user not found: $username")
        if (user.refreshToken == refreshToken ) { //matches the refresher token and assignes new tokens
            val newAccessToken = jwtUtil.generateAccessToken(userDetails)
            val newRefreshToken = jwtUtil.generateRefreshToken(userDetails)
            user.refreshToken = newRefreshToken
            authRepository.save(user)
            return AuthResponse(accessToken = newAccessToken, refreshToken = newRefreshToken)
        }
        return null
    }

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