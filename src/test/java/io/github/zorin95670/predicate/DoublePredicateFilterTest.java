package io.github.zorin95670.predicate;

import io.github.zorin95670.exception.SpringQueryFilterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
@DisplayName("Test class: DoublePredicateFilter")
class DoublePredicateFilterTest {

    @Test
    @DisplayName("Test parseValue, should return valid Double")
    void testParseValue() {
        var predicateFilter = new DoublePredicateFilter<>("name", "value");

        assertEquals(1.0d, predicateFilter.parseValue("1.0"));
        assertEquals(2.0d, predicateFilter.parseValue("2.0"));
    }

    @Test
    @DisplayName("Test parseValue, should throw exception on invalid value")
    void testParseValueThrowException() {
        var predicateFilter = new DoublePredicateFilter<>("name", "value");

        SpringQueryFilterException exception = null;

        try {
            predicateFilter.parseValue("bad");
        } catch (SpringQueryFilterException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Invalid number format: Unable to parse the value 'bad' as a double.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("DOUBLE", exception.getQueryFilterType());
        assertEquals("name", exception.getQueryParameterName());
        assertEquals("bad", exception.getQueryParameterValue());
    }
}
