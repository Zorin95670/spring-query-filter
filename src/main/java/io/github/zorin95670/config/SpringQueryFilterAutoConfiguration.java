package io.github.zorin95670.config;

import io.github.zorin95670.executor.SpringQueryExecutor;
import io.github.zorin95670.executor.SpringQueryExecutorImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that registers the default Spring Query Filter beans.
 *
 * <p>This configuration creates a {@link SpringQueryExecutor} bean when no
 * custom implementation is already defined in the application context.</p>
 */
@AutoConfiguration
public class SpringQueryFilterAutoConfiguration {

    /**
     * Creates the default Spring Query executor bean.
     *
     * @return the Spring Query executor instance
     */
    @Bean
    @ConditionalOnMissingBean(SpringQueryExecutor.class)
    public SpringQueryExecutor springQueryExecutor() {
        return new SpringQueryExecutorImpl();
    }
}
