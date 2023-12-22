package com.penguineering.moss.wb.security.directory;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.net.URI;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "moss_user")
public class MossUserEntity extends AbstractPersistable<UUID> {
    String displayName;

    String email;

    URI avatarUrl;

       public static MossUserEntity fromDTO(MossUserDTO mossUserDTO) {
            MossUserEntity mossUserEntity = new MossUserEntity();

            mossUserEntity.setId(mossUserDTO.id());
            mossUserEntity.setDisplayName(mossUserDTO.displayName());
            mossUserEntity.setEmail(mossUserDTO.email());
            mossUserEntity.setAvatarUrl(mossUserDTO.avatarUrl());

            return mossUserEntity;
        }

    public MossUserDTO toMossUserDTO() {
        return new MossUserDTO(getId(), displayName, email, avatarUrl);
    }
}