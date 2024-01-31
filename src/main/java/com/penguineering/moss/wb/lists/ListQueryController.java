package com.penguineering.moss.wb.lists;

import com.penguineering.moss.wb.security.MossAuthentication;
import com.penguineering.moss.wb.security.directory.MossUserRepository;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/lists/query")
@PermitAll
public class ListQueryController extends AbstractListController {

    public ListQueryController(ListRepository listRepository, MossUserRepository userRepository) {
        super(listRepository, userRepository);
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<ListQueryResultDTO>> getLists(@AuthenticationPrincipal MossAuthentication authentication) {
        return Mono.just(authentication.getUserId())
                .map(userRepository::getOne)
                .flatMapIterable(listRepository::findByUser)
                .collectList()
                .map(ListQueryResultDTO::fromEntities)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ListQueryResultDTO>> getList(@PathVariable UUID id,
                                                            @AuthenticationPrincipal MossAuthentication authentication) {
        return findAndVerify(id, authentication)
                .map(list -> ListQueryResultDTO.fromEntities(List.of(list)))
                .onErrorReturn(NoSuchElementException.class, ListQueryResultDTO.fromError("not found"))
                .map(ResponseEntity::ok);
    }
}