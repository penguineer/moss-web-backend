package com.penguineering.moss.wb.security.directory;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface MossUserRepository extends CrudRepository<MossUserEntity, UUID> {
}