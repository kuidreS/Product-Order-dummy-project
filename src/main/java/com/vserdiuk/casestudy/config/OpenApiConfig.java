package com.vserdiuk.casestudy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productOrderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Order API")
                        .version("1.0.0")
                        .description("API for managing products and orders with stock reservation")
                        .contact(new Contact()
                                .name("Vitalii Serdiuk")
                                .email("vitaliy.serdiuk@gmail.com")));
    }
}
