package com.penguineering.moss.wb;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public Mono<String> index() {
        return Mono.just("Aministration area");
    }
}