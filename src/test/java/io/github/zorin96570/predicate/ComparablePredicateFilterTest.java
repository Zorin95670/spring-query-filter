package io.github.zorin96570.predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
@DisplayName("Test class: ComparablePredicateFilter")
class ComparablePredicateFilterTest {

    class TestPredicateFilter<T> extends ComparablePredicateFilter<T, String> {
        TestPredicateFilter(String name, String value) {
            super(name, value);
        }

        @Override
        public String parseValue(String value) {
            return value;
        }
    }

    @Test
    @DisplayName("Test setOperatorFromValue, should setValid operator and value")
    void testSetOperatorFromValue() {
        var predicateFilter = new TestPredicateFilter<>("name", "lt_value1|LT_value2");
        predicateFilter.extract();
        assertEquals(2, predicateFilter.getValues().length);
        assertEquals(PredicateOperator.INFERIOR, predicateFilter.getOperator(0));
        assertEquals("value1", predicateFilter.getValue(0));
        assertEquals(PredicateOperator.INFERIOR, predicateFilter.getOperator(1));
        assertEquals("value2", predicateFilter.getValue(1));

        predicateFilter = new TestPredicateFilter<>("name", "gt_value1|GT_value2");
        predicateFilter.extract();
        assertEquals(2, predicateFilter.getValues().length);
        assertEquals(PredicateOperator.SUPERIOR, predicateFilter.getOperator(0));
        assertEquals("value1", predicateFilter.getValue(0));
        assertEquals(PredicateOperator.SUPERIOR, predicateFilter.getOperator(1));
        assertEquals("value2", predicateFilter.getValue(1));

        predicateFilter = new TestPredicateFilter<>("name", "value1_bt_value2|value3_BT_value4");
        predicateFilter.extract();
        assertEquals(2, predicateFilter.getValues().length);
        assertEquals(PredicateOperator.BETWEEN, predicateFilter.getOperator(0));
        assertEquals("value1_bt_value2", predicateFilter.getValue(0));
        assertEquals(PredicateOperator.BETWEEN, predicateFilter.getOperator(1));
        assertEquals("value3_BT_value4", predicateFilter.getValue(1));
    }
}
