package io.github.zorin95670.predicate;

import io.github.zorin95670.exception.SpringQueryFilterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@DisplayName("Test class: DatePredicateFilter")
class DatePredicateFilterTest {

    @Test
    @DisplayName("Test setDateFormat: should use default dateFormat on null")
    void testSetDateFormatWithNull() {
        var predicateFilter = new DatePredicateFilter<>("name", "value");

        assertEquals(new Date(1), predicateFilter.parseValue("1"));

        predicateFilter = new DatePredicateFilter<>("name", "value", "dd/mm");
        predicateFilter.setDateFormat(null);
        assertEquals(new Date(1), predicateFilter.parseValue("1"));
    }

    @Test
    @DisplayName("Test setDateFormat: should use specific dateFormat")
    void testSetDateFormat() throws ParseException {
        var predicateFilter = new DatePredicateFilter<>("name", "2024.01.01", "yyyy.MM.dd");
        var expectedDate = new SimpleDateFormat("yyyy.MM.dd").parse("2024.01.01");
        assertEquals(expectedDate, predicateFilter.parseValue("2024.01.01"));
    }

    @Test
    @DisplayName("Test setDateFormat: should throw exception on bad format")
    void testSetDateFormatThrowException() {
        SpringQueryFilterException exception = null;

        try {
            new DatePredicateFilter<>("name", "value", "INVALID");
        } catch (SpringQueryFilterException e) {
            exception = e;
        }


        assertNotNull(exception);
        assertEquals("Invalid date format: Unable to use 'INVALID' as date format.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("DATE_FORMAT", exception.getQueryFilterType());
        assertNull(exception.getQueryParameterName());
        assertEquals("INVALID", exception.getQueryParameterValue());
    }

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

    @Test
    @DisplayName("Test parseValue, should throw exception on invalid value with format")
    void testParseValueThrowExceptionWithFormat() {
        var predicateFilter = new DatePredicateFilter<>("name", "value", "yyyy.MM.dd");

        SpringQueryFilterException exception = null;

        try {
            predicateFilter.parseValue("bad");
        } catch (SpringQueryFilterException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Invalid date format: Unable to parse the value 'bad' as a date according the provided format 'yyyy.MM.dd'.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("DATE", exception.getQueryFilterType());
        assertEquals("name", exception.getQueryParameterName());
        assertEquals("bad", exception.getQueryParameterValue());
    }
}
