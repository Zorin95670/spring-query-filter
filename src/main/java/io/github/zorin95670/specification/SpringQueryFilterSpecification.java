package io.github.zorin95670.specification;

import io.github.zorin95670.exception.SpringQueryFilterException;
import io.github.zorin95670.predicate.BooleanPredicateFilter;
import io.github.zorin95670.predicate.DatePredicateFilter;
import io.github.zorin95670.predicate.DoublePredicateFilter;
import io.github.zorin95670.predicate.FilterType;
import io.github.zorin95670.predicate.FloatPredicateFilter;
import io.github.zorin95670.predicate.IPredicateFilter;
import io.github.zorin95670.predicate.IntegerPredicateFilter;
import io.github.zorin95670.predicate.LongPredicateFilter;
import io.github.zorin95670.predicate.StringPredicateFilter;
import io.github.zorin95670.predicate.UUIDPredicateFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A specification used for filtering entities based on a map of filters.
 * <p>
 * This class implements the {@link Specification} interface and is used to create dynamic queries based on
 * a set of filters.
 * It supports filtering by various field types such as {@code String}, {@code Date}, {@code Integer}, {@code Long},
 * {@code Float},
 * {@code Double}, {@code Boolean}, and {@code UUID}. The filters are specified as a map where the key is the field name
 * and the value is a list of filter values. This class also supports extracting predicates based on annotations
 * defined on entity fields.
 * </p>
 *
 * @param <T> The entity type for which the specification is created.
 */
public class SpringQueryFilterSpecification<T> implements Specification<T> {

    /**
     * The map of filters where the key is the field name and the value is a list of filter values.
     * <p>
     * This map is used to dynamically build the filtering predicates based on the provided values for each field.
     * </p>
     */
    private final Map<String, List<String>> filters;

    /**
     * The class of the entity to filter.
     * <p>
     * This class is used to reflect on the fields of the entity and apply filters accordingly.
     * </p>
     */
    private final Class<T> entityClass;

    /**
     * Constructs a new specification with the provided entity class and filters.
     *
     * @param entityClass The entity class to apply the specification to.
     * @param filters The map of filters for field names and values.
     */
    public SpringQueryFilterSpecification(final Class<T> entityClass, final Map<String, List<String>> filters) {
        this.entityClass = entityClass;
        this.filters = filters;
    }

    /**
     * Gets the map of filters.
     *
     * @return the map of filters
     */
    public Map<String, List<String>> getFilters() {
        return filters;
    }

    /**
     * Gets the class of the entity to filter.
     *
     * @return the class of the entity
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * Returns a list of all fields of the entity class, including fields from superclasses.
     *
     * @return A list of {@link Field} objects representing all the fields of the entity and its superclasses.
     */
    public final List<Field> getFields() {
        final List<Field> fields = new ArrayList<>();

        Class<?> current = entityClass;
        while (current != null) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }

    /**
     * Creates a predicate filter based on the field type.
     * <p>
     * This method maps the field type (such as {@code STRING}, {@code DATE}, etc.) to the appropriate predicate filter
     * implementation. If the filter type is unsupported, an exception is thrown.
     * </p>
     *
     * @param type The type of the field to filter.
     * @param name The name of the field to filter.
     * @param value The value to filter by.
     * @return An {@link IPredicateFilter} that can generate a predicate for the specified field.
     * @throws SpringQueryFilterException If the filter type is unsupported.
     */
    public IPredicateFilter<T, ?> getPredicateFilter(final Class<?> type, final String name, final String value) {
        if (String.class.equals(type)) {
            return new StringPredicateFilter<>(name, value);
        }

        if (Date.class.equals(type)) {
            return new DatePredicateFilter<>(name, value);
        }

        if (Integer.class.equals(type)) {
            return new IntegerPredicateFilter<>(name, value);
        }

        if (Long.class.equals(type)) {
            return new LongPredicateFilter<>(name, value);
        }

        if (Float.class.equals(type)) {
            return new FloatPredicateFilter<>(name, value);
        }

        if (Double.class.equals(type)) {
            return new DoublePredicateFilter<>(name, value);
        }

        if (Boolean.class.equals(type)) {
            return new BooleanPredicateFilter<>(name, value);
        }

        if (UUID.class.equals(type)) {
            return new UUIDPredicateFilter<>(name, value);
        }

        throw new SpringQueryFilterException(
            "Unsupported filter type: '" + type.getSimpleName() + "'. Valid types are String, Date, Integer, Long,"
                + " Float, Double, Boolean, UUID.",
            type.getSimpleName(),
            name,
            value
        );
    }

    /**
     * Converts the map of filters into a {@link Predicate} that can be used in a JPA query.
     * <p>
     * This method iterates over the fields of the entity and checks if any field is annotated with {@link FilterType}.
     * If the field is annotated and its name is present in the filters map, a predicate is created for that field.
     * The predicates for all fields are then combined using the {@link CriteriaBuilder#and(Predicate...)} method.
     * </p>
     *
     * @param root The root of the query, representing the entity.
     * @param query The query being created.
     * @param builder The criteria builder used to construct the predicates.
     * @return A combined {@link Predicate} that represents the filters.
     */
    @Override
    public final Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query,
                                       final CriteriaBuilder builder) {
        Predicate[] predicates = this.getFields().stream()
        .filter(field -> field.isAnnotationPresent(FilterType.class)
                && this.filters.containsKey(field.getName()))
        .flatMap(field -> {
            final String name = field.getName();
            final FilterType filterType = field.getAnnotation(FilterType.class);

            return this.filters.get(name).stream().map(value -> {
                IPredicateFilter<T, ?> filter = this.getPredicateFilter(filterType.type(), name, value);

                filter.extract();

                return filter.getPredicate(builder, root, query);
            });
        })
        .toArray(Predicate[]::new);

        return builder.and(predicates);
    }
}
