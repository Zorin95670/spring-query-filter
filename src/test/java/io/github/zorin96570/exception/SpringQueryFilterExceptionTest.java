package io.github.zorin96570.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("unit")
@DisplayName("Test class: SpringQueryFilterException")
class SpringQueryFilterExceptionTest {

    @Test
    @DisplayName("Test all constructors, should set arguments.")
    void testConstructor() {
        var exception = new SpringQueryFilterException("type1", "name1", "value1");
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
        assertEquals( "type1", exception.getQueryFilterType());
        assertEquals("name1", exception.getQueryParameterName());
        assertEquals("value1", exception.getQueryParameterValue());

        exception = new SpringQueryFilterException("message2", "type2", "name2", "value2");
        assertEquals("message2", exception.getMessage());
        assertNull(exception.getCause());
        assertEquals("type2", exception.getQueryFilterType());
        assertEquals("name2", exception.getQueryParameterName());
        assertEquals("value2", exception.getQueryParameterValue());

        var cause = new NullPointerException();
        exception = new SpringQueryFilterException(cause, "type3", "name3", "value3");
        assertNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("type3", exception.getQueryFilterType());
        assertEquals("name3", exception.getQueryParameterName());
        assertEquals("value3", exception.getQueryParameterValue());

        cause = new NullPointerException();
        exception = new SpringQueryFilterException("message4", cause, "type4", "name4", "value4");
        assertEquals("message4", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("type4", exception.getQueryFilterType());
        assertEquals("name4", exception.getQueryParameterName());
        assertEquals("value4", exception.getQueryParameterValue());
    }
}
