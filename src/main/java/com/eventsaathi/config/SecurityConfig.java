package com.eventsaathi.config;

import com.eventsaathi.dto.ApiDtos.ErrorResponse;
import com.eventsaathi.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/index.html", "/style-guide.html", "/*.html", "/pages/**", "/favicon.ico", "/error",
                            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                            "/css/**", "/js/**", "/images/**", "/assets/**", "/*.css", "/*.js", "/*.ico", "/*.png", "/*.jpg", "/*.svg").permitAll()
                    .requestMatchers("/api/v1/health", "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/vendors/me").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/v1/vendors", "/api/v1/vendors/*",
                            "/api/v1/categories", "/api/v1/cities", "/api/v1/event-types",
                            "/api/v1/vendors/*/reviews").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/recommendations").permitAll()
                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> writeError(response, 401, "Unauthorized",
                "Authentication is required to access this resource", request.getRequestURI());
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> writeError(response, 403, "Forbidden",
                "You do not have permission to access this resource", request.getRequestURI());
    }

    private void writeError(HttpServletResponse response, int status, String error, String message, String path)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                new ErrorResponse(LocalDateTime.now(), status, error, message, path));
    }
}
