package io.github.zorin96570.predicate;

import io.github.zorin96570.exception.SpringQueryFilterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
@DisplayName("Test class: IntegerPredicateFilter")
class IntegerPredicateFilterTest {

    @Test
    @DisplayName("Test parseValue, should return valid Integer")
    void testParseValue() {
        var predicateFilter = new IntegerPredicateFilter<>("name", "value");

        assertEquals(1, predicateFilter.parseValue("1"));
        assertEquals(2, predicateFilter.parseValue("2"));
    }

    @Test
    @DisplayName("Test parseValue, should throw exception on invalid value")
    void testParseValueThrowException() {
        var predicateFilter = new IntegerPredicateFilter<>("name", "value");

        SpringQueryFilterException exception = null;

        try {
            predicateFilter.parseValue("bad");
        } catch (SpringQueryFilterException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Invalid number format: Unable to parse the value 'bad' as a integer.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("INTEGER", exception.getQueryFilterType());
        assertEquals("name", exception.getQueryParameterName());
        assertEquals("bad", exception.getQueryParameterValue());
    }
}
