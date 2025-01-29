package com.Tms.TMS.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Your API Title")
                    .description("Your API Description")
                    .version("1.0")
                    .contact(
                        Contact()
                            .name("Parmesh Bhatt")
                            .email("parmeshb90@gmail.com")
                    )
            )
    }
}