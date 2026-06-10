package br.edu.ifgoiano.academico.matricula_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do OpenAPI/Swagger.
 *
 * Após subir o serviço, a documentação interativa fica disponível em:
 *   - Swagger UI:   http://localhost:8081/swagger-ui.html
 *   - OpenAPI JSON: http://localhost:8081/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI matriculaServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Matrícula Service API")
                .description("API de criação, cancelamento e consulta de matrículas do Sistema Acadêmico Distribuído.")
                .version("v1"));
    }
}
