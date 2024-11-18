package io.github.zorin95670.predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
@DisplayName("Test class: PredicateFilter")
class PredicateFilterTest {

    class TestPredicateFilter<T> extends PredicateFilter<T, String> {
        TestPredicateFilter(String name, String value) {
            super(name, value);
        }

        @Override
        public String parseValue(String value) {
            return value;
        }
    }

    @Test
    @DisplayName("Test setValues, should valid values")
    void testSetValues() {
        var filter = new TestPredicateFilter<>("name", "value1");
        var values = filter.getValues();
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("value1", values[0]);

        filter = new TestPredicateFilter<>("name", "value1|value2|value3");
        values = filter.getValues();
        assertNotNull(values);
        assertEquals(3, values.length);
        assertEquals("value1", values[0]);
        assertEquals("value2", values[1]);
        assertEquals("value3", values[2]);
    }
}
