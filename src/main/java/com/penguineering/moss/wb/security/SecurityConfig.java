package com.penguineering.moss.wb.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/auth/**",
                                "/user/info",
                                "/user/register",
                                "/user/logout").permitAll()
                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .anyExchange().authenticated())
                .authenticationManager(Mono::just) // see SessionSecurityContextRepository
                .securityContextRepository(new SessionSecurityContextRepository())
                .httpBasic(httpBasicSpec -> httpBasicSpec
                        .authenticationEntryPoint(unauthorizedEntryPoint()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }

    private ServerAuthenticationEntryPoint unauthorizedEntryPoint() {
        return (exchange, e) -> {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return Mono.empty();
        };
    }
}