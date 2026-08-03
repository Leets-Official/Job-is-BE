package com.leets7th.job_is_be.global.config;

import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.properties.JwtProperties;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    /**
     * Refresh Token이 쿠키(HttpOnly)로 전달되는 엔드포인트만
     * CSRF 보호를 적용한다.
     *
     * Access Token 인증 API는 Authorization 헤더를 사용하므로
     * CSRF 공격 대상이 아니다.
     */
    private static final RequestMatcher REFRESH_COOKIE_CSRF_MATCHER = request -> {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());

        return "/api/auth/token/reissue".equals(path)
                || "/api/auth/logout".equals(path);
    };

    /**
     * CSRF 토큰을 JavaScript에서 읽을 수 있는 Cookie에 저장한다.
     * 프론트는 이 값을 X-CSRF-TOKEN 헤더로 전송한다.
     */
    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository(JwtProperties jwtProperties) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        repository.setCookieCustomizer(cookie -> cookie
                .path("/")
                .secure(jwtProperties.cookieSecure())
                .sameSite(jwtProperties.cookieSameSite()));

        return repository;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            @Qualifier("accessTokenDecoder") JwtDecoder accessTokenDecoder,
            CookieCsrfTokenRepository csrfTokenRepository,
            ObjectMapper objectMapper
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())
                // 테스트를 위해 CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 모든 요청에 대해 인증 없이 접근 허용
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // oauth2ResourceServer 설정은 그대로 두거나 주석 처리해도 무방합니다.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(accessTokenDecoder))
                        .authenticationEntryPoint((request, response, exception) ->
                                writeErrorResponse(response, objectMapper, ErrorStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) ->
                                writeErrorResponse(response, objectMapper, ErrorStatus.FORBIDDEN))
                );

        return http.build();
    }

    /**
     * Spring Security 기본 HTML 응답 대신
     * 프로젝트 공통 JSON 응답 형식으로 반환한다.
     */
    private void writeErrorResponse(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            ErrorStatus errorStatus
    ) throws IOException {

        response.setStatus(errorStatus.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                new ApiResponse<>(
                        false,
                        errorStatus.getCode(),
                        errorStatus.getMessage(),
                        null
                )
        );
    }
}
