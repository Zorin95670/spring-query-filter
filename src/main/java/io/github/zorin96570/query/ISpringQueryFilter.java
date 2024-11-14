package io.github.zorin96570.query;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

/**
 * Interface representing a query filter for pagination and sorting in a Spring application.
 * Provides methods to retrieve pagination parameters (page number and page size),
 * sorting criteria (order and sort direction), and utility methods to construct
 * {@link Sort} and {@link Pageable} objects based on the specified or default values.
 */
public interface ISpringQueryFilter {

    /**
     * Retrieves the page number for pagination.
     *
     * @return the page number (0-based index).
     */
    int getPage();

    /**
     * Retrieves the page size for pagination.
     *
     * @return the number of records per page.
     */
    int getPageSize();

    /**
     * Retrieves the field by which results should be ordered.
     *
     * @return the field name to order by.
     */
    String getOrder();

    /**
     * Indicates whether the sorting order is ascending.
     *
     * @return {@code true} if ascending order, {@code false} for descending.
     */
    boolean isAscendantSort();

    /**
     * Generates a {@link Sort} object based on the current sorting settings or a default field.
     *
     * @param defaultOrder the default field to sort by if no order field is specified.
     * @return a {@link Sort} object with the specified direction, or {@code null} if no order is specified.
     */
    default Sort getOrderBy(String defaultOrder) {
        String currentOrder = Optional.ofNullable(this.getOrder()).orElse(defaultOrder);

        if (currentOrder == null) {
            return null;
        }

        if (isAscendantSort()) {
            return Sort.by(Sort.Direction.ASC, currentOrder);
        }

        return Sort.by(Sort.Direction.DESC, currentOrder);
    }

    /**
     * Generates a {@link Pageable} object for pagination using default sorting.
     *
     * @return a {@link Pageable} object based on the page number, page size, and sorting criteria.
     */
    default Pageable getPageable() {
        return this.getPageable(null);
    }

    /**
     * Generates a {@link Pageable} object for pagination with a specified default order field.
     *
     * @param defaultOrder the default field to sort by if no order field is specified.
     * @return a {@link Pageable} object with the specified pagination and sorting settings.
     */
    default Pageable getPageable(String defaultOrder) {
        Sort sort = this.getOrderBy(defaultOrder);

        if (sort != null) {
            return PageRequest.of(this.getPage(), this.getPageSize(), sort);
        }

        return PageRequest.of(this.getPage(), this.getPageSize());
    }
}
