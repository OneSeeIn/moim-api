package com.moim.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI moimOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Moim API").description("모임 서비스 전반의 REST API 명세입니다.")
                        .version("1.0.0")
                        .contact(new Contact().name("Moim Team").url("https://igemoim.com")))
                .components(new Components())
                .servers(List.of(new Server().url("/").description("기본 서버")));
    }

    @Bean
    public GroupedOpenApi moimPublicApi() {
        // 신규 컨트롤러가 /api/** 경로를 사용하면 자동으로 문서화됨
        return GroupedOpenApi.builder().group("moim-public")
                .packagesToScan("com.moim.api.controller").pathsToMatch("/api/**").build();
    }
}
