package io.github.zorin96570.predicate;

import io.github.zorin96570.exception.SpringQueryFilterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
@DisplayName("Test class: LongPredicateFilter")
class LongPredicateFilterTest {

    @Test
    @DisplayName("Test parseValue, should return valid Long")
    void testParseValue() {
        var predicateFilter = new LongPredicateFilter<>("name", "value");

        assertEquals(1L, predicateFilter.parseValue("1"));
        assertEquals(2L, predicateFilter.parseValue("2"));
    }

    @Test
    @DisplayName("Test parseValue, should throw exception on invalid value")
    void testParseValueThrowException() {
        var predicateFilter = new LongPredicateFilter<>("name", "value");

        SpringQueryFilterException exception = null;

        try {
            predicateFilter.parseValue("bad");
        } catch (SpringQueryFilterException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Invalid number format: Unable to parse the value 'bad' as a long.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("LONG", exception.getQueryFilterType());
        assertEquals("name", exception.getQueryParameterName());
        assertEquals("bad", exception.getQueryParameterValue());
    }
}
