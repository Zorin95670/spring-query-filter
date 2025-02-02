# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)

## [Unreleased]

### Added

- Date format in query parameters for `DatePredicateFilter`.

### Removed

- `SpringQueryFilter` to uses `Pageable` of spring.

## [1.1.3] 2024/11/20

### Fixed

- Add missing `public` for constructors in `PredicateFilter` and `ComparablePredicateFilter`.

## [1.1.2] 2024/11/19

### Fixed

- Add missing module-info.java.

### Removed

- Lombok dependency.

## [1.1.1] 2024/11/18

### Fixed

- Fix deployment (gpg, javadoc and sources).

## [1.1.0] 2024/11/18

### Added

- Add gpg information.

## [1.0.0] 2024/11/18

### Added

* Setup library
* Add default filter for those types:
  * `String`
  * `Date`
  * `Integer`
  * `Long`
  * `Float`
  * `Double`
  * `Boolean`
  * `UUID`
* Add default pagination options:
  * `page`, the desired page.
  * `pageSize`, the number of elements per page.
  * `order`, field name to sort by.
  * `sort`, sort direction.
* Add default operators:
  * `eq_` or ` `: equals
  * `gt_`: greater than
  * `lt_`: lesser than
  * `_bt_`: between
  * `lk_`: like, with `*` or `%` as a wildcard equivalent to SQL `%`
  * `not_`: negation
  * `|`: or

[1.1.3]: https://github.com/Zorin95670/spring-query-filter/blob/1.1.3/changelog.md
[1.1.2]: https://github.com/Zorin95670/spring-query-filter/blob/1.1.2/changelog.md
[1.1.1]: https://github.com/Zorin95670/spring-query-filter/blob/1.1.1/changelog.md
[1.1.0]: https://github.com/Zorin95670/spring-query-filter/blob/1.1.0/changelog.md
[1.0.0]: https://github.com/Zorin95670/spring-query-filter/blob/1.0.0/changelog.md