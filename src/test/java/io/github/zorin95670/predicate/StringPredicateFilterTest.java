package io.github.zorin95670.predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@DisplayName("Test class: StringPredicateFilter")
class StringPredicateFilterTest {

    @Test
    @DisplayName("Test parseValue, should return valid String")
    void testParseValue() {
        var predicateFilter = new StringPredicateFilter<>("name", "value");

        assertEquals("test", predicateFilter.parseValue("test"));
        assertEquals("", predicateFilter.parseValue(""));
        assertNull(predicateFilter.parseValue(null));
    }

    @Test
    @DisplayName("Test setOperatorFromValue: should set value without lk_")
    void testSetOperatorFromValue() {
        var predicateFilter = new StringPredicateFilter<>("name", "value|lk_test*");

        predicateFilter.extract();

        var values = predicateFilter.getValues();
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("VALUE", values[0]);
        assertEquals(PredicateOperator.EQUALS, predicateFilter.getOperator(0));
        assertEquals("TEST%", values[1]);
        assertEquals(PredicateOperator.LIKE, predicateFilter.getOperator(1));
    }
}
