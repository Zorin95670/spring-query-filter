package io.github.zorin95670.predicate;

import io.github.zorin95670.exception.SpringQueryFilterException;

/**
 * A predicate filter specifically for handling {@link Integer} values in a
 * {@link jakarta.persistence.criteria.CriteriaQuery}.
 * <p>
 * This class extends {@link ComparablePredicateFilter} to support filtering based on {@link Integer} fields in
 * the database.
 * It handles different types of integer comparisons and parses string values into {@link Integer} objects.
 * </p>
 *
 * @param <T> The type of the entity being queried.
 */
public class IntegerPredicateFilter<T> extends ComparablePredicateFilter<T, Integer> {

    /**
     * Constructs a new {@link IntegerPredicateFilter} with the specified name and filter value.
     *
     * @param name The name of the field to filter by.
     * @param value The filter value(s) to apply.
     */
    public IntegerPredicateFilter(final String name, final String value) {
        super(name, value);
    }

    /**
     * Parses the given string value into an {@link Integer} object.
     * <p>
     * This method attempts to convert the string value into an {@link Integer} by using
     * {@link Integer#parseInt(String)}.
     * If the value cannot be parsed as a valid integer, a {@link SpringQueryFilterException} is thrown.
     * </p>
     *
     * @param value The string value to be parsed.
     * @return The parsed {@link Integer} object.
     * @throws SpringQueryFilterException if the value cannot be parsed into a valid {@link Integer}.
     * @see Integer#parseInt(String)
     */
    @Override
    public Integer parseValue(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new SpringQueryFilterException(
                "Invalid number format: Unable to parse the value '" + value + "' as a integer.",
                exception,
                "INTEGER",
                this.getName(),
                value
            );
        }
    }
}
