package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V19__MigrateNoEvidenceFoundAndIncompleteTest {
    private val frameworkSfdr = "sfdr"

    @Test
    fun `validate that all no evidence found values are set to null and their quality bucket to No Data Found`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkSfdr,
            "V19/originalSfdrDatabaseEntryWithNoEvidenceFoundValue.json",
            "V19/expectedSfdrDatabaseEntryWithNoEvidenceFoundValue.json",
            V19__MigrateNoEvidenceFoundAndIncomplete()::migrateSfdrData,
        )
    }
}
