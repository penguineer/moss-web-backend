package com.penguineering.moss.wb.lists;

import com.penguineering.moss.wb.security.directory.MossUserEntity;
import com.penguineering.moss.wb.security.directory.MossUserRepository;

import java.util.UUID;

public record ListDTO(UUID id, String title, UUID userId) {

    public static ListDTO fromEntity(ListEntity entity) {
        return new ListDTO(entity.getId(), entity.getTitle(), entity.getUser().getId());
    }

    public ListEntity toEntity(MossUserRepository userRepository) {
        ListEntity entity = new ListEntity();
        entity.setId(this.id());
        entity.setTitle(this.title());
        MossUserEntity user = userRepository.getOne(this.userId());
        entity.setUser(user);
        return entity;
    }
}