package com.penguineering.moss.wb.security.directory;

import com.penguineering.moss.wb.security.mapping.OidcMappingKey;
import com.penguineering.moss.wb.security.mapping.OidcMappingRepository;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@PermitAll
@RestController
@RequestMapping("/user")
public class MossUserController {

    private final MossUserRepository mossUserRepository;
    private final OidcMappingRepository oidcMappingRepository;

    public MossUserController(MossUserRepository mossUserRepository,
                              OidcMappingRepository oidcMappingRepository) {
        this.mossUserRepository = mossUserRepository;
        this.oidcMappingRepository = oidcMappingRepository;
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<MossUserDTO>> getUserInfo(WebSession session) {
        return Mono.justOrEmpty(MossUserDTO.fromWebSession(session))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<MossUserDTO>> registerUser(@RequestBody @Nullable MossUserDTO mossUserDTO,
                                                          WebSession session) {
        final Optional<OidcMappingKey> oidcKey = OidcMappingKey.ofWebSession(session);

        if (oidcKey.isEmpty())
            return session.invalidate()
                    .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        return Mono.justOrEmpty(mossUserDTO)
                .publishOn(Schedulers.boundedElastic())
                // check if session has oidc mapping, otherwise invalidate
                .map(dto -> mossUserRepository
                        .save(MossUserEntity.fromDTO(dto))
                        .toMossUserDTO()) // with added UUID
                .doOnNext(user -> user.saveToWebSession(session))
                // store mapping
                .doOnNext(user -> oidcKey
                        .map(k -> k.toMapping(user))
                        .ifPresent(oidcMappingRepository::save))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Object>> logout(WebSession session) {
        return session.invalidate()
                .thenReturn(ResponseEntity.ok().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}