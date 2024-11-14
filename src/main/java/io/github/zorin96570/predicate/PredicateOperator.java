package io.github.zorin96570.predicate;

import lombok.Getter;

/**
 * Enum representing different predicate operators used for query filtering.
 * <p>
 * This enum defines various operators that can be applied to filters when creating queries.
 * The operators correspond to common comparison operations such as equality, less than, greater than,
 * between, and null checks. Each operator is associated with a string value used for filtering purposes.
 * </p>
 */
public enum PredicateOperator {

    /**
     * Represents the equality operator (`=`) for filtering values.
     * <p>
     * The value associated with this operator is {@code "eq_"}.
     * </p>
     */
    EQUALS("eq_"),

    /**
     * Represents the "less than" operator (`<`) for filtering values.
     * <p>
     * The value associated with this operator is {@code "lt_"}.
     * </p>
     */
    INFERIOR("lt_"),

    /**
     * Represents the "greater than" operator (`>`) for filtering values.
     * <p>
     * The value associated with this operator is {@code "gt_"}.
     * </p>
     */
    SUPERIOR("gt_"),

    /**
     * Represents the "between" operator for filtering a range of values.
     * <p>
     * The value associated with this operator is {@code "_bt_"}.
     * </p>
     */
    BETWEEN("_bt_"),

    /**
     * Represents the "null" operator for filtering null values.
     * <p>
     * The value associated with this operator is {@code "null"}.
     * </p>
     */
    NULL("null"),

    /**
     * Represents the "like" operator for pattern matching in queries.
     * <p>
     * The value associated with this operator is {@code "lk_"}.
     * </p>
     */
    LIKE("lk_"),

    /**
     * Represents the "not" operator for pattern matching in queries.
     * <p>
     * The value associated with this operator is {@code "not_"}.
     * </p>
     */
    NOT("not_"),

    /**
     * Represents the "or" operator for pattern matching in queries.
     * <p>
     * The value associated with this operator is {@code "|"}.
     * </p>
     */
    OR("\\|");

    /**
     * Operator value.
     */
    @Getter
    private final String value;

    /**
     * Constructor to assign the string value to each operator.
     *
     * @param value the string value associated with the operator.
     */
    PredicateOperator(final String value) {
        this.value = value;
    }
}
