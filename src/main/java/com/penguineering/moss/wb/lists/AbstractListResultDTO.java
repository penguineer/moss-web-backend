package com.penguineering.moss.wb.lists;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Optional;

/**
 * Abstract Data Transfer Object for list results.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractListResultDTO {
    @JsonProperty("error")
    private final String error;

    /**
     * Constructs a new AbstractListResultDTO with the given error.
     *
     * @param error the error message
     */
    protected AbstractListResultDTO(String error) {
        this.error = error;
    }

    /**
     * Returns the error message as an Optional.
     *
     * @return the error message as an Optional
     */
    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }
}