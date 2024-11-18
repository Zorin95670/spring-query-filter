package io.github.zorin95670.predicate;

/**
 * A concrete implementation of {@link PredicateFilter} used to handle boolean filters in queries.
 * <p>
 * This class provides functionality to process boolean query parameters and generate the corresponding
 * Hibernate predicates for filtering boolean fields in the database. It supports both equality checks
 * and negations (e.g., {@code true} and {@code false} values).
 * </p>
 *
 * @param <T> The type of the entity to be queried.
 */
public class BooleanPredicateFilter<T> extends PredicateFilter<T, Boolean> {

    /**
     * Constructs a {@link BooleanPredicateFilter} with the provided filter name and value.
     * <p>
     * This constructor initializes the parent {@link PredicateFilter} class with the name of the field
     * and the string value for the filter, which will be parsed to a boolean.
     * </p>
     *
     * @param name  The name of the field to be filtered.
     * @param value The value of the filter, which will be parsed as a boolean.
     */
    public BooleanPredicateFilter(final String name, final String value) {
        super(name, value);
    }

    /**
     * This method converts the string value ("true" or "false") into a {@link Boolean} object
     * using {@link Boolean#parseBoolean(String)}.
     *
     * @param value The string value to be parsed.
     * @return The parsed {@link Boolean} value.
     */
    @Override
    public Boolean parseValue(final String value) {
        return Boolean.parseBoolean(value);
    }
}
