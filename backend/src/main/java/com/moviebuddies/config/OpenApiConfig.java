package com.moviebuddies.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 설정 클래스
 * JWT 인증을 포함한 API 문서화 설정을 담당
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 설정
     * JWT Bearer 토큰 인증을 지원하는 API 문서 구성
     * 어떤 네트워크에서든 현재 접속한 주소로 API 호출 가능
     * .servers()를 사용해서 특정 서버에서만 접근이 가능하도록 할 수 있음
     *
     * @return 설정된 OpenAPI 객체
     */
    @Bean
    public OpenAPI openAPI() {

        // API 기본 정보
        Info info = new Info()
                .title("MovieBuddies API")
                .version("v1.0.0")
                .description("영화 정보 공유 및 리뷰 플랫폼 API 문서");
        
        // JWT 보안 스키마 이름
        String jwtSchemeName = "Bearer Authentication";

        // API 요청 헤더에 JWT 인증 정보 포함 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        
        // JWT 보안 스키마 등록
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")   // Bearer 토큰
                        .bearerFormat("JWT")    // JWT 형식
                        .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다."));

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
