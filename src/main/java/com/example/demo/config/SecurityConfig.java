package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API is stateless here, so CSRF protection is turned off.
                .csrf(AbstractHttpConfigurer::disable)
                // Demo app keeps endpoints open; tighten this for production.
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                // Keeps default basic auth wiring available when policies change.
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}

