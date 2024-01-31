package com.penguineering.moss.wb.lists;

import com.penguineering.moss.wb.security.MossAuthentication;
import com.penguineering.moss.wb.security.directory.MossUserRepository;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/lists/change")
@PermitAll
public class ListChangeController extends AbstractListController {

    public ListChangeController(ListRepository listRepository, MossUserRepository userRepository) {
        super(listRepository, userRepository);
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<ListChangeResultDTO>> createList(@RequestBody Mono<String> title,
                                                                @AuthenticationPrincipal MossAuthentication authentication) {
        return Mono.just(authentication.getUserId())
                .map(userRepository::getOne)
                .zipWith(title, (user, t) -> {
                    ListEntity list = new ListEntity();
                    list.setTitle(t);
                    list.setUser(user);
                    return Tuples.of(user, list);
                })
                .publishOn(Schedulers.boundedElastic())
                .filter(t -> !listRepository.existsByUserAndTitle(t.getT1(), t.getT2().getTitle()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("already exists")))
                .map(Tuple2::getT2)
                .map(listRepository::save)
                .mapNotNull(ListEntity::getId)
                .map(ListChangeResultDTO::fromId)
                .onErrorResume(
                        IllegalArgumentException.class,
                        e -> Mono.just(ListChangeResultDTO.fromError(e.getMessage())))
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ListChangeResultDTO>> updateList(@PathVariable UUID id,
                                                                @RequestBody Mono<String> title,
                                                                @AuthenticationPrincipal MossAuthentication authentication) {

        var userAndTitle = Mono.just(authentication.getUserId())
                .map(userRepository::getOne)
                .zipWith(title, Tuples::of)
                .share();

        var checkUnique = userAndTitle
                .publishOn(Schedulers.boundedElastic())
                .filter(t -> !listRepository.existsByUserAndTitle(t.getT1(), t.getT2()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("already exists")));

        var update = findAndVerify(id, authentication)
                .zipWith(userAndTitle, (list, ut) -> {
                    String t = ut.getT2();
                    list.setTitle(t);
                    return list;
                })
                .map(listRepository::save)
                .map(list -> ListChangeResultDTO.fromId(list.getId()));

        return checkUnique
                .then(update)
                .onErrorReturn(NoSuchElementException.class, ListChangeResultDTO.fromError("not found"))
                .onErrorResume(
                        IllegalArgumentException.class,
                        e -> Mono.just(ListChangeResultDTO.fromError(e.getMessage())))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ListChangeResultDTO>> deleteList(@PathVariable UUID id,
                                                                @AuthenticationPrincipal MossAuthentication authentication) {
        return findAndVerify(id, authentication)
                .mapNotNull(ListEntity::getId)
                .doOnNext(listRepository::deleteById)
                .map(ListChangeResultDTO::fromId)
                .switchIfEmpty(Mono.just(ListChangeResultDTO.fromError("not found")))
                .onErrorReturn(NoSuchElementException.class, ListChangeResultDTO.fromError("not found"))
                .onErrorReturn(IllegalAccessException.class, ListChangeResultDTO.fromError("not found"))
                .map(ResponseEntity::ok);
    }
}