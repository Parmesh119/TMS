package com.Tms.TMS.config

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CloudinaryConfig {
    @Value("\${cloudinary.cloud-name}")
    private lateinit var cloudName: String

    @Value("\${cloudinary.api-key}")
    private lateinit var apiKey: String

    @Value("\${cloudinary.api-secret}")
    private lateinit var apiSecret: String

    @Bean
    fun cloudinary(): Cloudinary {
        val config = hashMapOf<String, String>()
        config["cloud_name"] = cloudName
        config["api_key"] = apiKey
        config["api_secret"] = apiSecret
        return Cloudinary(config)
    }
}