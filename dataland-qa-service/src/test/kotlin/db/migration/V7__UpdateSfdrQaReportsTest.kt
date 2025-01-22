package db.migration

import org.dataland.datalandqaservice.db.migration.V7__UpdateSfdrQaReports
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

@Suppress("ClassName")
class V7__UpdateSfdrQaReportsTest {
    private fun testMigrationOfSingleQaReport(
        locationOfOriginalReport: String,
        locationOfExpectedReport: String,
        migratingFunction: (JSONObject) -> JSONObject,
    ) {
        val originalQaReport = JSONObject(javaClass.getResource("/db/migration/$locationOfOriginalReport")!!.readText())
        val expectedQaReport = JSONObject(javaClass.getResource("/db/migration/$locationOfExpectedReport")!!.readText())

        val migratedQaReport = migratingFunction(originalQaReport)
        JSONAssert.assertEquals(expectedQaReport, migratedQaReport, true)
    }

    @Test
    fun `check migration for SFDR one`() {
        testMigrationOfSingleQaReport(
            "V7/originalSfdrOne.json",
            "V7/expectedSfdrOne.json",
            V7__UpdateSfdrQaReports()::migrateQaReport,
        )
    }

    @Test
    fun `check migration for SFDR two`() {
        testMigrationOfSingleQaReport(
            "V7/originalSfdrTwo.json",
            "V7/expectedSfdrTwo.json",
            V7__UpdateSfdrQaReports()::migrateQaReport,
        )
    }

    @Test
    fun `check migration for SFDR three`() {
        testMigrationOfSingleQaReport(
            "V7/originalSfdrThree.json",
            "V7/expectedSfdrThree.json",
            V7__UpdateSfdrQaReports()::migrateQaReport,
        )
    }
}
