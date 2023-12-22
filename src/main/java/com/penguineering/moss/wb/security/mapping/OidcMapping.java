package com.penguineering.moss.wb.security.mapping;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"issuer", "oidcId"})
@IdClass(OidcMappingKey.class)
@Table(name = "oidc_mapping")
public class OidcMapping {
    @Id
    @Column(name = "issuer")
    private String issuer;

    @Id
    @Column(name = "oidc_id")
    private String oidcId;

    @Column(name = "moss_user_id")
    private UUID mossUserId;

    @Override
    public String toString() {
        return issuer + "/" + oidcId + " -> " + mossUserId;
    }
}