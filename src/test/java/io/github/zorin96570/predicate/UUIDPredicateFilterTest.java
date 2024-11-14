package io.github.zorin96570.predicate;

import io.github.zorin96570.exception.SpringQueryFilterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
@DisplayName("Test class: UUIDPredicateFilter")
class UUIDPredicateFilterTest {

    @Test
    @DisplayName("Test parseValue, should return valid UUID")
    void testParseValue() {
        var predicateFilter = new UUIDPredicateFilter<>("name", "value");

        var uuid = UUID.randomUUID();
        assertEquals(uuid, predicateFilter.parseValue(uuid.toString()));
    }

    @Test
    @DisplayName("Test parseValue, should throw exception on invalid value")
    void testParseValueThrowException() {
        var predicateFilter = new UUIDPredicateFilter<>("name", "value");

        SpringQueryFilterException exception = null;

        try {
            predicateFilter.parseValue("bad");
        } catch (SpringQueryFilterException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Invalid UUID format: Unable to parse the value 'bad' as a UUID.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("UUID", exception.getQueryFilterType());
        assertEquals("name", exception.getQueryParameterName());
        assertEquals("bad", exception.getQueryParameterValue());
    }
}
