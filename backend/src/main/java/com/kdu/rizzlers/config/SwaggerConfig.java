package com.kdu.rizzlers.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rizzlers IBE API")
                        .description("API documentation for the Rizzlers")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Rizzlers Team")
                                .email("singhgaganbtp@gmail.com")
                                )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("https://uydc3b10re.execute-api.ap-south-1.amazonaws.com/dev/api").description("Development Server"),
                        new Server().url("https://lmak1hj7n7.execute-api.ap-south-1.amazonaws.com/qa/api").description("QA Server")
                ));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }
}