package io.github.zorin95670.predicate;

import io.github.zorin95670.exception.SpringQueryFilterException;

/**
 * A predicate filter specifically for handling {@link Long} values in a
 * {@link jakarta.persistence.criteria.CriteriaQuery}.
 * <p>
 * This class extends {@link ComparablePredicateFilter} to support filtering based on {@link Long} fields in
 * the database.
 * It handles different types of long comparisons and parses string values into {@link Long} objects.
 * </p>
 *
 * @param <T> The type of the entity being queried.
 */
public class LongPredicateFilter<T> extends ComparablePredicateFilter<T, Long> {

    /**
     * Constructs a new {@link LongPredicateFilter} with the specified name and filter value.
     *
     * @param name The name of the field to filter by.
     * @param value The filter value(s) to apply.
     */
    public LongPredicateFilter(final String name, final String value) {
        super(name, value);
    }

    /**
     * Parses the given string value into a {@link Long} object.
     * <p>
     * This method attempts to convert the string value into a {@link Long} by using {@link Long#parseLong(String)}.
     * If the value cannot be parsed as a valid long, a {@link SpringQueryFilterException} is thrown.
     * </p>
     *
     * @param value The string value to be parsed.
     * @return The parsed {@link Long} object.
     * @throws SpringQueryFilterException if the value cannot be parsed into a valid {@link Long}.
     * @see Long#parseLong(String)
     */
    @Override
    public Long parseValue(final String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new SpringQueryFilterException(
                "Invalid number format: Unable to parse the value '" + value + "' as a long.",
                exception,
                "LONG",
                this.getName(),
                value
            );
        }
    }
}
