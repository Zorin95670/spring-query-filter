package io.github.zorin96570.predicate;

import io.github.zorin96570.exception.SpringQueryFilterException;

import java.util.Date;

/**
 * A predicate filter specifically for handling {@link Date} values in a
 * {@link jakarta.persistence.criteria.CriteriaQuery}.
 * <p>
 * This class extends {@link ComparablePredicateFilter} to support filtering based on {@link Date} fields in
 * the database.
 * It handles different types of date comparisons and parses string values into {@link Date} objects.
 * </p>
 *
 * @param <T> The type of the entity being queried.
 */
public class DatePredicateFilter<T> extends ComparablePredicateFilter<T, Date> {

    /**
     * Constructs a new {@link DatePredicateFilter} with the specified name and filter value.
     *
     * @param name The name of the field to filter by.
     * @param value The filter value(s) to apply.
     */
    public DatePredicateFilter(final String name, final String value) {
        super(name, value);
    }

    /**
     * Parses the given string value into a {@link Date} object.
     * <p>
     * This method attempts to convert the string value into a {@link Date} by parsing it as a long timestamp.
     * If the value cannot be parsed as a valid timestamp, a {@link SpringQueryFilterException} is thrown.
     * </p>
     *
     * @param value The string value to be parsed.
     * @return The parsed {@link Date} object.
     * @throws SpringQueryFilterException if the value cannot be parsed into a valid {@link Date}.
     * @see Date#Date(long)
     */
    @Override
    public Date parseValue(final String value) {
        try {
            return new Date(Long.parseLong(value));
        } catch (NumberFormatException exception) {
            throw new SpringQueryFilterException(
                "Invalid date format: Unable to parse the value '" + value + "' as a long timestamp.",
                exception,
                "DATE",
                this.getName(),
                value
            );
        }
    }
}
