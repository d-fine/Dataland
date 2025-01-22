package db.migration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.dataland.datalandqaservice.db.migration.V7__UpdateSfdrQaReports
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

@Suppress("ClassName")
class V7__UpdateSfdrQaReportsTest {
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules().registerKotlinModule()

    private fun testMigrationOfSingleQaReport(
        locationOfOriginalReport: String,
        locationOfExpectedReport: String,
        migratingFunction: () -> Unit,
    ) {
        val originalQaReport = objectMapper.readValue(File(locationOfOriginalReport), JSONObject::class.java)
        val expectedQaReport = objectMapper.readValue(File(locationOfExpectedReport), JSONObject::class.java)

        val migratedQaReport = migratingFunction(originalQaReport)
        Assertions.assertEquals(expectedQaReport, migratedQaReport)
    }

    @Test
    fun `check migration for SFDR one`() {
        testMigrationOfSingleQaReport(
            "./src/test/resources/originalSfdrOne.json",
            "./src/test/resources/expectedSfdrOne.json",
            V7__UpdateSfdrQaReports()::migrate,
        )
    }

    @Test
    fun `check migration for SFDR two`() {
        testMigrationOfSingleQaReport(
            "./src/test/resources/originalSfdrTwo.json",
            "./src/test/resources/expectedSfdrTwo.json",
            V7__UpdateSfdrQaReports()::migrate,
        )
    }

    @Test
    fun `check migration for SFDR three`() {
        testMigrationOfSingleQaReport(
            "./src/test/resources/originalSfdrThree.json",
            "./src/test/resources/expectedSfdrThree.json",
            V7__UpdateSfdrQaReports()::migrate,
        )
    }
}
