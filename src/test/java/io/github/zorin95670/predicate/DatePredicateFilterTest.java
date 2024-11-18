package io.github.zorin95670.predicate;

import io.github.zorin95670.exception.SpringQueryFilterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
@DisplayName("Test class: DatePredicateFilter")
class DatePredicateFilterTest {

    @Test
    @DisplayName("Test parseValue, should return valid Date")
    void testParseValue() {
        var predicateFilter = new DatePredicateFilter<>("name", "value");

        assertEquals(new Date(1), predicateFilter.parseValue("1"));
        assertEquals(new Date(2), predicateFilter.parseValue("2"));
    }

    @Test
    @DisplayName("Test parseValue, should throw exception on invalid value")
    void testParseValueThrowException() {
        var predicateFilter = new DatePredicateFilter<>("name", "value");

        SpringQueryFilterException exception = null;

        try {
            predicateFilter.parseValue("bad");
        } catch (SpringQueryFilterException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Invalid date format: Unable to parse the value 'bad' as a long timestamp.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("DATE", exception.getQueryFilterType());
        assertEquals("name", exception.getQueryParameterName());
        assertEquals("bad", exception.getQueryParameterValue());
    }
}
