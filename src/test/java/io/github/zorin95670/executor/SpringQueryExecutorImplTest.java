package io.github.zorin95670.executor;

import io.github.zorin95670.specification.SpringQueryFilterSpecification;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestExecutorConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SpringQueryExecutorImplTest {

    @Autowired
    private MyEntityRepository repository;

    @Autowired
    private SpringQueryExecutorImpl executor;

    /**
     * DTO used to test multi-field constructor projection. Constructor parameter order must
     * match the order in which {@code text} and {@code numberInteger} are declared relative to
     * each other in {@link MyEntity} (adjust field order here if it doesn't match).
     */
    public static class TextNumberProjection {
        private final String text;
        private final Integer numberInteger;

        public TextNumberProjection(String text, Integer numberInteger) {
            this.text = text;
            this.numberInteger = numberInteger;
        }

        public String getText() {
            return text;
        }

        public Integer getNumberInteger() {
            return numberInteger;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TextNumberProjection other)) {
                return false;
            }
            return Objects.equals(text, other.text) && Objects.equals(numberInteger, other.numberInteger);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, numberInteger);
        }
    }

    MyEntity createEntity(int number, UUID uuid) {
        MyEntity entity = new MyEntity();

        entity.setText("text" + number);
        entity.setDate(new Date(10L * number));
        entity.setUuid(uuid);
        entity.setNumberInteger(100 * number);
        entity.setNumberFloat(1000.1f * number);
        entity.setNumberDouble(10000.1d * number);

        return entity;
    }

    Specification<MyEntity> noFilterSpecification() {
        return new SpringQueryFilterSpecification<>(MyEntity.class, new HashMap<>());
    }

    // ------------------------------------------------------------------
    // getFieldNames
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Test getFieldNames: should return all declared fields including private ones")
    void testGetFieldNamesReturnsAllDeclaredFields() {
        String[] fieldNames = executor.getFieldNames(MyEntity.class);

        assertNotNull(fieldNames);
        assertTrue(fieldNames.length > 0);
        assertTrue(List.of(fieldNames).contains("text"));
        assertTrue(List.of(fieldNames).contains("numberInteger"));
    }

    @Test
    @DisplayName("Test getFieldNames: should return an empty array for a class with no own fields")
    void testGetFieldNamesEmptyWhenNoFields() {
        class Empty { }

        String[] fieldNames = executor.getFieldNames(Empty.class);

        assertNotNull(fieldNames);
        assertEquals(0, fieldNames.length);
    }

    // ------------------------------------------------------------------
    // buildSelection (single field / multi field), via find/findDistinct
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Test find with a single field name: should return raw projected values")
    void testFindSingleFieldProjection() {
        repository.deleteAll();
        repository.flush();

        UUID uuid1 = UUID.randomUUID();
        repository.save(createEntity(1, uuid1));

        UUID uuid2 = UUID.randomUUID();
        repository.save(createEntity(2, uuid2));

        List<String> texts = executor.find(
            MyEntity.class, String.class, noFilterSpecification(), "text");

        assertNotNull(texts);
        assertEquals(2, texts.size());
        assertTrue(texts.contains("text1"));
        assertTrue(texts.contains("text2"));
    }

    @Test
    @Transactional
    @DisplayName("Test find with two or more field names: should build a constructor projection")
    void testFindMultipleFieldsProjection() {
        repository.deleteAll();
        repository.flush();

        repository.save(createEntity(1, UUID.randomUUID()));
        repository.save(createEntity(2, UUID.randomUUID()));

        List<TextNumberProjection> results = executor.find(
            MyEntity.class, TextNumberProjection.class, noFilterSpecification(), "text", "numberInteger");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(new TextNumberProjection("text1", 100)));
        assertTrue(results.contains(new TextNumberProjection("text2", 200)));
    }

    // ------------------------------------------------------------------
    // find (Sort, fieldNames)
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Test find with Sort and fieldNames: should return sorted projected results")
    void testFindWithSortAndFieldNames() {
        repository.deleteAll();
        repository.flush();

        repository.save(createEntity(2, UUID.randomUUID()));
        repository.save(createEntity(1, UUID.randomUUID()));

        List<String> texts = executor.find(
            MyEntity.class,
            String.class,
            noFilterSpecification(),
            Sort.by(Sort.Order.asc("text")),
            "text");

        assertNotNull(texts);
        assertEquals(List.of("text1", "text2"), texts);
    }

    // ------------------------------------------------------------------
    // findDistinct (fieldNames) / findDistinct (Sort, fieldNames)
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Test findDistinct with a single field name: should deduplicate identical projected values")
    void testFindDistinctSingleField() {
        repository.deleteAll();
        repository.flush();

        MyEntity entity1 = createEntity(1, UUID.randomUUID());
        entity1.setText("same");
        repository.save(entity1);

        MyEntity entity2 = createEntity(2, UUID.randomUUID());
        entity2.setText("same");
        repository.save(entity2);

        List<String> texts = executor.findDistinct(
            MyEntity.class, String.class, noFilterSpecification(), "text");

        assertNotNull(texts);
        assertEquals(1, texts.size());
        assertEquals("same", texts.getFirst());
    }

    @Test
    @Transactional
    @DisplayName("Test findDistinct with Sort and fieldNames: should return sorted, deduplicated results")
    void testFindDistinctWithSortAndFieldNames() {
        repository.deleteAll();
        repository.flush();

        MyEntity entity1 = createEntity(1, UUID.randomUUID());
        entity1.setText("same");
        repository.save(entity1);

        MyEntity entity2 = createEntity(2, UUID.randomUUID());
        entity2.setText("same");
        repository.save(entity2);

        MyEntity entity3 = createEntity(3, UUID.randomUUID());
        entity3.setText("other");
        repository.save(entity3);

        List<String> texts = executor.findDistinct(
            MyEntity.class,
            String.class,
            noFilterSpecification(),
            Sort.by(Sort.Order.asc("text")),
            "text");

        assertNotNull(texts);
        assertEquals(List.of("other", "same"), texts);
    }

    // ------------------------------------------------------------------
    // findPage / findDistinctPage
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Test findPage: should return a Page with correct content and total when paged")
    void testFindPagePaged() {
        repository.deleteAll();
        repository.flush();

        repository.save(createEntity(1, UUID.randomUUID()));
        repository.save(createEntity(2, UUID.randomUUID()));
        repository.save(createEntity(3, UUID.randomUUID()));

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Order.asc("text")));

        Page<String> page = executor.findPage(
            MyEntity.class, String.class, noFilterSpecification(), pageable, "text");

        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals(List.of("text1", "text2"), page.getContent());
    }

    @Test
    @Transactional
    @DisplayName("Test findPage: should skip the COUNT query and derive total from content size when unpaged")
    void testFindPageUnpaged() {
        repository.deleteAll();
        repository.flush();

        repository.save(createEntity(1, UUID.randomUUID()));
        repository.save(createEntity(2, UUID.randomUUID()));

        Page<String> page = executor.findPage(
            MyEntity.class, String.class, noFilterSpecification(), Pageable.unpaged(), "text");

        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @Transactional
    @DisplayName("Test findDistinctPage: should deduplicate rows and compute a consistent total for a single field")
    void testFindDistinctPageSingleField() {
        repository.deleteAll();
        repository.flush();

        MyEntity entity1 = createEntity(1, UUID.randomUUID());
        entity1.setText("same");
        repository.save(entity1);

        MyEntity entity2 = createEntity(2, UUID.randomUUID());
        entity2.setText("same");
        repository.save(entity2);

        MyEntity entity3 = createEntity(3, UUID.randomUUID());
        entity3.setText("other");
        repository.save(entity3);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("text")));

        Page<String> page = executor.findDistinctPage(
            MyEntity.class, String.class, noFilterSpecification(), pageable, "text");

        assertNotNull(page);
        assertEquals(2, page.getTotalElements());
        assertEquals(List.of("other", "same"), page.getContent());
    }

    @Test
    @Transactional
    @DisplayName("Test findDistinctPage: should deduplicate combinations and compute a consistent total for multiple fields")
    void testFindDistinctPageMultipleFields() {
        repository.deleteAll();
        repository.flush();

        MyEntity entity1 = createEntity(1, UUID.randomUUID());
        entity1.setText("same");
        entity1.setNumberInteger(100);
        repository.save(entity1);

        MyEntity entity2 = createEntity(2, UUID.randomUUID());
        entity2.setText("same");
        entity2.setNumberInteger(100);
        repository.save(entity2);

        MyEntity entity3 = createEntity(3, UUID.randomUUID());
        entity3.setText("same");
        entity3.setNumberInteger(999);
        repository.save(entity3);

        Pageable pageable = PageRequest.of(0, 10);

        Page<TextNumberProjection> page = executor.findDistinctPage(
            MyEntity.class, TextNumberProjection.class, noFilterSpecification(), pageable, "text", "numberInteger");

        assertNotNull(page);
        assertEquals(2, page.getTotalElements());
    }

    // ------------------------------------------------------------------
    // countAll / countDistinctEntities / countDistinctSingleField / countDistinctMultipleFields
    // (indirectly via countResults)
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Test countResults: distinct=false should count all matching rows")
    void testCountResultsNotDistinct() {
        repository.deleteAll();
        repository.flush();

        repository.save(createEntity(1, UUID.randomUUID()));
        repository.save(createEntity(2, UUID.randomUUID()));

        long total = executor.countResults(MyEntity.class, noFilterSpecification(), false);

        assertEquals(2, total);
    }

    @Test
    @Transactional
    @DisplayName("Test countResults: distinct=true with no field names should count distinct entities")
    void testCountResultsDistinctEntities() {
        repository.deleteAll();
        repository.flush();

        repository.save(createEntity(1, UUID.randomUUID()));
        repository.save(createEntity(2, UUID.randomUUID()));

        long total = executor.countResults(MyEntity.class, noFilterSpecification(), true);

        assertEquals(2, total);
    }

    @Test
    @Transactional
    @DisplayName("Test countResults: distinct=true with one field name should count distinct values of that field")
    void testCountResultsDistinctSingleField() {
        repository.deleteAll();
        repository.flush();

        MyEntity entity1 = createEntity(1, UUID.randomUUID());
        entity1.setText("same");
        repository.save(entity1);

        MyEntity entity2 = createEntity(2, UUID.randomUUID());
        entity2.setText("same");
        repository.save(entity2);

        long total = executor.countResults(MyEntity.class, noFilterSpecification(), true, "text");

        assertEquals(1, total);
    }

    @Test
    @Transactional
    @DisplayName("Test countResults: distinct=true with two or more field names should count distinct combinations")
    void testCountResultsDistinctMultipleFields() {
        repository.deleteAll();
        repository.flush();

        MyEntity entity1 = createEntity(1, UUID.randomUUID());
        entity1.setText("same");
        entity1.setNumberInteger(100);
        repository.save(entity1);

        MyEntity entity2 = createEntity(2, UUID.randomUUID());
        entity2.setText("same");
        entity2.setNumberInteger(100);
        repository.save(entity2);

        MyEntity entity3 = createEntity(3, UUID.randomUUID());
        entity3.setText("same");
        entity3.setNumberInteger(999);
        repository.save(entity3);

        long total = executor.countResults(
            MyEntity.class, noFilterSpecification(), true, "text", "numberInteger");

        assertEquals(2, total);
    }

    // ------------------------------------------------------------------
    // buildTypedQuery
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Test buildTypedQuery: should build an executable TypedQuery honoring distinct and sort")
    void testBuildTypedQuery() {
        repository.deleteAll();
        repository.flush();

        repository.save(createEntity(2, UUID.randomUUID()));
        repository.save(createEntity(1, UUID.randomUUID()));

        TypedQuery<String> query = executor.buildTypedQuery(
            MyEntity.class,
            String.class,
            noFilterSpecification(),
            false,
            Sort.by(Sort.Order.asc("text")),
            "text");

        List<String> results = query.getResultList();

        assertEquals(List.of("text1", "text2"), results);
    }
}