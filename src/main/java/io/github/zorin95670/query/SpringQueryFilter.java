package io.github.zorin95670.query;

import java.util.Optional;

/**
 * Class implementing {@link ISpringQueryFilter} for managing pagination and sorting parameters in a Spring application.
 * This class provides default values for pagination and handles sorting direction based on the `sort` field.
 */
public class SpringQueryFilter implements ISpringQueryFilter {

    /**
     * Minimum of elements by page.
     */
    private static final int MIN_PAGE_SIZE = 1;

    /**
     * Maximum of elements by page.
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Default number of elements by page.
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Page number for pagination (0-based index). Defaults to 0 if not specified.
     */
    private Integer page = 0;

    /**
     * Number of records per page. Defaults to 1 if not specified.
     */
    private Integer pageSize = 1;

    /**
     * Field name by which results should be ordered. Can be set to null.
     */
    private String order = null;

    /**
     * Sorting direction. Should be "asc" for ascending order; any other value is treated as descending.
     */
    private String sort = null;

    /**
     * Default no-argument constructor.
     */
    public SpringQueryFilter() {
    }

    /**
     * Constructor to initialize all properties.
     *
     * @param page     the page number (0-based index).
     * @param pageSize the number of records per page.
     * @param order    the field name by which results should be ordered.
     * @param sort     the sorting direction (ascending or descending).
     */
    public SpringQueryFilter(final Integer page, final Integer pageSize, final String order, final String sort) {
        this.page = page;
        this.pageSize = pageSize;
        this.order = order;
        this.sort = sort;
    }

    /**
     * Returns the page number for pagination (0-based index).
     * Defaults to 0 if not specified.
     *
     * @return the page number (0-based index)
     */
    public Integer getPage() {
        return page;
    }

    /**
     * Returns the number of records per page.
     * Defaults to 1 if not specified.
     *
     * @return the number of records per page
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Returns the field name by which results should be ordered.
     * Can be set to null.
     *
     * @return the field name for ordering, or null if not specified
     */
    public String getOrder() {
        return order;
    }

    /**
     * Returns the sorting direction.
     * Should be "asc" for ascending order; any other value is treated as descending.
     *
     * @return the sorting direction, either "asc" for ascending or any other value for descending
     */
    public String getSort() {
        return sort;
    }

    /**
     * Retrieves the page number for pagination, defaulting to 0 if the page is null or less than 0.
     *
     * @return the page number, constrained to be non-negative.
     */
    @Override
    public int getComputedPage() {
        return Math.max(Optional.ofNullable(getPage()).orElse(0), 0);
    }

    /**
     * Retrieves the page size for pagination, constrained between 1 and 10.
     * Defaults to 1 if page size is null or less than 1.
     *
     * @return the page size, bounded by a minimum of 1 and a maximum of 10.
     */
    @Override
    public int getComputedPageSize() {
        return Math.clamp(Optional.ofNullable(getPageSize()).orElse(DEFAULT_PAGE_SIZE), MIN_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    /**
     * Determines if the sort order is ascending.
     *
     * @return {@code true} if `sort` is set to "asc" (case-insensitive), otherwise {@code false}.
     */
    @Override
    public boolean isAscendantSort() {
        return "asc".equalsIgnoreCase(getSort());
    }
}
