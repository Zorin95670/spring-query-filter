package io.github.zorin96570.query;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * Class implementing {@link ISpringQueryFilter} for managing pagination and sorting parameters in a Spring application.
 * This class provides default values for pagination and handles sorting direction based on the `sort` field.
 */
@NoArgsConstructor
@AllArgsConstructor
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
    private String order;

    /**
     * Sorting direction. Should be "asc" for ascending order; any other value is treated as descending.
     */
    private String sort;

    /**
     * Retrieves the page number for pagination, defaulting to 0 if the page is null or less than 0.
     *
     * @return the page number, constrained to be non-negative.
     */
    @Override
    public int getPage() {
        return Math.max(Optional.ofNullable(page).orElse(0), 0);
    }

    /**
     * Retrieves the page size for pagination, constrained between 1 and 10.
     * Defaults to 1 if page size is null or less than 1.
     *
     * @return the page size, bounded by a minimum of 1 and a maximum of 10.
     */
    @Override
    public int getPageSize() {
        return Math.clamp(Optional.ofNullable(pageSize).orElse(DEFAULT_PAGE_SIZE), MIN_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    /**
     * Retrieves the field name by which results should be ordered.
     *
     * @return the order field, or null if not set.
     */
    @Override
    public String getOrder() {
        return this.order;
    }

    /**
     * Determines if the sort order is ascending.
     *
     * @return {@code true} if `sort` is set to "asc" (case-insensitive), otherwise {@code false}.
     */
    @Override
    public boolean isAscendantSort() {
        return "asc".equalsIgnoreCase(sort);
    }
}