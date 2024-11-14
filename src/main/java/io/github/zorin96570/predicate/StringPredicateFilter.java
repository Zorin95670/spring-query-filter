package io.github.zorin96570.predicate;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/**
 * A predicate filter specifically for handling {@link String} values in a
 * {@link jakarta.persistence.criteria.CriteriaQuery}.
 * <p>
 * This class extends {@link PredicateFilter} to support filtering based on {@link String} fields in the database.
 * It handles string comparisons and modifies the query based on operators such as LIKE.
 * </p>
 *
 * @param <T> The type of the entity being queried.
 */
public class StringPredicateFilter<T> extends PredicateFilter<T, String> {

    /**
     * Constructs a new {@link StringPredicateFilter} with the specified name and filter value.
     *
     * @param name The name of the field to filter by.
     * @param value The filter value(s) to apply.
     */
    public StringPredicateFilter(final String name, final String value) {
        super(name, value);
    }

    /**
     * Sets the operator for the filter based on the provided value at the given index.
     * <p>
     * This method checks if the value starts with a "LIKE" operator and updates the operator accordingly.
     * It also replaces "*" with "%" for the LIKE operation and converts the value to uppercase.
     * </p>
     *
     * @param index The index of the current value in the filter.
     */
    @Override
    public final void setOperatorFromValue(final int index) {
        super.setOperatorFromValue(index);

        String value = getValue(index);

        if (value.toLowerCase().startsWith(PredicateOperator.LIKE.getValue())) {
            value = value.replace("*", "%");
            this.setOperator(index, PredicateOperator.LIKE);
            value = value.substring(PredicateOperator.LIKE.getValue().length());
        }

        this.setValue(index, value.toUpperCase());
    }

    /**
     * Returns a {@link Predicate} for the given field based on the filter conditions.
     * <p>
     * If the operator is "LIKE", it creates a LIKE predicate, considering the possibility of negation.
     * If the operator is not "LIKE", it delegates to the superclass for further processing.
     * </p>
     *
     * @param index The index of the current value in the filter.
     * @param builder The {@link CriteriaBuilder} used to construct the predicate.
     * @param field The field in the entity to apply the predicate to.
     * @return A {@link Predicate} that represents the filter condition.
     */
    @Override
    public final Predicate getPredicate(final int index,
                                        final CriteriaBuilder builder,
                                        final Expression<String> field) {
        if (PredicateOperator.LIKE.equals(this.getOperator(index))) {
            String value = this.getValue(index).toUpperCase();

            if (this.getIsNotOperator(index)) {
                return builder.notLike(builder.upper(field), value);
            }

            return builder.like(builder.upper(field), value);
        }

        return super.getPredicate(index, builder, builder.upper(field));
    }

    /**
     * Parses the given string value and returns it as a {@link String}.
     * <p>
     * This method simply returns the input value, as the filter does not require any specific parsing for strings.
     * </p>
     *
     * @param value The string value to be parsed.
     * @return The parsed string value.
     */
    @Override
    public String parseValue(final String value) {
        return value;
    }
}
