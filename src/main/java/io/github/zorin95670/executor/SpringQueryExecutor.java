package io.github.zorin95670.executor;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Generic executor for building and executing JPA Criteria queries from a
 * {@link Specification}.
 *
 * <p>The executor supports filtering, projection, sorting, distinct queries,
 * and pagination.</p>
 *
 * <p>Two different projection strategies are provided:</p>
 *
 * <ul>
 *     <li>
 *         <b>Standard methods</b> ({@code find}, {@code findDistinct},
 *         {@code findPage}, and {@code findDistinctPage}) are intended for
 *         simple projections. The requested {@code fieldNames} explicitly
 *         determine which attributes are selected.
 *     </li>
 *     <li>
 *         <b>Entity methods</b> ({@code findEntities},
 *         {@code findDistinctEntities}, {@code findPageEntities}, and
 *         {@code findDistinctPageEntities}) are intended for reconstructing
 *         complete result objects. The implementation derives the available
 *         attributes from {@code resultType}, including fields inherited from
 *         its superclasses, and selects the public constructor with the
 *         greatest number of compatible parameters.
 *     </li>
 * </ul>
 *
 * <h2>Standard projection</h2>
 *
 * <p>For the standard methods, the projection behavior depends on the number
 * of {@code fieldNames}:</p>
 *
 * <ul>
 *     <li>
 *         <b>No field names</b> — the entity itself is selected. The
 *         {@code resultType} must therefore be compatible with the queried
 *         {@code entityClass}.
 *     </li>
 *     <li>
 *         <b>One field name</b> — the value of the corresponding entity
 *         attribute is returned directly. The {@code resultType} must be
 *         compatible with that attribute's type.
 *     </li>
 *     <li>
 *         <b>Multiple field names</b> — the selected values are used to
 *         construct an instance of {@code resultType}. A compatible
 *         constructor must be available.
 *     </li>
 * </ul>
 *
 * <h2>Entity projection</h2>
 *
 * <p>For the {@code *Entities} methods, the implementation inspects the
 * fields declared by {@code resultType} and its superclasses. These fields
 * determine the attributes selected from the queried entity.</p>
 *
 * <p>The result is then reconstructed using the public constructor of
 * {@code resultType} that accepts the greatest number of compatible selected
 * values. This allows result types to provide several constructors with
 * different levels of completeness while always preferring the most complete
 * compatible constructor.</p>
 *
 * <p>For example, if a result type provides constructors accepting two, four,
 * and six compatible attributes, the six-argument constructor is preferred
 * whenever all six values are available and compatible.</p>
 *
 * <h2>Attribute paths</h2>
 *
 * <p>Only direct attributes of {@code entityClass} are supported for
 * projections and sorting. Nested paths such as {@code "address.city"} are
 * not supported.</p>
 *
 * <h2>Nullability</h2>
 *
 * <p>All parameters annotated with {@link Nonnull} must be non-null.
 * Passing {@code null} for such a parameter typically results in a
 * {@link NullPointerException} while building or executing the query.</p>
 */
public interface SpringQueryExecutor {

    /**
     * Executes a query against {@code entityClass} using the supplied
     * {@code specification}.
     *
     * <p>No explicit projection or sorting is applied. The result is returned
     * directly as instances of {@code resultType}.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return the matching results, never {@code null} but possibly empty
     */
    <T, R> List<R> find(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification
    );

    /**
     * Executes a query and reconstructs each result as an instance of
     * {@code resultType}.
     *
     * <p>The fields of {@code resultType}, including inherited fields, are used
     * to determine the selected attributes. The public constructor accepting
     * the greatest number of compatible arguments is selected.</p>
     *
     * <p>No sorting is applied.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the result type to construct, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return the reconstructed results, never {@code null} but possibly empty
     */
    <T, R> List<R> findEntities(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification
    );

    /**
     * Executes a query against {@code entityClass} using the supplied
     * {@code specification} and explicit field projection.
     *
     * <p>When one field is specified, its value is returned directly.
     * When multiple fields are specified, they are used to construct
     * {@code resultType}.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param fieldNames    attributes to project, must reference direct
     *                      attributes of {@code entityClass}
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return the matching projected results, never {@code null} but possibly empty
     */
    <T, R> List<R> find(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        String... fieldNames
    );

    /**
     * Executes a distinct query against {@code entityClass}.
     *
     * <p>This method behaves like {@link #find(Class, Class, Specification)}
     * but removes duplicate results.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return distinct matching results, never {@code null} but possibly empty
     */
    <T, R> List<R> findDistinct(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification
    );

    /**
     * Executes a distinct query and reconstructs each result as an instance of
     * {@code resultType}.
     *
     * <p>The fields of {@code resultType}, including inherited fields, are used
     * to determine the selected attributes. The public constructor accepting
     * the greatest number of compatible arguments is selected.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the result type to construct, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return distinct reconstructed results, never {@code null} but possibly empty
     */
    <T, R> List<R> findDistinctEntities(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification
    );

    /**
     * Executes a distinct query with an explicit field projection.
     *
     * <p>This method behaves like
     * {@link #find(Class, Class, Specification, String...)} but removes
     * duplicate results.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param fieldNames    attributes to project, must reference direct
     *                      attributes of {@code entityClass}
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return distinct projected results, never {@code null} but possibly empty
     */
    <T, R> List<R> findDistinct(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        String... fieldNames
    );

    /**
     * Executes a query against {@code entityClass} with the supplied filter
     * and sort order.
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param sort          the sort order; only direct entity attributes are supported
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return sorted matching results, never {@code null} but possibly empty
     */
    <T, R> List<R> find(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Sort sort
    );

    /**
     * Executes a query with sorting and reconstructs each result as an instance
     * of {@code resultType}.
     *
     * <p>The fields declared by {@code resultType}, including inherited fields,
     * determine the selected attributes. The public constructor accepting the
     * greatest number of compatible arguments is selected.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the result type to construct, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param sort          the sort order; only direct entity attributes are supported
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return sorted reconstructed results, never {@code null} but possibly empty
     */
    <T, R> List<R> findEntities(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Sort sort
    );

    /**
     * Executes a distinct query with sorting.
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param sort          the sort order; only direct entity attributes are supported
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return distinct sorted results, never {@code null} but possibly empty
     */
    <T, R> List<R> findDistinct(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Sort sort
    );

    /**
     * Executes a distinct query with sorting and reconstructs each result as
     * an instance of {@code resultType}.
     *
     * <p>The public constructor accepting the greatest number of compatible
     * arguments is selected.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the result type to construct, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param sort          the sort order; only direct entity attributes are supported
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return distinct sorted reconstructed results, never {@code null} but possibly empty
     */
    <T, R> List<R> findDistinctEntities(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Sort sort
    );

    /**
     * Executes a query with sorting and an explicit field projection.
     *
     * <p>One field returns its value directly. Multiple fields are used to
     * construct {@code resultType}.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param sort          the sort order; only direct entity attributes are supported
     * @param fieldNames    attributes to project
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return sorted projected results, never {@code null} but possibly empty
     */
    <T, R> List<R> find(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Sort sort,
        String... fieldNames
    );

    /**
     * Executes a distinct query with sorting and an explicit field projection.
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param sort          the sort order; only direct entity attributes are supported
     * @param fieldNames    attributes to project
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return distinct sorted projected results, never {@code null} but possibly empty
     */
    <T, R> List<R> findDistinct(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Sort sort,
        String... fieldNames
    );

    /**
     * Executes a paginated query against {@code entityClass}.
     *
     * <p>The {@link Pageable} controls pagination and sorting. When it is
     * unpaged, no count query is required and the total is derived from the
     * returned content.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param pageable      pagination and sorting information, must not be {@code null}
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return a page of matching results, never {@code null}
     * @throws IllegalArgumentException if the offset exceeds {@link Integer#MAX_VALUE}
     */
    <T, R> Page<R> findPage(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Pageable pageable
    );

    /**
     * Executes a paginated query and reconstructs each result as an instance
     * of {@code resultType}.
     *
     * <p>The fields declared by {@code resultType}, including inherited fields,
     * determine the selected attributes. The public constructor accepting the
     * greatest number of compatible arguments is selected.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the result type to construct, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param pageable      pagination and sorting information, must not be {@code null}
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return a page of reconstructed results, never {@code null}
     * @throws IllegalArgumentException if the offset exceeds {@link Integer#MAX_VALUE}
     */
    <T, R> Page<R> findPageEntities(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Pageable pageable
    );

    /**
     * Executes a paginated query with an explicit field projection.
     *
     * <p>The {@code fieldNames} explicitly determine the selected attributes.
     * A single field returns its value directly, while multiple fields are used
     * to construct {@code resultType}.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param pageable      pagination and sorting information, must not be {@code null}
     * @param fieldNames    attributes to project
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return a page of projected results, never {@code null}
     * @throws IllegalArgumentException if the offset exceeds {@link Integer#MAX_VALUE}
     */
    <T, R> Page<R> findPage(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Pageable pageable,
        String... fieldNames
    );

    /**
     * Executes a distinct paginated query.
     *
     * <p>Both the returned content and the total count are distinct.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param pageable      pagination and sorting information, must not be {@code null}
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return a page of distinct matching results, never {@code null}
     * @throws IllegalArgumentException if the offset exceeds {@link Integer#MAX_VALUE}
     */
    <T, R> Page<R> findDistinctPage(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Pageable pageable
    );

    /**
     * Executes a distinct paginated query.
     *
     * <p>Both the returned content and the total count are distinct.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the desired result type, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param pageable      pagination and sorting information, must not be {@code null}
     * @param fieldNames    attributes to project
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return a page of distinct matching results, never {@code null}
     * @throws IllegalArgumentException if the offset exceeds {@link Integer#MAX_VALUE}
     */
    <T, R> Page<R> findDistinctPage(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Pageable pageable,
        String... fieldNames
    );

    /**
     * Executes a distinct paginated query and reconstructs each result as an
     * instance of {@code resultType}.
     *
     * <p>The fields declared by {@code resultType}, including inherited fields,
     * determine the selected attributes. The public constructor accepting the
     * greatest number of compatible arguments is selected.</p>
     *
     * <p>Both the returned content and the total count are distinct.</p>
     *
     * @param entityClass   the JPA entity type to query, must not be {@code null}
     * @param resultType    the result type to construct, must not be {@code null}
     * @param specification the filtering criteria, must not be {@code null}
     * @param pageable      pagination and sorting information, must not be {@code null}
     * @param <T>           the entity type
     * @param <R>           the result type
     * @return a page of distinct reconstructed results, never {@code null}
     * @throws IllegalArgumentException if the offset exceeds {@link Integer#MAX_VALUE}
     */
    <T, R> Page<R> findDistinctPageEntities(
        @Nonnull Class<T> entityClass,
        @Nonnull Class<R> resultType,
        @Nonnull Specification<T> specification,
        @Nonnull Pageable pageable
    );
}
