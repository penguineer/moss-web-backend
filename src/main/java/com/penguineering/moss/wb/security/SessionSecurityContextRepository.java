package com.penguineering.moss.wb.security;

import com.penguineering.moss.wb.security.directory.MossUserDTO;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class SessionSecurityContextRepository implements ServerSecurityContextRepository {

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return new WebSessionServerSecurityContextRepository().save(exchange, context);
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return exchange
                .getSession()
                .flatMap(session -> MossUserDTO.fromWebSession(session)
                        .map(MossAuthentication::new)
                        .map(SecurityContextImpl::new)
                        .map(Mono::just)
                        .orElseGet(Mono::empty)
                );
    }
}