package com.skala.shopapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SKALA-SHOP API")
                        .description("온라인쇼핑몰 상품·고객·주문 관리 REST API")
                        .version("v1"));
    }
}
