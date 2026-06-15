package com.familyfinance.finance.config;

import com.familyfinance.common.security.JwtAuthenticationFilter;
import com.familyfinance.common.security.JwtUtils;
import com.familyfinance.finance.security.InternalTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final InternalTokenFilter internalTokenFilter;

    public SecurityConfig(JwtUtils jwtUtils, InternalTokenFilter internalTokenFilter) {
        this.jwtUtils = jwtUtils;
        this.internalTokenFilter = internalTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/dashboard", "/operations", "/family", "/reports",
                                "/styles.css", "/app.js",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/api/internal/**",
                                "/ws/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(internalTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtils),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
