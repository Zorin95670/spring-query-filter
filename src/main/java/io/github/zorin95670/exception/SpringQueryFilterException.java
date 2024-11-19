package io.github.zorin95670.exception;

/**
 * Exception thrown to indicate an error occurred while processing query filters in the Spring Query Filter library.
 * Provides additional details about the filter type, query parameter name, and the associated value,
 * aiding in debugging and error tracking.
 * <p>
 * Extends {@link RuntimeException}, allowing users to handle or propagate this exception as needed.
 * </p>
 */
public class SpringQueryFilterException extends RuntimeException {

    /**
     * Represents the type of the query filter or the root cause of the exception.
     * Can either indicate the type specified in the entity to help identify the filter
     * associated with {@code queryParameterName}, or describe the nature of the error.
     */
    private final String queryFilterType;

    /**
     * The name of the query parameter that caused the exception.
     */
    private final String queryParameterName;

    /**
     * The value of the query parameter that caused the exception.
     */
    private final String queryParameterValue;

    /**
     * Constructs a new {@code SpringQueryFilterException} with the specified filter type,
     * parameter name, and parameter value. No custom message or cause is provided.
     *
     * @param queryFilterType     represents the type of the filter or the cause of the exception
     * @param queryParameterName  the name of the query parameter causing the exception
     * @param queryParameterValue the value of the query parameter causing the exception
     */
    public SpringQueryFilterException(final String queryFilterType,
                                      final String queryParameterName,
                                      final String queryParameterValue) {
        this(null, null, queryFilterType, queryParameterName, queryParameterValue);
    }

    /**
     * Constructs a new {@code SpringQueryFilterException} with the specified message, filter type,
     * parameter name, and parameter value.
     *
     * @param message             the detail message (can be {@code null})
     * @param queryFilterType     represents the type of the filter or the cause of the exception
     * @param queryParameterName  the name of the query parameter causing the exception
     * @param queryParameterValue the value of the query parameter causing the exception
     */
    public SpringQueryFilterException(final String message,
                                      final String queryFilterType,
                                      final String queryParameterName,
                                      final String queryParameterValue) {
        this(message, null, queryFilterType, queryParameterName, queryParameterValue);
    }

    /**
     * Constructs a new {@code SpringQueryFilterException} with the specified cause, filter type,
     * parameter name, and parameter value.
     *
     * @param cause               the cause of the exception (can be {@code null})
     * @param queryFilterType     represents the type of the filter or the cause of the exception
     * @param queryParameterName  the name of the query parameter causing the exception
     * @param queryParameterValue the value of the query parameter causing the exception
     */
    public SpringQueryFilterException(final Throwable cause,
                                      final String queryFilterType,
                                      final String queryParameterName,
                                      final String queryParameterValue) {
        this(null, cause, queryFilterType, queryParameterName, queryParameterValue);
    }

    /**
     * Constructs a new {@code SpringQueryFilterException} with the specified message, cause, filter type,
     * parameter name, and parameter value.
     *
     * @param message             the detail message (can be {@code null})
     * @param cause               the cause of the exception (can be {@code null})
     * @param queryFilterType     represents the type of the filter or the cause of the exception
     * @param queryParameterName  the name of the query parameter causing the exception
     * @param queryParameterValue the value of the query parameter causing the exception
     */
    public SpringQueryFilterException(final String message,
                                      final Throwable cause,
                                      final String queryFilterType,
                                      final String queryParameterName,
                                      final String queryParameterValue) {
        super(message, cause);
        this.queryFilterType = queryFilterType;
        this.queryParameterName = queryParameterName;
        this.queryParameterValue = queryParameterValue;
    }

    /**
     * Gets the query filter type that caused the exception.
     *
     * @return the query filter type
     */
    public String getQueryFilterType() {
        return queryFilterType;
    }

    /**
     * Gets the query parameter name that caused the exception.
     *
     * @return the query parameter name
     */
    public String getQueryParameterName() {
        return queryParameterName;
    }

    /**
     * Gets the query parameter value that caused the exception.
     *
     * @return the query parameter value
     */
    public String getQueryParameterValue() {
        return queryParameterValue;
    }
}
