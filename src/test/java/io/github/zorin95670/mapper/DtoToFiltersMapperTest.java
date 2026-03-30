package io.github.zorin95670.mapper;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DtoToFiltersMapperTest {

    // Base DTO with some common fields
    static class BaseDto {
        List<String> baseTags;
        String baseName; // should be ignored
        List<String> nullList; // should be ignored (null)
    }

    // Derived DTO adds more fields
    static class DerivedDto extends BaseDto {
        List<String> derivedTags;
        Integer derivedNumber; // should be ignored
    }

    // Another DTO with overlapping field names
    static class AnotherDto {
        List<String> baseTags; // overlaps with BaseDto
        List<String> extraTags;
    }

    @Test
    void testToFiltersWithInheritanceAndNulls() {
        Map<String, List<String>> filters = new HashMap<>();
        DtoToFiltersMapper mapper = new DtoToFiltersMapper(filters);

        // Prepare DTOs
        DerivedDto dto1 = new DerivedDto();
        dto1.baseTags = Arrays.asList("a", "b", null);
        dto1.baseName = "ignoreMe";
        dto1.nullList = null;
        dto1.derivedTags = Arrays.asList("c", null);
        dto1.derivedNumber = 42;

        AnotherDto dto2 = new AnotherDto();
        dto2.baseTags = Arrays.asList("b", "d"); // overlaps with dto1.baseTags
        dto2.extraTags = Arrays.asList("x", "y");

        // Map DTOs to filters
        mapper.toFilters(dto1);
        mapper.toFilters(dto2);

        // Assertions
        assertEquals(3, filters.size(), "Should have 3 filter keys");

        // baseTags: merged a,b,d (ignore null, deduplicate)
        List<String> baseTags = filters.get("baseTags");
        assertNotNull(baseTags);
        assertEquals(3, baseTags.size()); // "a","b","d" from dto2 + "b" from dto1
        assertTrue(baseTags.containsAll(Arrays.asList("a", "b", "d")));

        // derivedTags: only from dto1
        List<String> derivedTags = filters.get("derivedTags");
        assertNotNull(derivedTags);
        assertEquals(1, derivedTags.size());
        assertTrue(derivedTags.contains("c"));

        // extraTags: only from dto2
        List<String> extraTags = filters.get("extraTags");
        assertNotNull(extraTags);
        assertEquals(2, extraTags.size());
        assertTrue(extraTags.containsAll(Arrays.asList("x", "y")));

        // nullList should be ignored
        assertFalse(filters.containsKey("nullList"));

        // baseName and derivedNumber should be ignored (not List)
        assertFalse(filters.containsKey("baseName"));
        assertFalse(filters.containsKey("derivedNumber"));
    }
}