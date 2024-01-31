package com.penguineering.moss.wb.lists;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Optional;
import java.util.UUID;

/**
 * Data Transfer Object for list change results.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListChangeResultDTO extends AbstractListResultDTO {
    @JsonProperty("id")
    private final UUID id;

    /**
     * Constructs a new ListChangeResultDTO with the given error and id.
     *
     * @param error the error message
     * @param id the list id
     */
    protected ListChangeResultDTO(String error, UUID id) {
        super(error);
        this.id = id;
    }

    /**
     * Returns the list id.
     *
     * @return the list id
     */
    public Optional<UUID> getId() {
        return Optional.ofNullable(id);
    }

    /**
     * Creates a new ListChangeResultDTO with the given id.
     *
     * @param id the list id
     * @return a new ListChangeResultDTO with the given id
     */
    public static ListChangeResultDTO fromId(UUID id) {
        return new ListChangeResultDTO(null, id);
    }

    /**
     * Creates a new ListChangeResultDTO with the given error message.
     *
     * @param error the error message
     * @return a new ListChangeResultDTO with the given error message
     */
    public static ListChangeResultDTO fromError(String error) {
        return new ListChangeResultDTO(error, null);
    }
}