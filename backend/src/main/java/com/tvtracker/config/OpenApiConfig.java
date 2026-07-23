package com.tvtracker.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Shogunate API", description = "TV show tracker REST API", version = "1.0"),
        tags = {
            @Tag(name = "Auth", description = "Registration, login, and current user"),
            @Tag(name = "Shows", description = "TVmaze search and user library"),
            @Tag(name = "Watch", description = "Mark and unmark watched targets"),
            @Tag(name = "Reviews", description = "Episode, season, and show reviews"),
            @Tag(name = "Favorites", description = "Favorite shows and suggestions"),
            @Tag(name = "Analytics", description = "Watch and library statistics"),
            @Tag(name = "System", description = "Health and diagnostics")
        })
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI shogunateOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_SCHEME_NAME,
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .name(BEARER_SCHEME_NAME)
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
