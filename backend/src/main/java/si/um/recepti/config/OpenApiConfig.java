package si.um.recepti.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI receptiOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Recepti API")
                .description("REST vmesnik aplikacije za shranjevanje receptov (recepti).")
                .version("1.0"));
    }
}
