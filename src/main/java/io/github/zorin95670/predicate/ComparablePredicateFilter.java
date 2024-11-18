package io.github.zorin95670.predicate;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/**
 * Abstract class that provides filtering functionality for {@link Comparable} types.
 * <p>
 * This class extends {@link PredicateFilter} and is used for filtering fields that implement
 * the {@link Comparable} interface, such as numeric or string fields. It supports common comparison
 * operators such as equal, greater than, less than, and between.
 * </p>
 * <p>
 * This class provides specialized handling of comparison operators, including:
 * </p>
 * <ul>
 *     <li>{@link PredicateOperator#EQUALS}</li>
 *     <li>{@link PredicateOperator#INFERIOR} (less than)</li>
 *     <li>{@link PredicateOperator#SUPERIOR} (greater than)</li>
 *     <li>{@link PredicateOperator#BETWEEN}</li>
 * </ul>
 *
 * @param <T> the type of the entity being queried
 * @param <Y> the type of the field being filtered, which must extend {@link Comparable}
 *
 * @see PredicateFilter
 * @see Comparable
 * @see PredicateOperator
 */
public abstract class ComparablePredicateFilter<T, Y extends Comparable<Y>> extends PredicateFilter<T, Y> {

    /**
     * Constructor to initialize the filter with the field name and value.
     *
     * @param name the name of the field being filtered
     * @param value the value(s) used for filtering, typically passed from query parameters
     */
    ComparablePredicateFilter(final String name, final String value) {
        super(name, value);
    }

    /**
     * Sets the appropriate {@link PredicateOperator} for the filter based on the provided value.
     * <p>
     * This method analyzes the value at the given index to determine which comparison operator should be used
     * for the filter. It looks for specific operator prefixes (e.g., less than, greater than) and modifies the filter
     * operator accordingly. It also handles the "BETWEEN" operator if the value contains the keyword "BETWEEN".
     * </p>
     * <p>
     * The method will:
     * </p>
     * <ul>
     *     <li>Set the operator to {@link PredicateOperator#INFERIOR} if the value starts with
     *     the "less than" symbol.</li>
     *     <li>Set the operator to {@link PredicateOperator#SUPERIOR} if the value starts with
     *     the "greater than" symbol.</li>
     *     <li>Set the operator to {@link PredicateOperator#BETWEEN} if the value contains the keyword "BETWEEN".</li>
     * </ul>
     * <p>
     * Additionally, when setting the operator to {@link PredicateOperator#INFERIOR} or
     * {@link PredicateOperator#SUPERIOR},
     * the prefix is removed from the value for further processing.
     * </p>
     *
     * @param index the index of the current filter value
     */
    @Override
    public void setOperatorFromValue(final int index) {
        super.setOperatorFromValue(index);

        String value = this.getValue(index);

        if (value.toLowerCase().startsWith(PredicateOperator.INFERIOR.getValue())) {
            this.setOperator(index, PredicateOperator.INFERIOR);
            this.setValue(index, value.substring(PredicateOperator.INFERIOR.getValue().length()));
        } else if (value.toLowerCase().startsWith(PredicateOperator.SUPERIOR.getValue())) {
            this.setOperator(index, PredicateOperator.SUPERIOR);
            this.setValue(index, value.substring(PredicateOperator.SUPERIOR.getValue().length()));
        } else if (value.toLowerCase().contains(PredicateOperator.BETWEEN.getValue())) {
            this.setOperator(index, PredicateOperator.BETWEEN);
        }
    }

    /**
     * Creates a {@link Predicate} for the specified field using the filter values and operator.
     * <p>
     * This method generates the appropriate predicate based on the filter operator. It supports operators
     * like equals, less than, greater than, and between.
     * </p>
     *
     * @param index the index of the filter value to use
     * @param builder the {@link CriteriaBuilder} used to create the predicate
     * @param field the field to apply the predicate to
     * @return a {@link Predicate} representing the condition for the field
     */
    @Override
    public Predicate getPredicate(final int index, final CriteriaBuilder builder, final Expression<Y> field) {
        Predicate predicate;
        if (PredicateOperator.EQUALS.equals(this.getOperator(index))) {
            predicate = builder.equal(field, parseValue(this.getValue(index)));
        } else if (PredicateOperator.INFERIOR.equals(this.getOperator(index))) {
            predicate = builder.lessThan(field, parseValue(this.getValue(index)));
        } else if (PredicateOperator.SUPERIOR.equals(this.getOperator(index))) {
            predicate = builder.greaterThan(field, parseValue(this.getValue(index)));
        } else {
            String value = this.getValue(index);
            int operatorIndex = value.indexOf(PredicateOperator.BETWEEN.getValue());
            String value1 = value.substring(0, operatorIndex);
            String value2 = value.substring(operatorIndex + PredicateOperator.BETWEEN.getValue().length());

            predicate = builder.between(field, parseValue(value1), parseValue(value2));
        }

        if (this.getIsNotOperator(index)) {
            return builder.not(predicate);
        }

        return predicate;
    }
}
