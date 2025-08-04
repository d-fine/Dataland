package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.configuration.TestRabbitConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for integration tests (service + associated DB) with standardized configuration.
 *
 * - Uses "test" profile (e.g. disables AssembledDataMigrationTrigger and RabbitMQ)
 * - Provides real PostgreSQL DB via container
 * - Enables transactions with rollback, i.e. all data is automatically reset in between tests
 */
@SpringBootTest(classes = [DatalandBackend::class, TestRabbitConfiguration::class])
@ActiveProfiles("test")
@Testcontainers
@Transactional
@Rollback
// Suppress is required as detekt wrongfully suggests to convert this class into an object which would break it
@Suppress("UtilityClassWithPublicConstructor")
abstract class BaseIntegrationTest {
    companion object {
        @Container
        @JvmStatic
        val postgres = TestPostgresContainer.postgres

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            TestPostgresContainer.configureProperties(registry)
        }
    }
}
