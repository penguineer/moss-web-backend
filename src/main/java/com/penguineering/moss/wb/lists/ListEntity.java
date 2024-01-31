package com.penguineering.moss.wb.lists;

import com.penguineering.moss.wb.security.directory.MossUserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.UUID;

@Getter
@Setter
@Entity(name = "list")
public class ListEntity extends AbstractPersistable<UUID> {
    public void setId(UUID id) {
        super.setId(id);
    }

    String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    MossUserEntity user;
}