package com.penguineering.moss.wb.security.directory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.server.WebSession;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record MossUserDTO(
        @JsonProperty("id") UUID id,
        @JsonProperty("display_name") String displayName,
        @JsonProperty("email") String email,
        @JsonProperty("avatar_url") URI avatarUrl) {

    public MossUserDTO {
        Objects.requireNonNull(displayName, "displayName must not be null");
    }

    @Override
    public String toString() {
        return displayName + "(" + id + ")";
    }

    @JsonIgnore
    public boolean isNew() {
        return Objects.isNull(id);
    }

    public static Optional<MossUserDTO> fromWebSession(WebSession session) {
        return Optional.ofNullable(session.getAttribute("user"))
                .filter(user -> user instanceof MossUserDTO)
                .map(MossUserDTO.class::cast);
    }

    public Optional<MossUserDTO> saveToWebSession(WebSession session) {
        return Optional.ofNullable(session.getAttributes().put("user", this))
                .filter(user -> user instanceof MossUserDTO)
                .map(MossUserDTO.class::cast);
    }
}