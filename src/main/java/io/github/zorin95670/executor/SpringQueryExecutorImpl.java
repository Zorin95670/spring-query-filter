package io.github.zorin95670.executor;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default JPA Criteria API-based implementation of {@link SpringQueryExecutor}.
 *
 * <p>Every public method delegates to {@link #buildTypedQuery} to build a {@link TypedQuery},
 * and to {@link #buildPage} for paginated variants. See {@link SpringQueryExecutor} for the
 * projection, sorting, and pagination contract implemented here.</p>
 */
@Repository
public class SpringQueryExecutorImpl implements SpringQueryExecutor {

    /**
     * The JPA entity manager used to build and execute Criteria queries.
     */
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <T, R> List<R> find(final @Nonnull Class<T> entityClass,
                               final @Nonnull Class<R> resultType,
                               final @Nonnull Specification<T> specification) {
        return find(entityClass, resultType, specification, Sort.unsorted());
    }

    @Override
    public <T, R> List<R> find(final @Nonnull Class<T> entityClass,
                               final @Nonnull Class<R> resultType,
                               final @Nonnull Specification<T> specification,
                               final String... fieldNames) {
        return find(entityClass, resultType, specification, Sort.unsorted(), fieldNames);
    }

    @Override
    public <T, R> List<R> findDistinct(final @Nonnull Class<T> entityClass,
                                       final @Nonnull Class<R> resultType,
                                       final @Nonnull Specification<T> specification) {
        return findDistinct(entityClass, resultType, specification, Sort.unsorted());
    }

    @Override
    public <T, R> List<R> findDistinct(final @Nonnull Class<T> entityClass,
                                       final @Nonnull Class<R> resultType,
                                       final @Nonnull Specification<T> specification,
                                       final String... fieldNames) {
        return findDistinct(entityClass, resultType, specification, Sort.unsorted(), fieldNames);
    }

    @Override
    public <T, R> List<R> find(final @Nonnull Class<T> entityClass,
                               final @Nonnull Class<R> resultType,
                               final @Nonnull Specification<T> specification,
                               final @Nonnull Sort sort) {
        return buildTypedQuery(entityClass, resultType, specification, false, sort, getFieldNames(resultType))
            .getResultList();
    }

    @Override
    public <T, R> List<R> find(final @Nonnull Class<T> entityClass,
                               final @Nonnull Class<R> resultType,
                               final @Nonnull Specification<T> specification,
                               final @Nonnull Sort sort,
                               final String... fieldNames) {
        return buildTypedQuery(entityClass, resultType, specification, false, sort, fieldNames)
            .getResultList();
    }

    @Override
    public <T, R> List<R> findDistinct(final @Nonnull Class<T> entityClass,
                                       final @Nonnull Class<R> resultType,
                                       final @Nonnull Specification<T> specification,
                                       final @Nonnull Sort sort) {
        return buildTypedQuery(entityClass, resultType, specification, true, sort, getFieldNames(resultType))
            .getResultList();
    }

    @Override
    public <T, R> List<R> findDistinct(final @Nonnull Class<T> entityClass,
                                       final @Nonnull Class<R> resultType,
                                       final @Nonnull Specification<T> specification,
                                       final @Nonnull Sort sort,
                                       final String... fieldNames) {
        return buildTypedQuery(entityClass, resultType, specification, true, sort, fieldNames)
            .getResultList();
    }

    @Override
    public <T, R> Page<R> findPage(final @Nonnull Class<T> entityClass,
                                   final @Nonnull Class<R> resultType,
                                   final @Nonnull Specification<T> specification,
                                   final @Nonnull Pageable pageable) {
        return buildPage(entityClass, resultType, specification, false, pageable, getFieldNames(resultType));
    }

    @Override
    public <T, R> Page<R> findPage(final @Nonnull Class<T> entityClass,
                                   final @Nonnull Class<R> resultType,
                                   final @Nonnull Specification<T> specification,
                                   final @Nonnull Pageable pageable,
                                   final String... fieldNames) {
        return buildPage(entityClass, resultType, specification, false, pageable, fieldNames);
    }

    @Override
    public <T, R> Page<R> findDistinctPage(final @Nonnull Class<T> entityClass,
                                           final @Nonnull Class<R> resultType,
                                           final @Nonnull Specification<T> specification,
                                           final @Nonnull Pageable pageable) {
        return buildPage(entityClass, resultType, specification, true, pageable, getFieldNames(resultType));
    }

    @Override
    public <T, R> Page<R> findDistinctPage(final @Nonnull Class<T> entityClass,
                                           final @Nonnull Class<R> resultType,
                                           final @Nonnull Specification<T> specification,
                                           final @Nonnull Pageable pageable,
                                           final String... fieldNames) {
        return buildPage(entityClass, resultType, specification, true, pageable, fieldNames);
    }

    /**
     * Builds a {@link TypedQuery} from the given entity type, result type, specification,
     * distinct flag, sort, and projected field names.
     *
     * @param entityClass    the JPA entity type to query
     * @param resultType     the desired result type
     * @param specification  the filtering criteria; its {@code toPredicate} result may be
     *                       {@code null}, in which case no {@code WHERE} clause is applied
     * @param distinct       whether to apply {@code SELECT DISTINCT}
     * @param sort           the sort order to apply; ignored if {@link Sort#isSorted()} is
     *                       {@code false}
     * @param fieldNames     optional attribute names used to build the selection; see
     *                       {@link #buildSelection} for the projection rules
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return a {@link TypedQuery} ready to be executed or further configured (e.g. paging)
     */
    public <T, R> TypedQuery<R> buildTypedQuery(final @Nonnull Class<T> entityClass,
                                                final @Nonnull Class<R> resultType,
                                                final @Nonnull Specification<T> specification,
                                                final boolean distinct,
                                                final @Nonnull Sort sort,
                                                final String... fieldNames) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<R> query = cb.createQuery(resultType);
        Root<T> root = query.from(entityClass);
        Selection<? extends R> selection = buildSelection(cb, root, resultType, fieldNames);
        Predicate predicate = specification.toPredicate(root, query, cb);

        query.select(selection);

        if (predicate != null) {
            query.where(predicate);
        }

        if (distinct) {
            query.distinct(true);
        }

        if (sort.isSorted()) {
            query.orderBy(buildOrders(cb, root, sort));
        }

        return entityManager.createQuery(query);
    }

    /**
     * Builds a {@link Page} of results by executing the query built by {@link #buildTypedQuery}
     * with the offset/limit derived from {@code pageable}.
     *
     * <p>When {@code pageable} is unpaged, no separate {@code COUNT} query is issued: the total
     * is directly derived from the size of the returned content. Otherwise, a {@code COUNT}
     * query is executed via {@link #countResults} to compute the total number of matching
     * rows across all pages, using a strategy consistent with {@code distinct} and
     * {@code fieldNames} — see {@link #countResults} for details on why a plain
     * {@code COUNT(DISTINCT root)} would be incorrect for distinct field projections.</p>
     *
     * @param entityClass    the JPA entity type to query
     * @param resultType     the desired result type
     * @param specification  the filtering criteria
     * @param distinct       whether to apply {@code SELECT DISTINCT} to both the content and
     *                       count queries
     * @param pageable       the paging and sorting information
     * @param fieldNames     optional attribute names used to build the selection
     * @param <T>            the entity type
     * @param <R>            the result type
     * @return a {@link Page} of matching results
     * @throws IllegalArgumentException if {@code pageable.getOffset()} exceeds
     *                                  {@link Integer#MAX_VALUE}, since JPA's
     *                                  {@code setFirstResult(int)} cannot represent larger
     *                                  offsets
     */
    public <T, R> Page<R> buildPage(final @Nonnull Class<T> entityClass,
                                    final @Nonnull Class<R> resultType,
                                    final @Nonnull Specification<T> specification,
                                    final boolean distinct,
                                    final @Nonnull Pageable pageable,
                                    final String... fieldNames) {
        TypedQuery<R> typedQuery = buildTypedQuery(
            entityClass, resultType, specification, distinct, pageable.getSort(), fieldNames);

        if (pageable.isUnpaged()) {
            List<R> content = typedQuery.getResultList();
            return new PageImpl<>(content, pageable, content.size());
        }

        long offset = pageable.getOffset();
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "Pageable offset too large to be used with JPA setFirstResult(int): " + offset);
        }

        typedQuery.setFirstResult((int) offset);
        typedQuery.setMaxResults(pageable.getPageSize());

        List<R> content = typedQuery.getResultList();
        long total = countResults(entityClass, specification, distinct, fieldNames);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Computes the total number of rows matching {@code specification}, consistent with the
     * projection and distinctness of the corresponding content query.
     *
     * <p>The counting strategy depends on {@code distinct} and the number of
     * {@code fieldNames}, since a naive {@code COUNT(DISTINCT root)} only reflects distinct
     * <em>entities</em>, not distinct <em>projected values</em>:</p>
     * <ul>
     *     <li>{@code distinct == false} — plain {@code COUNT(root)} via
     *     {@link #countAll}: total number of matching rows, ignoring projection.</li>
     *     <li>{@code distinct == true} and no field names — {@code COUNT(DISTINCT root)} via
     *     {@link #countDistinctEntities}: number of distinct entities (by identity).</li>
     *     <li>{@code distinct == true} and exactly one field name —
     *     {@code COUNT(DISTINCT root.<field>)} via {@link #countDistinctSingleField}: number of
     *     distinct values of that single projected attribute.</li>
     *     <li>{@code distinct == true} and two or more field names —
     *     {@link #countDistinctMultipleFields}: number of distinct combinations of the
     *     projected attributes, computed by fetching all distinct tuples and counting them in
     *     memory, since standard JPA/JPQL has no portable way to express
     *     {@code COUNT(DISTINCT (a, b, ...))} at the database level.</li>
     * </ul>
     *
     * @param entityClass    the JPA entity type to query
     * @param specification  the filtering criteria; its {@code toPredicate} result may be
     *                       {@code null}, in which case no {@code WHERE} clause is applied
     * @param distinct       whether the corresponding content query applies
     *                       {@code SELECT DISTINCT}
     * @param fieldNames     the attribute names projected by the corresponding content query,
     *                       or none to count on the full entity
     * @param <T>            the entity type
     * @return the total number of matching rows, consistent with the content query's
     *         projection and distinctness
     */
    public <T> long countResults(final @Nonnull Class<T> entityClass,
                                 final @Nonnull Specification<T> specification,
                                 final boolean distinct,
                                 final String... fieldNames) {
        if (!distinct) {
            return countAll(entityClass, specification);
        }

        if (fieldNames == null || fieldNames.length == 0) {
            return countDistinctEntities(entityClass, specification);
        }

        if (fieldNames.length == 1) {
            return countDistinctSingleField(entityClass, specification, fieldNames[0]);
        }

        return countDistinctMultipleFields(entityClass, specification, fieldNames);
    }

    /**
     * Counts all rows matching {@code specification}, without regard to distinctness.
     *
     * @param entityClass    the JPA entity type to query
     * @param specification  the filtering criteria
     * @param <T>            the entity type
     * @return the total number of matching rows
     */
    public <T> long countAll(final @Nonnull Class<T> entityClass,
                             final @Nonnull Specification<T> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> root = countQuery.from(entityClass);
        Predicate predicate = specification.toPredicate(root, countQuery, cb);

        countQuery.select(cb.count(root));

        if (predicate != null) {
            countQuery.where(predicate);
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * Counts the number of distinct entities (by identity) matching {@code specification}.
     * Used when the content query projects the entity itself ({@code fieldNames} is empty).
     *
     * @param entityClass    the JPA entity type to query
     * @param specification  the filtering criteria
     * @param <T>            the entity type
     * @return the number of distinct matching entities
     */
    public <T> long countDistinctEntities(final @Nonnull Class<T> entityClass,
                                          final @Nonnull Specification<T> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> root = countQuery.from(entityClass);
        Predicate predicate = specification.toPredicate(root, countQuery, cb);

        countQuery.select(cb.countDistinct(root));

        if (predicate != null) {
            countQuery.where(predicate);
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * Counts the number of distinct values of a single projected attribute matching
     * {@code specification}. Used when the content query projects exactly one field.
     *
     * @param entityClass    the JPA entity type to query
     * @param specification  the filtering criteria
     * @param fieldName      the projected attribute name
     * @param <T>            the entity type
     * @return the number of distinct values of {@code fieldName}
     */
    public <T> long countDistinctSingleField(final @Nonnull Class<T> entityClass,
                                             final @Nonnull Specification<T> specification,
                                             final String fieldName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> root = countQuery.from(entityClass);
        Predicate predicate = specification.toPredicate(root, countQuery, cb);

        countQuery.select(cb.countDistinct(root.get(fieldName)));

        if (predicate != null) {
            countQuery.where(predicate);
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * Counts the number of distinct combinations of two or more projected attributes matching
     * {@code specification}.
     *
     * <p>Standard JPA/JPQL has no portable way to express {@code COUNT(DISTINCT (a, b, ...))}
     * at the database level. This method instead selects the distinct tuple of
     * {@code fieldNames} (without pagination) and counts the resulting rows in memory. This is
     * correct but, unlike the single-field and no-projection cases, does not push the counting
     * work down to the database — for very large result sets this may be noticeably more
     * expensive than the other counting strategies.</p>
     *
     * @param entityClass    the JPA entity type to query
     * @param specification  the filtering criteria
     * @param fieldNames     the projected attribute names (two or more)
     * @param <T>            the entity type
     * @return the number of distinct combinations of {@code fieldNames}
     */
    public <T> long countDistinctMultipleFields(final @Nonnull Class<T> entityClass,
                                                final @Nonnull Specification<T> specification,
                                                final String... fieldNames) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<T> root = query.from(entityClass);
        Predicate predicate = specification.toPredicate(root, query, cb);

        Selection<?>[] selections = Arrays.stream(fieldNames)
            .map(root::get)
            .toArray(Selection[]::new);

        query.select(cb.tuple(selections)).distinct(true);

        if (predicate != null) {
            query.where(predicate);
        }

        return entityManager.createQuery(query).getResultList().size();
    }

    /**
     * Translates a {@link Sort} into a list of JPA Criteria {@link Order} instances.
     *
     * <p>Only direct attributes of {@code root} are supported; nested (dotted) paths such as
     * {@code "address.city"} are not resolved and will cause an {@link IllegalArgumentException}
     * to be thrown by the underlying JPA provider.</p>
     *
     * @param cb    the criteria builder
     * @param root  the query root
     * @param sort  the sort to translate; must be sorted (callers should check
     *              {@link Sort#isSorted()} beforehand)
     * @param <T>   the entity type
     * @return the list of {@link Order} instances corresponding to {@code sort}
     */
    public <T> List<Order> buildOrders(final CriteriaBuilder cb,
                                       final Root<T> root,
                                       final @Nonnull Sort sort) {
        return sort.stream()
            .map(order -> {
                Path<?> path = root.get(order.getProperty());

                if (order.isAscending()) {
                    return cb.asc(path);
                }

                return cb.desc(path);
            })
            .collect(Collectors.toList());
    }

    /**
     * Builds the selection (projection) for a query based on the number of {@code fieldNames}
     * provided:
     * <ul>
     *     <li>zero field names — selects the entity itself ({@code root}), cast to
     *     {@code Selection<? extends R>}; the caller is responsible for ensuring
     *     {@code resultType} is compatible with {@code T};</li>
     *     <li>exactly one field name — selects the raw attribute path;</li>
     *     <li>two or more field names — builds a constructor expression via
     *     {@link CriteriaBuilder#construct}, requiring {@code resultType} to declare a matching
     *     public constructor.</li>
     * </ul>
     *
     * @param cb          the criteria builder
     * @param root        the query root
     * @param resultType  the desired result type
     * @param fieldNames  the attribute names to project, or none/{@code null} to select the
     *                    entity itself
     * @param <T>         the entity type
     * @param <R>         the result type
     * @return the {@link Selection} to use in the query
     */
    @SuppressWarnings("unchecked")
    public <T, R> Selection<? extends R> buildSelection(final CriteriaBuilder cb,
                                                        final Root<T> root,
                                                        final @Nonnull Class<R> resultType,
                                                        final String... fieldNames) {
        if (fieldNames == null || fieldNames.length == 0) {
            return (Selection<? extends R>) root;
        }

        if (fieldNames.length == 1) {
            return root.get(fieldNames[0]);
        }

        return cb.construct(
            resultType,
            Arrays.stream(fieldNames)
                .map(root::get)
                .toArray(Selection[]::new)
        );
    }

    /**
     * Retrieves the names of all fields declared by {@code resultType}, including private
     * fields, walking up the class hierarchy to collect fields declared by parent classes as
     * well.
     *
     * <p>The traversal stops once {@link Object} is reached, so fields declared by
     * {@code Object} itself are never included. Fields are returned in declaration order,
     * starting with those declared by {@code resultType} and followed by those declared by
     * each successive superclass.</p>
     *
     * <p>If a field is shadowed (i.e. redeclared with the same name in a subclass), its name
     * will appear more than once in the returned array, once for each class that declares
     * it.</p>
     *
     * @param resultType the class whose field names should be retrieved, must not be
     *                    {@code null}
     * @param <R>         the type of the class being inspected
     * @return an array of field names, never {@code null}; empty if {@code resultType}
     *         declares no fields and has no parent class other than {@link Object}
     */
    public <R> String[] getFieldNames(final @Nonnull Class<R> resultType) {
        List<String> fieldNames = new ArrayList<>();
        Class<?> currentClass = resultType;

        while (currentClass != null && currentClass != Object.class) {
            Arrays.stream(currentClass.getDeclaredFields())
                .map(Field::getName)
                .forEach(fieldNames::add);

            currentClass = currentClass.getSuperclass();
        }

        return fieldNames.toArray(new String[0]);
    }
}
