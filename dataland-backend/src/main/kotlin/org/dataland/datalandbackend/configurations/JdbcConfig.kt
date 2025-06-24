package org.dataland.datalandbackend.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

/**
 * Configuration class for setting up the JdbcTemplate bean required for direct connection to the database.
 */
@Configuration
class JdbcConfig {
    /**
     * Creates a JdbcTemplate bean using the provided DataSource.
     * @param dataSource the DataSource to be used by the JdbcTemplate
     * @return a configured JdbcTemplate instance
     */
    @Bean
    fun jdbcTemplate(dataSource: DataSource): JdbcTemplate = JdbcTemplate(dataSource)
}
