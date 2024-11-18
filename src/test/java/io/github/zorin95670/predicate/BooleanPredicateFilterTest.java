package io.github.zorin95670.predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
@DisplayName("Test class: BooleanPredicateFilter")
class BooleanPredicateFilterTest {

    @Test
    @DisplayName("Test parseValue, should return valid Boolean")
    void testParseValue() {
        var predicateFilter = new BooleanPredicateFilter<>("name", "true");

        assertEquals(Boolean.TRUE, predicateFilter.parseValue("true"));
        assertEquals(Boolean.TRUE, predicateFilter.parseValue("True"));
        assertEquals(Boolean.TRUE, predicateFilter.parseValue("TRUE"));
        assertEquals(Boolean.FALSE, predicateFilter.parseValue("false"));
        assertEquals(Boolean.FALSE, predicateFilter.parseValue("False"));
        assertEquals(Boolean.FALSE, predicateFilter.parseValue("FALSE"));
        assertEquals(Boolean.FALSE, predicateFilter.parseValue("other"));
        assertEquals(Boolean.FALSE, predicateFilter.parseValue(""));
        assertEquals(Boolean.FALSE, predicateFilter.parseValue(null));
    }
}
