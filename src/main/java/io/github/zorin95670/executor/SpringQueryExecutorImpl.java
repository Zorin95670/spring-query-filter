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
    public <T, R> List<R> find(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification) {

        return find(
            entityClass,
            resultType,
            specification,
            Sort.unsorted()
        );
    }

    @Override
    public <T, R> List<R> find(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final String... fieldNames) {

        return find(
            entityClass,
            resultType,
            specification,
            Sort.unsorted(),
            fieldNames
        );
    }

    @Override
    public <T, R> List<R> findDistinct(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification) {

        return findDistinct(
            entityClass,
            resultType,
            specification,
            Sort.unsorted()
        );
    }

    @Override
    public <T, R> List<R> findDistinct(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final String... fieldNames) {

        return findDistinct(
            entityClass,
            resultType,
            specification,
            Sort.unsorted(),
            fieldNames
        );
    }

    @Override
    public <T, R> List<R> find(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final @Nonnull Sort sort) {

        String[] fieldNames = getFieldNames(resultType);

        return executeList(
            entityClass,
            resultType,
            specification,
            false,
            sort,
            fieldNames
        );
    }

    @Override
    public <T, R> List<R> find(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final @Nonnull Sort sort,
        final String... fieldNames) {

        return executeList(
            entityClass,
            resultType,
            specification,
            false,
            sort,
            fieldNames
        );
    }

    @Override
    public <T, R> List<R> findDistinct(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final @Nonnull Sort sort) {

        String[] fieldNames = getFieldNames(resultType);

        return executeList(
            entityClass,
            resultType,
            specification,
            true,
            sort,
            fieldNames
        );
    }

    @Override
    public <T, R> List<R> findDistinct(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final @Nonnull Sort sort,
        final String... fieldNames) {

        return executeList(
            entityClass,
            resultType,
            specification,
            true,
            sort,
            fieldNames
        );
    }

    @Override
    public <T, R> Page<R> findPage(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final @Nonnull Pageable pageable) {

        return buildPage(
            entityClass,
            resultType,
            specification,
            false,
            pageable,
            getFieldNames(resultType)
        );
    }

    @Override
    public <T, R> Page<R> findPage(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final @Nonnull Pageable pageable,
        final String... fieldNames) {

        return buildPage(
            entityClass,
            resultType,
            specification,
            false,
            pageable,
            fieldNames
        );
    }

    @Override
    public <T, R> Page<R> findDistinctPage(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final @Nonnull Pageable pageable) {

        return buildPage(
            entityClass,
            resultType,
            specification,
            true,
            pageable,
            getFieldNames(resultType)
        );
    }

    @Override
    public <T, R> Page<R> findDistinctPage(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final @Nonnull Pageable pageable,
        final String... fieldNames) {

        return buildPage(
            entityClass,
            resultType,
            specification,
            true,
            pageable,
            fieldNames
        );
    }

    /**
     * Executes a query and returns the mapped result list.
     *
     * <p>When {@code entityClass} and {@code resultType} are the same, the entity
     * query is used directly. When they differ, the requested fields are selected
     * as a {@link Tuple} and mapped to the result type.</p>
     */
    private <T, R> List<R> executeList(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final boolean distinct,
        final @Nonnull Sort sort,
        final String... fieldNames) {

        if (!entityClass.equals(resultType)
            && fieldNames != null
            && fieldNames.length > 0) {

            return executeProjection(
                entityClass,
                resultType,
                specification,
                distinct,
                sort,
                fieldNames
            );
        }

        return buildTypedQuery(
            entityClass,
            resultType,
            specification,
            distinct,
            sort,
            fieldNames
        ).getResultList();
    }

    /**
     * Executes a projection from one entity type to another result type.
     *
     * <p>The source entity is used as the query root, which means that the
     * {@link Specification} can filter on fields belonging to the source entity.
     * Only fields declared by the result type are selected.</p>
     */
    private <T, R> List<R> executeProjection(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final boolean distinct,
        final @Nonnull Sort sort,
        final String... fieldNames) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<T> root = query.from(entityClass);

        Selection<?>[] selections = Arrays.stream(fieldNames)
            .map(fieldName -> root.get(fieldName).alias(fieldName))
            .toArray(Selection[]::new);

        query.multiselect(selections);

        Predicate predicate = specification.toPredicate(
            root,
            query,
            cb
        );

        if (predicate != null) {
            query.where(predicate);
        }

        if (distinct) {
            query.distinct(true);
        }

        if (sort.isSorted()) {
            query.orderBy(
                buildOrders(cb, root, sort)
            );
        }

        List<Tuple> tuples = entityManager
            .createQuery(query)
            .getResultList();

        return tuples.stream()
            .map(tuple -> mapTuple(tuple, resultType, fieldNames))
            .toList();
    }

    /**
     * Builds a regular typed JPA query.
     *
     * <p>This method is used when the source entity and result type are the same,
     * or when a constructor projection is explicitly requested.</p>
     */
    public <T, R> TypedQuery<R> buildTypedQuery(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final boolean distinct,
        final @Nonnull Sort sort,
        final String... fieldNames) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<R> query = cb.createQuery(resultType);

        Root<T> root = query.from(entityClass);

        Selection<? extends R> selection =
            buildSelection(
                cb,
                root,
                resultType,
                fieldNames
            );

        Predicate predicate =
            specification.toPredicate(
                root,
                query,
                cb
            );

        query.select(selection);

        if (predicate != null) {
            query.where(predicate);
        }

        if (distinct) {
            query.distinct(true);
        }

        if (sort.isSorted()) {
            query.orderBy(
                buildOrders(cb, root, sort)
            );
        }

        return entityManager.createQuery(query);
    }

    /**
     * Builds a paginated result.
     */
    public <T, R> Page<R> buildPage(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final boolean distinct,
        final @Nonnull Pageable pageable,
        final String... fieldNames) {

        /*
         * When source and result types differ, we cannot use TypedQuery<R>
         * because Hibernate would try to instantiate R using a constructor
         * expression. Execute a Tuple projection instead.
         */
        if (!entityClass.equals(resultType)
            && fieldNames != null
            && fieldNames.length > 0) {

            List<R> content = executeProjectionPage(
                entityClass,
                resultType,
                specification,
                distinct,
                pageable,
                fieldNames
            );

            if (pageable.isUnpaged()) {
                return new PageImpl<>(
                    content,
                    pageable,
                    content.size()
                );
            }

            long total = countResults(
                entityClass,
                specification,
                distinct,
                fieldNames
            );

            return new PageImpl<>(
                content,
                pageable,
                total
            );
        }

        TypedQuery<R> typedQuery = buildTypedQuery(
            entityClass,
            resultType,
            specification,
            distinct,
            pageable.getSort(),
            fieldNames
        );

        if (pageable.isUnpaged()) {
            List<R> content = typedQuery.getResultList();

            return new PageImpl<>(
                content,
                pageable,
                content.size()
            );
        }

        long offset = pageable.getOffset();

        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "Pageable offset too large to be used with JPA "
                    + "setFirstResult(int): " + offset
            );
        }

        typedQuery.setFirstResult((int) offset);
        typedQuery.setMaxResults(pageable.getPageSize());

        List<R> content = typedQuery.getResultList();

        long total = countResults(
            entityClass,
            specification,
            distinct,
            fieldNames
        );

        return new PageImpl<>(
            content,
            pageable,
            total
        );
    }

    /**
     * Executes a paginated Tuple projection.
     */
    private <T, R> List<R> executeProjectionPage(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Class<R> resultType,
        final @Nonnull Specification<T> specification,
        final boolean distinct,
        final @Nonnull Pageable pageable,
        final String... fieldNames) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<T> root = query.from(entityClass);

        Selection<?>[] selections = Arrays.stream(fieldNames)
            .map(fieldName -> root.get(fieldName).alias(fieldName))
            .toArray(Selection[]::new);

        query.multiselect(selections);

        Predicate predicate =
            specification.toPredicate(
                root,
                query,
                cb
            );

        if (predicate != null) {
            query.where(predicate);
        }

        if (distinct) {
            query.distinct(true);
        }

        if (pageable.getSort().isSorted()) {
            query.orderBy(
                buildOrders(
                    cb,
                    root,
                    pageable.getSort()
                )
            );
        }

        var typedQuery = entityManager.createQuery(query);

        if (!pageable.isUnpaged()) {
            long offset = pageable.getOffset();

            if (offset > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                    "Pageable offset too large to be used with JPA "
                        + "setFirstResult(int): " + offset
                );
            }

            typedQuery.setFirstResult((int) offset);
            typedQuery.setMaxResults(pageable.getPageSize());
        }

        return typedQuery.getResultList()
            .stream()
            .map(tuple -> mapTuple(
                tuple,
                resultType,
                fieldNames
            ))
            .toList();
    }

    /**
     * Maps a Tuple to an instance of the requested result type.
     *
     * <p>Fields declared by the result type and its superclasses are populated
     * using the aliases defined on the Tuple selections.</p>
     */
    private <R> R mapTuple(
        final @Nonnull Tuple tuple,
        final @Nonnull Class<R> resultType,
        final String... fieldNames) {

        try {
            R instance = resultType
                .getDeclaredConstructor()
                .newInstance();

            List<String> projectedFields =
                Arrays.asList(fieldNames);

            Class<?> currentClass = resultType;

            while (currentClass != null
                && currentClass != Object.class) {

                for (Field field : currentClass.getDeclaredFields()) {

                    if (!projectedFields.contains(field.getName())) {
                        continue;
                    }

                    field.setAccessible(true);

                    Object value = tuple.get(field.getName());

                    field.set(instance, value);
                }

                currentClass = currentClass.getSuperclass();
            }

            return instance;

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                "Unable to map Tuple to "
                    + resultType.getName(),
                e
            );
        }
    }

    /**
     * Computes the total number of rows matching the specification.
     */
    public <T> long countResults(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Specification<T> specification,
        final boolean distinct,
        final String... fieldNames) {

        if (!distinct) {
            return countAll(
                entityClass,
                specification
            );
        }

        if (fieldNames == null
            || fieldNames.length == 0) {

            return countDistinctEntities(
                entityClass,
                specification
            );
        }

        if (fieldNames.length == 1) {
            return countDistinctSingleField(
                entityClass,
                specification,
                fieldNames[0]
            );
        }

        return countDistinctMultipleFields(
            entityClass,
            specification,
            fieldNames
        );
    }

    /**
     * Counts all rows matching the specification.
     */
    public <T> long countAll(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Specification<T> specification) {

        CriteriaBuilder cb =
            entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery =
            cb.createQuery(Long.class);

        Root<T> root =
            countQuery.from(entityClass);

        Predicate predicate =
            specification.toPredicate(
                root,
                countQuery,
                cb
            );

        countQuery.select(
            cb.count(root)
        );

        if (predicate != null) {
            countQuery.where(predicate);
        }

        return entityManager
            .createQuery(countQuery)
            .getSingleResult();
    }

    /**
     * Counts distinct entities.
     */
    public <T> long countDistinctEntities(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Specification<T> specification) {

        CriteriaBuilder cb =
            entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery =
            cb.createQuery(Long.class);

        Root<T> root =
            countQuery.from(entityClass);

        Predicate predicate =
            specification.toPredicate(
                root,
                countQuery,
                cb
            );

        countQuery.select(
            cb.countDistinct(root)
        );

        if (predicate != null) {
            countQuery.where(predicate);
        }

        return entityManager
            .createQuery(countQuery)
            .getSingleResult();
    }

    /**
     * Counts distinct values of a single field.
     */
    public <T> long countDistinctSingleField(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Specification<T> specification,
        final String fieldName) {

        CriteriaBuilder cb =
            entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery =
            cb.createQuery(Long.class);

        Root<T> root =
            countQuery.from(entityClass);

        Predicate predicate =
            specification.toPredicate(
                root,
                countQuery,
                cb
            );

        countQuery.select(
            cb.countDistinct(
                root.get(fieldName)
            )
        );

        if (predicate != null) {
            countQuery.where(predicate);
        }

        return entityManager
            .createQuery(countQuery)
            .getSingleResult();
    }

    /**
     * Counts distinct combinations of multiple fields.
     */
    public <T> long countDistinctMultipleFields(
        final @Nonnull Class<T> entityClass,
        final @Nonnull Specification<T> specification,
        final String... fieldNames) {

        CriteriaBuilder cb =
            entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query =
            cb.createTupleQuery();

        Root<T> root =
            query.from(entityClass);

        Predicate predicate =
            specification.toPredicate(
                root,
                query,
                cb
            );

        Selection<?>[] selections = Arrays.stream(fieldNames)
            .map(fieldName ->
                root.get(fieldName).alias(fieldName)
            )
            .toArray(Selection[]::new);

        query.multiselect(selections);
        query.distinct(true);

        if (predicate != null) {
            query.where(predicate);
        }

        return entityManager
            .createQuery(query)
            .getResultList()
            .size();
    }

    /**
     * Translates a Spring Data Sort into JPA Criteria orders.
     */
    public <T> List<Order> buildOrders(
        final CriteriaBuilder cb,
        final Root<T> root,
        final @Nonnull Sort sort) {

        return sort.stream()
            .map(order -> {

                Path<?> path =
                    root.get(order.getProperty());

                if (order.isAscending()) {
                    return cb.asc(path);
                }

                return cb.desc(path);
            })
            .collect(Collectors.toList());
    }

    /**
     * Builds a selection for regular typed queries.
     *
     * <p>This method is only used when the source entity and result type are
     * compatible with the CriteriaQuery result type.</p>
     */
    @SuppressWarnings("unchecked")
    public <T, R> Selection<? extends R> buildSelection(
        final CriteriaBuilder cb,
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
     * Retrieves all field names declared by the result type and its
     * superclasses.
     */
    public <R> String[] getFieldNames(
        final @Nonnull Class<R> resultType) {

        List<String> fieldNames =
            new ArrayList<>();

        Class<?> currentClass =
            resultType;

        while (currentClass != null
            && currentClass != Object.class) {

            Arrays.stream(
                    currentClass.getDeclaredFields()
                )
                .map(Field::getName)
                .forEach(fieldNames::add);

            currentClass =
                currentClass.getSuperclass();
        }

        return fieldNames.toArray(new String[0]);
    }
}