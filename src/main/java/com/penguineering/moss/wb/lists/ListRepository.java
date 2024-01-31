package com.penguineering.moss.wb.lists;

import com.penguineering.moss.wb.security.directory.MossUserEntity;
import jakarta.annotation.Nonnull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.Optional;
import java.util.UUID;

public interface ListRepository extends CrudRepository<ListEntity, UUID> {
    @Nonnull
    Optional<ListEntity> findById(@Nonnull UUID id);

    boolean existsByUserAndTitle(@Nonnull MossUserEntity user, @Nonnull String title);

    Streamable<ListEntity> findByUser(MossUserEntity user);
}