package io.github.zorin96570.predicate;

import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Interface defining methods for converting query parameters into Hibernate predicates.
 * <p>
 * Implementations of this interface should define logic to extract, parse, and construct
 * query parameters as predicates used in Hibernate filtering.
 *
 * @param <T> the type of the root entity for filtering
 * @param <Y> the type of the field to filter
 */
public interface IPredicateFilter<T, Y> {
    /**
     * Extracts and processes query parameter values and operators for use in filtering.
     * <p>
     * Implementations should prepare any necessary data to generate predicates
     * when {@code getPredicate} is called.
     */
    void extract();

    /**
     * Parses a query parameter value from a {@code String} to the field type {@code Y}.
     *
     * @param value the value to parse
     * @return the parsed value as type {@code Y}
     */
    Y parseValue(String value);

    /**
     * Builds a {@link Predicate} for filtering based on the extracted parameters.
     *
     * @param builder the {@link CriteriaBuilder} used to create the predicate
     * @param root the root entity for the query
     * @param query the common query criteria
     * @return a {@link Predicate} representing the filter condition
     */
    Predicate getPredicate(CriteriaBuilder builder, Root<T> root, CommonAbstractCriteria query);
}
