package com.penguineering.moss.wb.security.mapping;

import com.penguineering.moss.wb.security.directory.MossUserDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.server.WebSession;

import java.io.Serializable;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class OidcMappingKey implements Serializable {
    protected static final String SESSION_KEY = "oidc-mapping-key";

    private String issuer;
    private String oidcId;

    public static OidcMappingKey of(String issuer, String oidcId) {
        return new OidcMappingKey(issuer, oidcId);
    }

    public static Optional<OidcMappingKey> ofWebSession(WebSession session) {
        return Optional.ofNullable(session.getAttributes().get(SESSION_KEY))
                .filter(key -> key instanceof OidcMappingKey)
                .map(OidcMappingKey.class::cast);
    }

    public OidcMappingKey(String issuer, String oidcId) {
        this.issuer = issuer;
        this.oidcId = oidcId;
    }

    public Optional<OidcMappingKey> saveToWebSession(WebSession session) {
        return Optional
                .ofNullable(session.getAttributes().put(SESSION_KEY, this))
                .filter(key -> key instanceof OidcMappingKey)
                .map(OidcMappingKey.class::cast);
    }

    public OidcMapping toMapping(MossUserDTO mossUser) {
        return new OidcMapping(issuer, oidcId, mossUser.id());
    }

    @Override
    public String toString() {
        return issuer + "/" + oidcId;
    }
}