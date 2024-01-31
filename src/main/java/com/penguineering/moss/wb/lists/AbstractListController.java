package com.penguineering.moss.wb.lists;

import com.penguineering.moss.wb.security.MossAuthentication;
import com.penguineering.moss.wb.security.directory.MossUserRepository;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractListController {

    protected final ListRepository listRepository;
    protected final MossUserRepository userRepository;

    public AbstractListController(ListRepository listRepository, MossUserRepository userRepository) {
        this.listRepository = listRepository;
        this.userRepository = userRepository;
    }

    protected Mono<ListEntity> findAndVerify(UUID id, MossAuthentication authentication) {
        return Mono.just(id)
                .map(listRepository::findById)
                .filter(Optional::isPresent)
                .switchIfEmpty(Mono.error(new NoSuchElementException()))
                .map(Optional::get)
                .filter(list -> Objects.equals(list.getUser().getId(), authentication.getUserId()))
                .switchIfEmpty(Mono.error(new IllegalAccessException("access denied")));
    }
}