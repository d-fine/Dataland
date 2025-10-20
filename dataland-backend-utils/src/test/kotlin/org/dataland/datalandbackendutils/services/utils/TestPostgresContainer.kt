package org.dataland.datalandbackendutils.services.utils

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer

object TestPostgresContainer {
    @JvmStatic
    val postgres: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("dataland_test")
            .withUsername("test")
            .withPassword("test")
            .withCommand("postgres -c max_connections=200")
            .also { it.start() }
    }

    @JvmStatic
    fun configureProperties(registry: DynamicPropertyRegistry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
        registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
        registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
        registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        registry.add("spring.jpa.show-sql") { "false" }
        registry.add("spring.jpa.properties.hibernate.format_sql") { "false" }
        registry.add("spring.flyway.enabled") { "false" }
    }
}
