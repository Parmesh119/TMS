package com.Tms.TMS.config


import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.websocket.Decoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.lang.Exception
import java.security.Key
import java.security.Security
import java.util.*
import kotlin.collections.HashMap

@Component
class JwtUtil {
    @Value("\${jwt.expiration}")
    private var accessTokenExpiration: Long = 0

    @Value("\${jwt.refresh-token.expiration}")
    private var refreshTokenExpiration: Long = 0

    @Value("\${jwt.secret}")
    private lateinit var jwtsecret: String

    fun generateToken(userDetails: UserDetails, expiration: Long): String {
        val claims: MutableMap<String, Any> = HashMap()
        val role = userDetails.authorities.firstOrNull()?.authority ?: "ROLE_STAFF"
        claims["role"] = role

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun generateAccessToken(userDetails: UserDetails): String {
        return generateToken(userDetails, accessTokenExpiration)
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        return generateToken(userDetails, refreshTokenExpiration)
    }

    fun getSignKey(): Key {
        val keyBytes = Decoders.BASE64.decode(jwtsecret)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    fun extractRoles(token: String): String {
        val claims = extractAllClaims(token)
        return claims["role"] as String
    }

    fun isTokenExpired(token: String): Boolean {
        return extractAllClaims(token).expiration.before(Date())
    }

    fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .body
    }


    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        return (extractUsername(token) == userDetails.username && !isTokenExpired(token))
    }
}