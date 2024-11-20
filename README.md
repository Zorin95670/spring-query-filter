# Spring Query Filter

This Java library directly converts HTTP query parameters into Hibernate predicates, making it easier to implement dynamic filters for APIs.

## Installation and Dependencies

### Requirements

- **Java 21** or later.
- **Spring boot 3.X** for application configuration.
- **spring-boot-starter-data-jpa** for entity management and filtering predicates.

### Adding the Dependency

**Maven**

```xml
<dependency>
    <groupId>io.github.zorin95670</groupId>
    <artifactId>spring-query-filter</artifactId>
    <version>1.1.3</version>
</dependency>
```

**Gradle**

```groovy
dependencies {
    implementation("io.github.zorin95670:spring-query-filter:1.1.3")
}
```

## Usage in HTTP requests

### Pagination Parameters

The following query parameters manage pagination options:
- `page`: Integer starting at 0, representing the desired page.
- `pageSize`: Integer from 1 to 100, representing the number of elements per page. Defaults to 10.
- `order`: Field name to sort by.
- `sort`: Sort direction, either asc (ascending) or desc (descending). Defaults to descending.

**Example:**

```text
http://localhost:8080/myEndpoint?page=2&pageSize=7&order=name&sort=asc
```

### Filtering Operators

Here is the list of operator that can be used to filter data:
- `eq_` or ` `: equals 
- `gt_`: greater than
- `lt_`: lesser than
- `_bt_`: between
- `lk_`: like, with `*` or `%` as a wildcard equivalent to SQL `%`
- `not_`: negation
- `|`: or

| Type    | `eq_`               | `gt_`              | `lt_`              | `_bt_`             | `lk_`              |
|---------|---------------------|--------------------|--------------------|--------------------|--------------------|
| Boolean | :white_check_mark:  | :x:                | :x:                | :x:                | :x:                |
| String  | :white_check_mark:  | :x:                | :x:                | :x:                | :white_check_mark: |
| Integer | :white_check_mark:  | :white_check_mark: | :white_check_mark: | :white_check_mark: | :x:                |
| Long    | :white_check_mark:  | :white_check_mark: | :white_check_mark: | :white_check_mark: | :x:                |
| Float   | :white_check_mark:  | :white_check_mark: | :white_check_mark: | :white_check_mark: | :x:                |
| Double  | :white_check_mark:  | :white_check_mark: | :white_check_mark: | :white_check_mark: | :x:                |
| Date    | :white_check_mark:  | :white_check_mark: | :white_check_mark: | :white_check_mark: | :x:                |
| UUID    | :white_check_mark:  | :x:                | :x:                | :x:                | :x:                |


### Basic Filtering Example

**Example:**

```text
http://localhost:8080/myEndpoint?name=toto&age=gt_10&age=lt_20&updateDate=1_bt_5
```

This query filters `YourEntity` where:
- `name` is equal to `toto`.
- `age` is greater than 10 and less than 20.
- `updateDate` is between 1 and 5 (timestamps assumed, simplified here as integers).

Adding multiple query parameters combines filters with an SQL `AND`.

### Using `OR`

For a single field, you can specify an `OR` filter like this: `?name=toto|tata`, meaning name should be either `toto` or `tata`.

To mix `AND` and `OR`:

```text
?name=tata&name=toto|tutu
```

This corresponds to the SQL:

```sql
SELECT * FROM you_entity_table WHERE name = 'tata' AND (name = 'toto' OR name = 'tutu');
```

### Using `NOT`

To negate a filter, use the `not_` prefix. For `OR` filters, apply `not_` to each value.

- `?name=not_test`: name is not `test`
- `?name=not_lk_test*`: name does not match `test*`
- `?name=not_toto&name=not_tata`: name is neither `toto` nor `tata`

## Usage in Code

### Available Filters

This library provides default filters for:
- `String`
- `Date`
- `Integer`
- `Long`
- `Float`
- `Double`
- `Boolean`
- `UUID`

### Declaring Filters in Entities

To enable filtering on a field, annotate it with `@FilterType`.

**Example Entity:**

```java
import io.github.zorin95670.predicate.FilterType;
(...)

@Entity
@Table(name = "your_entity_table")
public class YourEntity {

    @Id
    @Column(name = "id")
    @FilterType(type = Long.class)
    private Long id;
    
    (...)
}
```

### Retrieving Query Parameters in a Controller

```java
import io.github.zorin95670.query.SpringQueryFilter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ExampleController {

    @GetMapping("/myEndpoint")
    public Page<MyEntity> find(@RequestParam Map<String, List<String>> allParams,
                               @ModelAttribute SpringQueryFilter queryFilter) {
        return myService.find(allParams, queryFilter);
    }
}
```

### Adding Methods in the Repository

```java
public interface YourEntityRepository extends JpaRepository<YourEntity, Long> {

    Page<YourEntity> find(Specification<YourEntity> specification, Pageable pageable);
}
```

### Using the Filter in a Service

**Service Interface:**

```java
public interface YourEntityService {
    (...)
    
    Page<YourEntity> find(Map<String, List<String>> filters, SpringQueryFilter queryFilter);
}
```

**Service Implementation:**

```java
@Service
@Transactional
public class YourEntityServiceImpl implements YourEntityService {
    (...)

    @Override
    public Page<YourEntity> find(final Map<String, List<String>> filters,
                                 final SpringQueryFilter queryFilter) {
        return this.yourEntityRepository.find(
                new QueryFilterSpecification<>(YourEntity.class, filters),
                queryFilter.getPageable() // or queryFilter.getPageable("name") to apply a default order.
        );
    }
}
```

You can define default sort in your service by using:
```java
queryFilter.getPageable("name");
```

### Filter without queryParameters

To apply filters without using query parameters directly from the controller, you can manually define filter conditions:

```java
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class YourEntityServiceImpl implements YourEntityService {
    (...)

    @Override
    public Page<YourEntity> specificFind() {
        Map<String, List<String>> filters = new HashMap<>();

        // Filter by id is not equal to 1
        filters.put("id", List.of("not_1"));
        // Filter by name that begin by "test"
        filters.put("name", List.of("lk_test%"));

        return this.yourEntityRepository.find(
                new QueryFilterSpecification<>(YourEntity.class, filters),
                // Current page, limit, field to order and sort
                new SpringQueryFilter(0, 10, "name", "asc").getPageable()
        );
    }
}
```
## Custom Pagination Options

If you need specific pagination settings, implement `ISpringQueryFilter` to define your own minimum, maximum, sort values, or any other pagination-related configurations.

```java
@NoArgsConstructor
@AllArgsConstructor
public class SpringQueryFilter implements ISpringQueryFilter {

    private static final int DEFAULT_PAGE_SIZE = 1;
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 10;
    private Integer page = 0;
    private Integer pageSize = 1;
    private String order;
    private String sort;

    @Override
    public int getComputedPage() {
        return Math.max(Optional.ofNullable(page).orElse(0), 0);
    }
    
    @Override
    public int getComputedSize() {
        return Math.clamp(Optional.ofNullable(pageSize).orElse(DEFAULT_PAGE_SIZE), MIN_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    @Override
    public String getOrder() {
        return this.order;
    }


    @Override
    public boolean isAscendantSort() {
        return "asc".equalsIgnoreCase(sort);
    }
}
```

This configuration allows you to control the min, max, sort values, and create a customized pageable option name in query parameters.

## Custom Types

If you need support for a custom type, you can extend `ComparablePredicateFilter`.

### Creating a Custom PredicateFilter

**Example:**

```java
public class YourTypePredicateFilter<T> extends ComparablePredicateFilter<T, YourType> {

    public YourTypePredicateFilter(String name, String value) {
        super(name, value);
    }

    @Override
    public YourType parseValue(String value) {
        // You have to parse the string value to YourType
        return YourType.parseYourType(value);
    }
}
```

For non-comparable types, extend `PredicateFilter` directly.

**Example:**

```java
public abstract class YourTypePredicateFilter<T> extends PredicateFilter<T, YourType> {
    YourTypePredicateFilter(String name, String value) {
        super(name, value);
    }

    @Override
    public YourType parseValue(String value) {
        // You have to parse the string value to YourType
        return YourType.parseYourType(value);
    }

    @Override
    public Predicate getPredicate(final int index, final CriteriaBuilder builder, final Expression<YourType> field) {
        // Example of content
        Predicate predicate;
        if (PredicateOperator.INFERIOR.equals(this.getOperator(index))) {
            predicate = builder.lessThan(field, parseValue(this.getValue(index)));
        } else if (PredicateOperator.SUPERIOR.equals(this.getOperator(index))) {
            predicate = builder.greaterThan(field, parseValue(this.getValue(index)));
        } else {
            predicate = builder.equal(field, parseValue(this.getValue(index)));
        }

        if (this.getIsNotOperator(index)) {
            return builder.not(predicate);
        }

        return predicate;
    }
}
```

### Using Your Custom PredicateFilter

Create a class that extends `QueryFilterSpecification` and override `getPredicateFilter`:

```java
public class CustomQueryFilterSpecification<T> extends QueryFilterSpecification<T> {

    public CustomQueryFilterSpecification(Class<T> entityClass, Map<String, List<String>> filters) {
        super(entityClass, filters);
    }

    @Override
    public IPredicateFilter<T, ?> getPredicateFilter(final Class<?> type, final String name, final String value) {
        if (Yourtype.class.equals(type)) {
            return new YourTypePredicateFilter<>(name, value);
        }
        
        // To manage default type
        return super.getPredicateFilter(type, name, value);
    }
}
```