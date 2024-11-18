package io.github.zorin95670.predicate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the filter type associated with a field.
 * <p>
 * This annotation allows the user to define a filter type for a field in a class. The filter type helps
 * identify how a field should be handled when applying query filters in a filtering mechanism (e.g.,
 * query parameters in an API request).
 * </p>
 * <p>
 * The `type` attribute specifies the type of the filter, which can be used for processing the filter values
 * dynamically.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FilterType {

    /**
     * Defines the filter type associated with the annotated field.
     * <p>
     * This value can be used to determine how the field should be processed during query filtering.
     * </p>
     *
     * @return the filter type as a string
     */
    Class<?> type();
}
