package com.cnki.www.cnki_java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/papers/search").permitAll()
                .requestMatchers("/api/papers/recommend").permitAll()
                .requestMatchers("/api/papers/latest").permitAll()
                .requestMatchers("/api/papers/view").permitAll()
                .requestMatchers("/api/papers/view-history").permitAll()
                .requestMatchers("/api/papers/favorite").permitAll()
                .requestMatchers("/api/papers/favorites").permitAll()
                .requestMatchers("/captcha").permitAll()
                .requestMatchers("/pages/**").permitAll()
                .requestMatchers("/register").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/api/user/update").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/user/update").permitAll()
                .requestMatchers("/api/papers/upload").permitAll()
                .requestMatchers("/avatars/**").permitAll()
                .requestMatchers("/api/user/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/user/info").permitAll()
                .requestMatchers("/api/papers/download/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/papers/download/**").permitAll()

                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Arrays.asList(
                    "http://localhost:8081",
                    "http://localhost:8080"
                ));
                config.setAllowedMethods(Arrays.asList(
                    "GET", "POST", "PUT", "DELETE", "OPTIONS"
                ));
                config.setAllowedHeaders(Arrays.asList(
                    "Content-Type",
                    "Authorization",
                    "X-Requested-With",
                    "Accept",
                    "Origin",
                    "Access-Control-Request-Method",
                    "Access-Control-Request-Headers"
                ));
                config.setExposedHeaders(Arrays.asList(
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Credentials"
                ));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);
                return config;
            }));
        return http.build();
    }
} 