package com.example.packageaggregator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 (Swagger) configuration. Exposes API docs at /v3/api-docs and Swagger UI at /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Package Aggregation Service API")
                .description(
                        "REST API for the Package Aggregation Service. " +
                        "Packages aggregate products from an external product API; totals are stored in USD and " +
                        "converted to the requested currency (e.g. EUR, GBP) at response time using Frankfurter exchange rates. " +
                        "Internal endpoints support the frontend (product catalog, currency list)."
                )
                .version("1.0.0")
                .contact(new Contact()
                        .name("Package Aggregation Service"))
                .license(new License().name("Unspecified"));
    }
}
