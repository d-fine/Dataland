package db.migration

import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import javax.sql.DataSource

@SpringBootTest(classes = [DatalandDataSourcingService::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
class V2__AddNonSourceableVerificationToConstraintTest : BaseFlywayMigrationTest() {
    @Autowired
    lateinit var dataSource: DataSource

    override fun getFlywayBaselineVersion(): String = "1"

    override fun getFlywayTargetVersion(): String = "2"

    override fun setupBeforeMigration() = Unit

    @Test
    fun `NonSourceableVerification state is accepted in data_sourcing after migration`() {
        dataSource.connection.use { conn ->
            assertDoesNotThrow {
                conn.createStatement().execute(
                    """
                    INSERT INTO data_sourcing (data_sourcing_id, company_id, reporting_period, data_type, state, priority)
                    VALUES ('${UUID.randomUUID()}', '${UUID.randomUUID()}', '2024', 'sfdr', 'NonSourceableVerification', 10)
                    """.trimIndent(),
                )
            }
        }
    }
}

@SpringBootTest(classes = [DatalandDataSourcingService::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
class V3__AddNonSourceableVerificationToConstraintInAudTableTest : BaseFlywayMigrationTest() {
    @Autowired
    lateinit var dataSource: DataSource

    override fun getFlywayBaselineVersion(): String = "2"

    override fun getFlywayTargetVersion(): String = "3"

    override fun setupBeforeMigration() {
        dataSource.connection.use { conn ->
            conn.createStatement().execute(
                "INSERT INTO revinfo (rev, revtstmp) VALUES (1, ${System.currentTimeMillis()}) ON CONFLICT DO NOTHING",
            )
        }
    }

    @Test
    fun `NonSourceableVerification state is accepted in data_sourcing_aud after migration`() {
        dataSource.connection.use { conn ->
            assertDoesNotThrow {
                conn.createStatement().execute(
                    """
                    INSERT INTO data_sourcing_aud (data_sourcing_id, rev, state)
                    VALUES ('${UUID.randomUUID()}', 1, 'NonSourceableVerification')
                    """.trimIndent(),
                )
            }
        }
    }
}
