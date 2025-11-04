package org.dataland.datalandbackendutils.services.utils

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import javax.sql.DataSource

private class TestContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    val postgres: PostgreSQLContainer<*> =
        PostgreSQLContainer("postgres:15").apply {
            withDatabaseName("dataland_test")
            withUsername("test")
            withPassword("test")
            withReuse(true)
            start()
        }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        TestPropertyValues
            .of(
                "spring.datasource.url=${postgres.jdbcUrl}",
                "spring.datasource.username=${postgres.username}",
                "spring.datasource.password=${postgres.password}",
                "spring.datasource.driver-class-name=org.postgresql.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false",
                "spring.jpa.properties.hibernate.format_sql=false",
                "spring.flyway.enabled=false",
                "spring.flyway.url=${postgres.jdbcUrl}",
                "spring.flyway.user=${postgres.username}",
                "spring.flyway.password=${postgres.password}",
            ).applyTo(applicationContext.environment)
    }
}

/**
 * Base class for flyway migration tests (service + associated DB) with standardized configuration.
 *
 * - Uses "test" profile (e.g. disables AssembledDataMigrationTrigger and RabbitMQ)
 * - Provides real PostgreSQL DB via container
 * - Uses one container per test class
 */
@SpringBootTest
@ContextConfiguration(initializers = [TestContainerInitializer::class])
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
abstract class BaseFlywayMigrationTest {
    @Autowired
    lateinit var applicationContext: ApplicationContext

    @BeforeAll
    fun migrate() {
        setupBeforeMigration()
        val flyway =
            Flyway
                .configure()
                .baselineOnMigrate(true)
                .baselineVersion(getFlywayBaselineVersion())
                .target(getFlywayTargetVersion())
                .dataSource(applicationContext.getBean(DataSource::class.java))
                .load()
        flyway.migrate()
    }

    /**
     * Set up test data before migration is executed
     */
    abstract fun setupBeforeMigration()

    /**
     * Get the baseline version for the Flyway migration
     */
    abstract fun getFlywayBaselineVersion(): String

    /**
     * Get the target version for the Flyway migration
     */
    abstract fun getFlywayTargetVersion(): String
}
