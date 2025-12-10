package org.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI apiDocs() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Mi API")
                                                .description("Documentación de mi API con Swagger y OpenAPI 3")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("ZuxerCoding")
                                                                .url("https://github.com/zuxercoding99"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                                .servers(List.of(
                                                new Server().url("http://localhost:8080")
                                                                .description("Servidor local")))
                                // 🔒 Seguridad global (para JWT)
                                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                                .components(new io.swagger.v3.oas.models.Components()
                                                .addSecuritySchemes("BearerAuth",
                                                                new SecurityScheme()
                                                                                .name("Authorization")
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")));
        }
}
