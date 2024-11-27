package io.github.zorin95670.predicate;

import io.github.zorin95670.exception.SpringQueryFilterException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A predicate filter specifically for handling {@link Date} values in a
 * {@link jakarta.persistence.criteria.CriteriaQuery}.
 * <p>
 * This class extends {@link ComparablePredicateFilter} to support filtering based on {@link Date} fields in
 * the database.
 * It handles different types of date comparisons and parses string values into {@link Date} objects.
 * </p>
 *
 * @param <T> The type of the entity being queried.
 */
public class DatePredicateFilter<T> extends ComparablePredicateFilter<T, Date> {

    /**
     * The {@link SimpleDateFormat} used to parse date strings into {@link Date} objects.
     * If {@code null}, date values are interpreted as long timestamps.
     */
    private SimpleDateFormat dateFormat;

    /**
     * Constructs a new {@link DatePredicateFilter} with the specified name and filter value.
     *
     * @param name The name of the field to filter by.
     * @param value The filter value(s) to apply.
     */
    public DatePredicateFilter(final String name, final String value) {
        this(name, value, null);
    }

    /**
     * Constructs a new {@link DatePredicateFilter} with the specified name, filter value and date format.
     *
     * @param name The name of the field to filter by.
     * @param value The filter value(s) to apply.
     * @param dateFormat The date format to apply.
     */
    public DatePredicateFilter(final String name, final String value, final String dateFormat) {
        super(name, value);
        this.setDateFormat(dateFormat);
    }

    /**
     * Sets the date format to be used for parsing date values.
     * <p>
     * If the provid ed date format is {@code null}, no format will be applied, and date values will be interpreted
     * as long timestamps. If the format is invalid, a {@link SpringQueryFilterException} is thrown.
     * </p>
     *
     * @param dateFormat The date format to apply, or {@code null} to disable format parsing.
     * @throws SpringQueryFilterException if the provided format is invalid.
     */
    public void setDateFormat(final String dateFormat) {
        if (dateFormat == null) {
            this.dateFormat = null;
            return;
        }

        try {
            this.dateFormat = new SimpleDateFormat(dateFormat);
        } catch (IllegalArgumentException exception) {
            throw new SpringQueryFilterException(
                    "Invalid date format: Unable to use '" + dateFormat + "' as date format.",
                    exception,
                    "DATE_FORMAT",
                    null,
                    dateFormat
            );
        }
    }

    /**
     * Parses the given string value into a {@link Date} object.
     * <p>
     * This method attempts to convert the string value into a {@link Date} by parsing it as a long timestamp.
     * If the value cannot be parsed as a valid timestamp, a {@link SpringQueryFilterException} is thrown.
     * </p>
     *
     * @param value The string value to be parsed.
     * @return The parsed {@link Date} object.
     * @throws SpringQueryFilterException if the value cannot be parsed into a valid {@link Date}.
     * @see Date#Date(long)
     */
    @Override
    public Date parseValue(final String value) {
        try {
            if (this.dateFormat == null) {
                return new Date(Long.parseLong(value));
            }
            return dateFormat.parse(value);
        } catch (NumberFormatException exception) {
            throw new SpringQueryFilterException(
                "Invalid date format: Unable to parse the value '" + value + "' as a long timestamp.",
                exception,
                "DATE",
                this.getName(),
                value
            );
        } catch (ParseException exception) {
            throw new SpringQueryFilterException(
                    "Invalid date format: Unable to parse the value '" + value + "' as a date according the provided "
                        + "format '" + this.dateFormat.toPattern() + "'.",
                    exception,
                    "DATE",
                    this.getName(),
                    value
            );
        }
    }
}
