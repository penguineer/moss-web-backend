package com.penguineering.moss.wb.lists;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for list results.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListQueryResultDTO extends AbstractListResultDTO {
    @JsonProperty("items")
    private final List<ListDTO> items;

    /**
     * Constructs a new ListResultDTO with the given error and items.
     *
     * @param error the error message
     * @param items the list items
     */
    protected ListQueryResultDTO(String error, List<ListDTO> items) {
        super(error);
        this.items = items;
    }

    /**
     * Returns the list items.
     *
     * @return the list items
     */
    public List<ListDTO> getItems() {
        return Objects.requireNonNullElse(items, List.of());
    }

    /**
     * Creates a new ListResultDTO from the given list entities.
     *
     * @param entities the list entities
     * @return a new ListResultDTO with the given list entities
     */
    public static ListQueryResultDTO fromEntities(List<ListEntity> entities) {
        List<ListDTO> items = entities.stream()
                .map(ListDTO::fromEntity)
                .collect(Collectors.toList());
        return new ListQueryResultDTO(null, items);
    }

    public static ListQueryResultDTO empty() {
        return new ListQueryResultDTO(null, List.of());
    }

    /**
     * Creates a new ListResultDTO with the given error message.
     *
     * @param error the error message
     * @return a new ListResultDTO with the given error message
     */
    public static ListQueryResultDTO fromError(String error) {
        return new ListQueryResultDTO(error, null);
    }
}