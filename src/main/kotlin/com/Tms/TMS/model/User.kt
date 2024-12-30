package com.Tms.TMS.model

import org.springframework.data.annotation.Id
import java.util.*

data class User (
    @Id val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String?,
    val passwordHash: String,
    val role: String = "User",
    var refreshToken: String? = null
)

data class RegisterRequest (
    val username: String,
    val password: String,
    val email: String,
    val role: String = "User"
)

data class LoginRequest (
    val email: String,
    val password: String
)

data class AuthResponse (
    val accessToken: String,
    val refreshToken: String
)