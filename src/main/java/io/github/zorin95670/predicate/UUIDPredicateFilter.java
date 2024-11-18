package io.github.zorin95670.predicate;

import io.github.zorin95670.exception.SpringQueryFilterException;

import java.util.UUID;

/**
 * A predicate filter specifically for handling {@link UUID} values in a
 * {@link jakarta.persistence.criteria.CriteriaQuery}.
 * <p>
 * This class extends {@link PredicateFilter} to support filtering based on {@link UUID} fields in the database.
 * It handles UUID comparisons and applies the appropriate operators such as equals.
 * </p>
 *
 * @param <T> The type of the entity being queried.
 */
public class UUIDPredicateFilter<T> extends PredicateFilter<T, UUID> {

    /**
     * Constructs a new {@link UUIDPredicateFilter} with the specified name and filter value.
     *
     * @param name The name of the field to filter by.
     * @param value The filter value(s) to apply.
     */
    public UUIDPredicateFilter(final String name, final String value) {
        super(name, value);
    }

    /**
     * Parses the given string value and returns it as a {@link UUID}.
     * <p>
     * This method attempts to parse the provided string value as a UUID. If the string is not in the valid UUID format,
     * a {@link SpringQueryFilterException} is thrown.
     * </p>
     *
     * @param value The string value to be parsed.
     * @return The parsed {@link UUID} value.
     * @throws SpringQueryFilterException if the string value cannot be parsed as a valid UUID.
     */
    @Override
    public UUID parseValue(final String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new SpringQueryFilterException(
                "Invalid UUID format: Unable to parse the value '" + value + "' as a UUID.",
                exception,
                "UUID",
                this.getName(),
                value
            );
        }
    }
}
