package io.github.zorin96570.predicate;

import io.github.zorin96570.exception.SpringQueryFilterException;

/**
 * A predicate filter specifically for handling {@link Float} values in a
 * {@link jakarta.persistence.criteria.CriteriaQuery}.
 * <p>
 * This class extends {@link ComparablePredicateFilter} to support filtering based on {@link Float} fields in
 * the database.
 * It handles different types of float comparisons and parses string values into {@link Float} objects.
 * </p>
 *
 * @param <T> The type of the entity being queried.
 */
public class FloatPredicateFilter<T> extends ComparablePredicateFilter<T, Float> {

    /**
     * Constructs a new {@link FloatPredicateFilter} with the specified name and filter value.
     *
     * @param name The name of the field to filter by.
     * @param value The filter value(s) to apply.
     */
    public FloatPredicateFilter(final String name, final String value) {
        super(name, value);
    }

    /**
     * Parses the given string value into a {@link Float} object.
     * <p>
     * This method attempts to convert the string value into a {@link Float} by using {@link Float#parseFloat(String)}.
     * If the value cannot be parsed as a valid float, a {@link SpringQueryFilterException} is thrown.
     * </p>
     *
     * @param value The string value to be parsed.
     * @return The parsed {@link Float} object.
     * @throws SpringQueryFilterException if the value cannot be parsed into a valid {@link Float}.
     * @see Float#parseFloat(String)
     */
    @Override
    public Float parseValue(final String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException exception) {
            throw new SpringQueryFilterException(
                "Invalid number format: Unable to parse the value '" + value + "' as a float.",
                exception,
                "FLOAT",
                this.getName(),
                value
            );
        }
    }
}