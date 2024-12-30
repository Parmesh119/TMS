package com.Tms.TMS.service

import com.Tms.TMS.repository.AuthRepository
import org.springframework.context.annotation.Bean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(private val authRepository: AuthRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {         //this function is of spring security and called during authentication to load user details by their username
        val user = authRepository.findByUsername(username) ?: throw UsernameNotFoundException("user not found")

        return User(
            user.username,
            user.passwordHash,
            listOf(SimpleGrantedAuthority("ROLE_${user.role}"))
        )
    }
}