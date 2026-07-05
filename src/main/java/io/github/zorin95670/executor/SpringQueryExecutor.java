package io.github.zorin95670.executor;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Generic executor for building and running JPA Criteria queries from a {@link Specification},
 * with optional projection onto a subset of fields, sorting, and pagination.
 *
 * <p>Projection behavior depends on the number of {@code fieldNames} provided:</p>
 * <ul>
 *     <li><b>No field names</b> — the query selects the entity itself; {@code resultType} must
 *     be assignable from {@code entityClass} (or be {@code entityClass} itself).</li>
 *     <li><b>Exactly one field name</b> — the query selects the raw value of that single
 *     attribute; {@code resultType} must match the attribute's type.</li>
 *     <li><b>Two or more field names</b> — the query uses a JPA constructor expression
 *     ({@code CriteriaBuilder#construct}) to build a {@code resultType} instance. In that case,
 *     {@code resultType} must declare a public constructor whose parameter types and order
 *     exactly match the given field names.</li>
 * </ul>
 *
 * <p><b>Nested (dotted) field paths, e.g. {@code "address.city"}, are not supported.</b>
 * Only direct attributes of {@code entityClass} may be referenced in {@code fieldNames} or in
 * a {@link Sort}.</p>
 *
 * <p>All methods expect their non-nullable parameters to be provided as documented via
 * {@code @Nonnull}; passing {@code null} for any of them results in unspecified behavior,
 * typically a {@link NullPointerException} thrown while building or executing the query.</p>
 */
public interface SpringQueryExecutor {

    /**
     * Executes a query against {@code entityClass}, applying the given {@code specification} as
     * a filter, and returns the results without sorting.
     *
     * <p>The projected attribute names are derived automatically from the fields declared by
     * {@code resultType}, including private fields and those inherited from parent classes
     * (see the type-level Javadoc for the projection rules based on the number of fields
     * found).</p>
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return the list of matching results, never {@code null} but possibly empty
     */
    <T, R> List<R> find(@Nonnull Class<T> entityClass,
                        @Nonnull Class<R> resultType,
                        @Nonnull Specification<T> specification);

    /**
     * Executes a query against {@code entityClass}, applying the given {@code specification} as
     * a filter, and returns the results without sorting.
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param fieldNames     optional attribute names to project onto {@code resultType};
     *                       see the type-level Javadoc for the projection rules
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return the list of matching results, never {@code null} but possibly empty
     */
    <T, R> List<R> find(@Nonnull Class<T> entityClass,
                        @Nonnull Class<R> resultType,
                        @Nonnull Specification<T> specification,
                        String... fieldNames);

    /**
     * Same as {@link #find(Class, Class, Specification)}, but eliminates duplicate rows from
     * the result using {@code SELECT DISTINCT}.
     *
     * <p>The projected attribute names are derived automatically from the fields declared by
     * {@code resultType}, including private fields and those inherited from parent classes
     * (see the type-level Javadoc for the projection rules based on the number of fields
     * found).</p>
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return the distinct list of matching results, never {@code null} but possibly empty
     */
    <T, R> List<R> findDistinct(@Nonnull Class<T> entityClass,
                                @Nonnull Class<R> resultType,
                                @Nonnull Specification<T> specification);

    /**
     * Same as {@link #find(Class, Class, Specification, String...)}, but eliminates duplicate
     * rows from the result using {@code SELECT DISTINCT}.
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param fieldNames     optional attribute names to project onto {@code resultType};
     *                       see the type-level Javadoc for the projection rules
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return the distinct list of matching results, never {@code null} but possibly empty
     */
    <T, R> List<R> findDistinct(@Nonnull Class<T> entityClass,
                                @Nonnull Class<R> resultType,
                                @Nonnull Specification<T> specification,
                                String... fieldNames);

    /**
     * Executes a query against {@code entityClass}, applying the given {@code specification} as
     * a filter and {@code sort} as the ordering.
     *
     * <p>The projected attribute names are derived automatically from the fields declared by
     * {@code resultType}, including private fields and those inherited from parent classes
     * (see the type-level Javadoc for the projection rules based on the number of fields
     * found).</p>
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param sort           the sort order to apply; must reference direct attributes of
     *                       {@code entityClass} only (nested paths are not supported),
     *                       must not be {@code null}
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return the sorted list of matching results, never {@code null} but possibly empty
     */
    <T, R> List<R> find(@Nonnull Class<T> entityClass,
                        @Nonnull Class<R> resultType,
                        @Nonnull Specification<T> specification,
                        @Nonnull Sort sort);

    /**
     * Executes a query against {@code entityClass}, applying the given {@code specification} as
     * a filter and {@code sort} as the ordering.
     *
     * <p>Note: sorting is only guaranteed to work reliably when combined with
     * {@link #findDistinct} if the sorted attributes are also part of {@code fieldNames};
     * some databases require {@code ORDER BY} expressions to appear in the
     * {@code SELECT DISTINCT} list.</p>
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param sort           the sort order to apply; must reference direct attributes of
     *                       {@code entityClass} only (nested paths are not supported),
     *                       must not be {@code null}
     * @param fieldNames     optional attribute names to project onto {@code resultType};
     *                       see the type-level Javadoc for the projection rules
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return the sorted list of matching results, never {@code null} but possibly empty
     */
    <T, R> List<R> find(@Nonnull Class<T> entityClass,
                        @Nonnull Class<R> resultType,
                        @Nonnull Specification<T> specification,
                        @Nonnull Sort sort,
                        String... fieldNames);

    /**
     * Same as {@link #find(Class, Class, Specification, Sort)}, but eliminates duplicate rows
     * from the result using {@code SELECT DISTINCT}.
     *
     * <p>The projected attribute names are derived automatically from the fields declared by
     * {@code resultType}, including private fields and those inherited from parent classes
     * (see the type-level Javadoc for the projection rules based on the number of fields
     * found).</p>
     *
     * <p>Note: sorting is only guaranteed to work reliably when combined with distinct
     * projection if the sorted attributes are also part of {@code resultType}'s fields; some
     * databases require {@code ORDER BY} expressions to appear in the {@code SELECT DISTINCT}
     * list.</p>
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param sort           the sort order to apply; must reference direct attributes of
     *                       {@code entityClass} only (nested paths are not supported),
     *                       must not be {@code null}
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return the distinct, sorted list of matching results, never {@code null} but possibly
     *         empty
     */
    <T, R> List<R> findDistinct(@Nonnull Class<T> entityClass,
                                @Nonnull Class<R> resultType,
                                @Nonnull Specification<T> specification,
                                @Nonnull Sort sort);

    /**
     * Same as {@link #find(Class, Class, Specification, Sort, String...)}, but eliminates
     * duplicate rows from the result using {@code SELECT DISTINCT}.
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param sort           the sort order to apply; must reference direct attributes of
     *                       {@code entityClass} only (nested paths are not supported),
     *                       must not be {@code null}
     * @param fieldNames     optional attribute names to project onto {@code resultType};
     *                       see the type-level Javadoc for the projection rules
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return the distinct, sorted list of matching results, never {@code null} but possibly
     *         empty
     */
    <T, R> List<R> findDistinct(@Nonnull Class<T> entityClass,
                                @Nonnull Class<R> resultType,
                                @Nonnull Specification<T> specification,
                                @Nonnull Sort sort,
                                String... fieldNames);

    /**
     * Executes a paginated query against {@code entityClass}, applying the given
     * {@code specification} as a filter and the sort/paging information carried by
     * {@code pageable}.
     *
     * <p>The projected attribute names are derived automatically from the fields declared by
     * {@code resultType}, including private fields and those inherited from parent classes
     * (see the type-level Javadoc for the projection rules based on the number of fields
     * found).</p>
     *
     * <p>If {@code pageable} is unpaged (see {@link Pageable#isUnpaged()}), no additional
     * {@code COUNT} query is issued; the page's total is derived directly from the size of the
     * returned content. Otherwise, a separate {@code COUNT} query is executed against the same
     * {@code specification} to compute the total number of matching rows.</p>
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param pageable       the paging and sorting information, must not be {@code null}
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return a {@link Page} of matching results, never {@code null}
     * @throws IllegalArgumentException if {@code pageable.getOffset()} exceeds
     *                                  {@link Integer#MAX_VALUE}
     */
    <T, R> Page<R> findPage(@Nonnull Class<T> entityClass,
                            @Nonnull Class<R> resultType,
                            @Nonnull Specification<T> specification,
                            @Nonnull Pageable pageable);

    /**
     * Executes a paginated query against {@code entityClass}, applying the given
     * {@code specification} as a filter and the sort/paging information carried by
     * {@code pageable}.
     *
     * <p>If {@code pageable} is unpaged (see {@link Pageable#isUnpaged()}), no additional
     * {@code COUNT} query is issued; the page's total is derived directly from the size of the
     * returned content. Otherwise, a separate {@code COUNT} query is executed against the same
     * {@code specification} to compute the total number of matching rows.</p>
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param pageable       the paging and sorting information, must not be {@code null}
     * @param fieldNames     optional attribute names to project onto {@code resultType};
     *                       see the type-level Javadoc for the projection rules
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return a {@link Page} of matching results, never {@code null}
     * @throws IllegalArgumentException if {@code pageable.getOffset()} exceeds
     *                                  {@link Integer#MAX_VALUE}
     */
    <T, R> Page<R> findPage(@Nonnull Class<T> entityClass,
                            @Nonnull Class<R> resultType,
                            @Nonnull Specification<T> specification,
                            @Nonnull Pageable pageable,
                            String... fieldNames);

    /**
     * Same as {@link #findPage(Class, Class, Specification, Pageable)}, but eliminates
     * duplicate rows from the result (and the total count) using {@code SELECT DISTINCT}.
     *
     * <p>The projected attribute names are derived automatically from the fields declared by
     * {@code resultType}, including private fields and those inherited from parent classes
     * (see the type-level Javadoc for the projection rules based on the number of fields
     * found).</p>
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param pageable       the paging and sorting information, must not be {@code null}
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return a {@link Page} of distinct matching results, never {@code null}
     * @throws IllegalArgumentException if {@code pageable.getOffset()} exceeds
     *                                  {@link Integer#MAX_VALUE}
     */
    <T, R> Page<R> findDistinctPage(@Nonnull Class<T> entityClass,
                                    @Nonnull Class<R> resultType,
                                    @Nonnull Specification<T> specification,
                                    @Nonnull Pageable pageable);
    /**
     * Same as {@link #findPage(Class, Class, Specification, Pageable, String...)}, but
     * eliminates duplicate rows from the result (and the total count) using
     * {@code SELECT DISTINCT}.
     *
     * @param entityClass    the JPA entity type to query, must not be {@code null}
     * @param resultType     the desired result type, must not be {@code null}
     * @param specification  the filtering criteria, must not be {@code null}
     * @param pageable       the paging and sorting information, must not be {@code null}
     * @param fieldNames     optional attribute names to project onto {@code resultType};
     *                       see the type-level Javadoc for the projection rules
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return a {@link Page} of distinct matching results, never {@code null}
     * @throws IllegalArgumentException if {@code pageable.getOffset()} exceeds
     *                                  {@link Integer#MAX_VALUE}
     */
    <T, R> Page<R> findDistinctPage(@Nonnull Class<T> entityClass,
                                    @Nonnull Class<R> resultType,
                                    @Nonnull Specification<T> specification,
                                    @Nonnull Pageable pageable,
                                    String... fieldNames);
}
