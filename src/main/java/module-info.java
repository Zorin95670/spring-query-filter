module io.github.zorin95670 {
    exports io.github.zorin95670.exception;
    exports io.github.zorin95670.executor;
    exports io.github.zorin95670.predicate;
    exports io.github.zorin95670.specification;

    requires transitive jakarta.persistence;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires spring.context;
    requires jakarta.annotation;

    opens io.github.zorin95670.specification;
    opens io.github.zorin95670.executor;
}
