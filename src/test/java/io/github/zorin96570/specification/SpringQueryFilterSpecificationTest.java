package io.github.zorin96570.specification;

import io.github.zorin96570.exception.SpringQueryFilterException;
import io.github.zorin96570.predicate.BooleanPredicateFilter;
import io.github.zorin96570.predicate.DatePredicateFilter;
import io.github.zorin96570.predicate.DoublePredicateFilter;
import io.github.zorin96570.predicate.FloatPredicateFilter;
import io.github.zorin96570.predicate.IntegerPredicateFilter;
import io.github.zorin96570.predicate.LongPredicateFilter;
import io.github.zorin96570.predicate.StringPredicateFilter;
import io.github.zorin96570.predicate.UUIDPredicateFilter;
import io.github.zorin96570.query.SpringQueryFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.text.html.parser.Entity;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TestConfig.class)
class SpringQueryFilterSpecificationTest {

    @Autowired
    private MyEntityRepository repository;

    MyEntity createEntity(int number, UUID uuid) {
        MyEntity entity = new MyEntity();

        entity.setId((long) number);
        entity.setText("text" + number);
        entity.setDate(new Date(10L * number));
        entity.setUuid(uuid);
        entity.setNumberInteger(100 * number);
        entity.setNumberFloat(1000.1f * number);
        entity.setNumberDouble(10000.1d * number);

        return entity;
    }

    @Test
    @DisplayName("Test getPredicateFilter: should return valid Predicate")
    void testGetPredicateFilter() {
        var specification = new SpringQueryFilterSpecification<>(Entity.class, new HashMap<>());

        assertEquals(StringPredicateFilter.class, specification.getPredicateFilter(String.class, "name", "value").getClass());
        assertEquals(DatePredicateFilter.class, specification.getPredicateFilter(Date.class, "name", "value").getClass());
        assertEquals(IntegerPredicateFilter.class, specification.getPredicateFilter(Integer.class, "name", "value").getClass());
        assertEquals(LongPredicateFilter.class, specification.getPredicateFilter(Long.class, "name", "value").getClass());
        assertEquals(FloatPredicateFilter.class, specification.getPredicateFilter(Float.class, "name", "value").getClass());
        assertEquals(DoublePredicateFilter.class, specification.getPredicateFilter(Double.class, "name", "value").getClass());
        assertEquals(BooleanPredicateFilter.class, specification.getPredicateFilter(Boolean.class, "name", "value").getClass());
        assertEquals(UUIDPredicateFilter.class, specification.getPredicateFilter(UUID.class, "name", "value").getClass());
    }

    @Test
    @DisplayName("Test getPredicateFilter: should throw exception on unknown type")
    void testGetPredicateFilterThrowException() {
        var specification = new SpringQueryFilterSpecification<>(Entity.class, new HashMap<>());

        SpringQueryFilterException exception = null;

        try {
            specification.getPredicateFilter(Timestamp.class, "name", "value");
        } catch(SpringQueryFilterException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Unsupported filter type: 'Timestamp'. Valid types are String, Date, Integer, Long, Float,"
            + " Double, Boolean, UUID.", exception.getMessage());
        assertEquals("Timestamp", exception.getQueryFilterType());
        assertEquals("name", exception.getQueryParameterName());
        assertEquals("value", exception.getQueryParameterValue());
    }

    @Test
    @DisplayName("should return all entities without filters")
    void testShouldReturnAllEntitiesWithoutFilters() {
        repository.deleteAll();

        Map<String, List<String>> filters = new HashMap<>();
        var pagination = new SpringQueryFilter(0, 10, "text", "asc");

        UUID uuid1 = UUID.randomUUID();
        MyEntity entity1 = createEntity(1, uuid1);
        entity1 = repository.save(entity1);

        UUID uuid2 = UUID.randomUUID();
        MyEntity entity2 = createEntity(2, uuid2);
        entity2 = repository.save(entity2);

        List<MyEntity> entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());

        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals(entity1, entities.get(0));
        assertEquals(entity2, entities.get(1));

        filters.put("id", List.of());
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());

        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals(entity1, entities.get(0));
        assertEquals(entity2, entities.get(1));
    }

    @Test
    @DisplayName("should return valid entities depend of pagination")
    void testShouldReturnValidEntitiesDependOfPagination() {
        repository.deleteAll();

        UUID uuid1 = UUID.randomUUID();
        MyEntity entity1 = createEntity(1, uuid1);
        entity1.setText(null);
        entity1 = repository.save(entity1);

        UUID uuid2 = UUID.randomUUID();
        MyEntity entity2 = createEntity(2, uuid2);
        entity2 = repository.save(entity2);

        UUID uuid3 = UUID.randomUUID();
        MyEntity entity3 = createEntity(3, uuid3);
        entity3.setText(null);
        entity3 = repository.save(entity3);

        Map<String, List<String>> filters = new HashMap<>();
        filters.put("uuid", List.of(uuid1.toString()));
        var pagination = new SpringQueryFilter(0, 2, null, "asc");
        List<MyEntity> entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity1, entities.getFirst());

        pagination = new SpringQueryFilter(0, 2, "id", "desc");
        filters = new HashMap<>();
        filters.put("uuid", List.of(uuid1 + "|" + uuid2 + "|" + uuid3));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals(entity3, entities.getFirst());
        assertEquals(entity2, entities.getLast());

        pagination = new SpringQueryFilter(1, 2, "id", "desc");
        filters = new HashMap<>();
        filters.put("uuid", List.of(uuid1 + "|" + uuid2 + "|" + uuid3));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity1, entities.getFirst());
    }

    @Test
    @DisplayName("should return entity for specific filter")
    void testShouldSimpleFilter() {
        repository.deleteAll();

        UUID uuid1 = UUID.randomUUID();
        MyEntity entity1 = createEntity(1, uuid1);
        entity1.setText(null);
        entity1 = repository.save(entity1);

        UUID uuid2 = UUID.randomUUID();
        MyEntity entity2 = createEntity(2, uuid2);
        entity2 = repository.save(entity2);

        UUID uuid3 = UUID.randomUUID();
        MyEntity entity3 = createEntity(3, uuid3);
        entity3.setText(null);
        entity3 = repository.save(entity3);

        var pagination = new SpringQueryFilter(0, 10, "text", "asc");

        Map<String, List<String>> filters = new HashMap<>();
        filters.put("id", List.of("2"));
        List<MyEntity> entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity2, entities.getFirst());

        filters = new HashMap<>();
        filters.put("text", List.of("text2"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity2, entities.getFirst());

        filters = new HashMap<>();
        filters.put("date", List.of("20"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity2, entities.getFirst());

        filters = new HashMap<>();
        filters.put("uuid", List.of(uuid2.toString()));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity2, entities.getFirst());

        filters = new HashMap<>();
        filters.put("numberInteger", List.of("200"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity2, entities.getFirst());

        filters = new HashMap<>();
        filters.put("numberFloat", List.of("2000.2"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity2, entities.getFirst());

        filters = new HashMap<>();
        filters.put("numberDouble", List.of("eq_20000.2"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity2, entities.getFirst());

        filters = new HashMap<>();
        filters.put("id", List.of("not_2", "not_3"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity1, entities.getFirst());

        filters = new HashMap<>();
        filters.put("id", List.of("not_1"));
        filters.put("text", List.of("null"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity3, entities.getFirst());

        filters = new HashMap<>();
        filters.put("text", List.of("not_null"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity2, entities.getFirst());

        filters = new HashMap<>();
        filters.put("id", List.of("lt_2", "gt_0"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity1, entities.getFirst());

        filters = new HashMap<>();
        filters.put("id", List.of("0_bt_1"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity1, entities.getFirst());

        filters = new HashMap<>();
        filters.put("text", List.of("not_lk_*4", "lk_*2"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity2, entities.getFirst());

        filters = new HashMap<>();
        filters.put("id", List.of("1|2"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals(entity1, entities.getFirst());
        assertEquals(entity2, entities.getLast());filters = new HashMap<>();

        filters.put("text", List.of("text2", "not_text1"));
        entities = repository
                .findAll(new SpringQueryFilterSpecification<>(MyEntity.class, filters), pagination.getPageable());
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(entity2, entities.getFirst());
    }

}
