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

    private static final RequestMatcher REFRESH_COOKIE_CSRF_MATCHER = request -> {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return "/api/auth/token/reissue".equals(path) || "/api/auth/logout".equals(path);
    };

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
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .requireCsrfProtectionMatcher(REFRESH_COOKIE_CSRF_MATCHER))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/health",
                                "/",
                                "/api/auth/csrf",
                                "/api/auth/oauth/**"
                        ).permitAll()
                        .requestMatchers(REFRESH_COOKIE_CSRF_MATCHER).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(accessTokenDecoder))
                        .authenticationEntryPoint((request, response, exception) ->
                                writeErrorResponse(response, objectMapper, ErrorStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) ->
                                writeErrorResponse(response, objectMapper, ErrorStatus.FORBIDDEN))
                );

        return http.build();
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            ErrorStatus errorStatus
    ) throws IOException {
        response.setStatus(errorStatus.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                new ApiResponse<Void>(false, errorStatus.getCode(), errorStatus.getMessage(), null)
        );
    }
}
