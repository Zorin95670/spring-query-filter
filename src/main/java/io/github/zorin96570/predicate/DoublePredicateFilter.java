package io.github.zorin96570.predicate;

import io.github.zorin96570.exception.SpringQueryFilterException;

/**
 * A predicate filter specifically for handling {@link Double} values in a
 * {@link jakarta.persistence.criteria.CriteriaQuery}.
 * <p>
 * This class extends {@link ComparablePredicateFilter} to support filtering based on {@link Double} fields in
 * the database.
 * It handles different types of double comparisons and parses string values into {@link Double} objects.
 * </p>
 *
 * @param <T> The type of the entity being queried.
 */
public class DoublePredicateFilter<T> extends ComparablePredicateFilter<T, Double> {

    /**
     * Constructs a new {@link DoublePredicateFilter} with the specified name and filter value.
     *
     * @param name The name of the field to filter by.
     * @param value The filter value(s) to apply.
     */
    public DoublePredicateFilter(final String name, final String value) {
        super(name, value);
    }

    /**
     * Parses the given string value into a {@link Double} object.
     * <p>
     * This method attempts to convert the string value into a {@link Double} by using
     * {@link Double#parseDouble(String)}.
     * If the value cannot be parsed as a valid double, a {@link SpringQueryFilterException} is thrown.
     * </p>
     *
     * @param value The string value to be parsed.
     * @return The parsed {@link Double} object.
     * @throws SpringQueryFilterException if the value cannot be parsed into a valid {@link Double}.
     * @see Double#parseDouble(String)
     */
    @Override
    public Double parseValue(final String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new SpringQueryFilterException(
                "Invalid number format: Unable to parse the value '" + value + "' as a double.",
                exception,
                "DOUBLE",
                this.getName(),
                value
            );
        }
    }
}
