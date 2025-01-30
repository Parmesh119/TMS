package com.Tms.TMS.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class CorsConfig {
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/api/**") // Allow all endpoints under /api
                    .allowedOrigins("http://localhost:3000") // Allow requests from this origin
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow these HTTP methods
                    .allowedHeaders("*") // Allow all headers
                    .allowCredentials(true) // Allow credentials (e.g., cookies, authorization headers)
            }
        }
    }
}