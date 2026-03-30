package io.github.zorin95670.mapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper class to extract all List<String> fields from DTOs
 * and aggregate them into a filters map.
 *
 * <p>This class supports inheritance and only considers fields
 * that are of type List. Duplicate values are ignored.</p>
 */
public class DtoToFiltersMapper {

    /** Map to store aggregated filter values by field name. */
    private final Map<String, List<String>> filters;

    /**
     * Constructs a new DtoToFiltersMapper with the provided filters map.
     *
     * @param filters the map where field names will be mapped to their List<String> values
     */
    public DtoToFiltersMapper(final Map<String, List<String>> filters) {
        this.filters = filters;
    }

    /**
     * Extracts all List<String> values from the given DTO and adds them
     * to the filters map. Supports inherited fields.
     *
     * @param dto the DTO object to extract List<String> fields from
     */
    public void toFilters(final Object dto) {
        Class<?> clazz = dto.getClass();

        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (isListField(field)) {
                    addFieldValues(dto, field);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Checks whether the given field is of type List.
     *
     * @param field the field to check
     * @return true if the field is assignable from List, false otherwise
     */
    private boolean isListField(final Field field) {
        return List.class.isAssignableFrom(field.getType());
    }

    /**
     * Adds all non-null items from the List field of the given DTO
     * into the filters map under the field's name.
     * Duplicate values are ignored.
     *
     * @param dto the DTO object containing the field
     * @param field the List field to extract values from
     */
    private void addFieldValues(final Object dto, final Field field) {
        field.setAccessible(true);
        Object value = null;

        try {
            value = field.get(dto);
        } catch (IllegalAccessException e) {
            return;
        }

        if (value == null) {
            return;
        }

        List<?> list = (List<?>) value;
        List<String> targetList = filters.computeIfAbsent(field.getName(), k -> new ArrayList<>());

        for (Object item : list) {
            if (item != null) {
                String str = item.toString();
                if (!targetList.contains(str)) {
                    targetList.add(str);
                }
            }
        }
    }
}
