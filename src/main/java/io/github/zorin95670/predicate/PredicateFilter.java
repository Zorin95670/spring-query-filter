package io.github.zorin95670.predicate;

import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Abstract class implementing common logic for creating query parameter predicates.
 * Provides utilities for managing parameter operators, values, and negation flags,
 * which are used to dynamically generate predicates.
 *
 * @param <T> the type of the root entity for filtering
 * @param <Y> the type of the field to filter
 */
public abstract class PredicateFilter<T, Y> implements IPredicateFilter<T, Y> {

    /**
     * Operators for each query parameter value.
     */
    private final PredicateOperator[] operators;

    /**
     * Flags indicating whether each operator should be negated.
     */
    private final boolean[] isNotOperators;

    /**
     * The name of the query parameter, representing the field to filter.
     */
    private String name;

    /**
     * The values provided for the query parameter, split by {@code OR_DELIMITER}.
     */
    private String[] values;

    /**
     * Constructor initializing a {@code PredicateFilter} with a name and raw query value.
     *
     * @param name the name of the field to filter
     * @param value the raw value of the query parameter
     */
    public PredicateFilter(final String name, final String value) {
        this.setName(name);
        this.setValues(value);
        this.operators = new PredicateOperator[this.getValues().length];
        this.isNotOperators = new boolean[this.getValues().length];
    }

    /**
     * Retrieves the negation flag for a specified index.
     *
     * @param index the index of the operator
     * @return {@code true} if negated; otherwise, {@code false}
     */
    public boolean getIsNotOperator(final int index) {
        return this.isNotOperators[index];
    }

    /**
     * Sets the negation flag for a specified index.
     *
     * @param index the index of the operator
     * @param state {@code true} to negate; {@code false} otherwise
     */
    public void setIsNotOperator(final int index, final boolean state) {
        this.isNotOperators[index] = state;
    }

    /**
     * Gets the name of the query parameter.
     *
     * @return the name of the query parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the query parameter.
     *
     * @param name the name of the query parameter
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Retrieves a specific value from the parameter list.
     *
     * @param index the index of the value
     * @return the parameter value at the specified index
     */
    public String getValue(final int index) {
        return this.values[index];
    }

    /**
     * Sets a specific value in the parameter list.
     *
     * @param index the index to set
     * @param value the new value
     */

    public void setValue(final int index, final String value) {
        this.values[index] = value;
    }

    /**
     * Gets all parameter values.
     *
     * @return a copy of the parameter values array
     */
    public String[] getValues() {
        return Arrays.copyOf(this.values, this.values.length);
    }

    /**
     * Sets parameter values by splitting the input string using {@code OR_DELIMITER}.
     *
     * @param value the raw parameter string
     */
    public void setValues(final String value) {
        this.values = value.split(PredicateOperator.OR.getValue());
    }

    /**
     * Retrieves the operator for a specified index.
     *
     * @param index the index of the operator
     * @return the operator at the specified index
     */
    public final PredicateOperator getOperator(final int index) {
        return operators[index];
    }

    /**
     * Sets the operator for a specified index.
     *
     * @param index the index to set
     * @param operator the new operator
     */
    public final void setOperator(final int index, final PredicateOperator operator) {
        this.operators[index] = operator;
    }

    /**
     * Sets the operator based on the parameter value.
     *
     * @param index the index to evaluate
     */
    public void setOperatorFromValue(final int index) {
        this.setOperator(index, PredicateOperator.EQUALS);

        if (this.values[index].toLowerCase().startsWith(PredicateOperator.NOT.getValue())) {
            this.setIsNotOperator(index, true);
            this.setValue(index, this.values[index].substring(PredicateOperator.NOT.getValue().length()));
        }

        if (PredicateOperator.NULL.getValue().equalsIgnoreCase(this.values[index])) {
            this.setOperator(index, PredicateOperator.NULL);
        }

        if (this.values[index].toLowerCase().startsWith(PredicateOperator.EQUALS.getValue())) {
            this.setValue(index, this.values[index].substring(PredicateOperator.EQUALS.getValue().length()));
        }
    }

    /**
     * Extracts operators for each parameter value.
     */
    @Override
    public void extract() {
        IntStream.range(0, this.getValues().length)
            .forEach(this::setOperatorFromValue);
    }

    /**
     * Generates a predicate combining all parameter conditions with logical OR.
     *
     * @param builder the criteria builder
     * @param root the query root
     * @param query the query criteria
     * @return a predicate representing the combined conditions
     */
    @Override
    public Predicate getPredicate(final CriteriaBuilder builder,
                                  final Root<T> root,
                                  final CommonAbstractCriteria query) {
        Predicate[] predicates = IntStream.range(0, this.getValues().length)
                .mapToObj(index -> this.getPredicate(index, builder, root))
                .toArray(Predicate[]::new);

        return builder.and(builder.or(predicates));
    }

    /**
     * Builds a predicate for a specific index based on the configured operator and field name.
     *
     * @param index the index of the parameter value to use
     * @param builder the {@link CriteriaBuilder} used to create the predicate
     * @param root the root entity containing the field to filter
     * @return a {@link Predicate} representing the condition at the specified index
     */
    public Predicate getPredicate(final int index,
                                  final CriteriaBuilder builder,
                                  final Root<T> root) {
        if (PredicateOperator.NULL.equals(this.operators[index])) {
            return this.getNullPredicate(index, builder, root.get(this.getName()));
        }

        return this.getPredicate(index, builder, root.get(this.getName()));
    }

    /**
     * Builds a predicate for a NULL condition on a specified field.
     *
     * @param index the index of the parameter to check for NULL
     * @param builder the {@link CriteriaBuilder} used to create the predicate
     * @param field the field expression on which the NULL condition will be applied
     * @return a {@link Predicate} that checks if the field is NULL or NOT NULL based on the negation flag
     */
    private Predicate getNullPredicate(final int index, final CriteriaBuilder builder, final Expression<Y> field) {
        if (this.getIsNotOperator(index)) {
            return builder.isNotNull(field);
        }
        return builder.isNull(field);
    }

    /**
     * Builds a predicate for a specified field with a comparison operator.
     * <p>
     * If the {@code isNotOperator} flag is true for the specified index, a NOT EQUAL condition is applied.
     * Otherwise, an EQUAL condition is applied based on the parsed value.
     *
     * @param index the index of the parameter value to use
     * @param builder the {@link CriteriaBuilder} used to create the predicate
     * @param field the field expression to compare
     * @return a {@link Predicate} comparing the field with the parsed parameter value
     */
    public Predicate getPredicate(final int index,
                                  final CriteriaBuilder builder,
                                  final Expression<Y> field) {
        if (this.getIsNotOperator(index)) {
            return builder.notEqual(field, parseValue(this.getValue(index)));
        }
        return builder.equal(field, parseValue(this.getValue(index)));
    }
}
