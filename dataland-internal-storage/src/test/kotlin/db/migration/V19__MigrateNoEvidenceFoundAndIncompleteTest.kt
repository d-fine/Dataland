package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V19__MigrateNoEvidenceFoundAndIncompleteTest {
    private val frameworkSfdr = "sfdr"

    @Test
    fun `check migration script for moving all incomplete quality buckets to no data found`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkSfdr,
            "V19/originalSfdrDatabaseEntryWithIncomplete.json",
            "V19/expectedSfdrDatabaseEntryWithIncomplete.json",
            V19__MigrateNoEvidenceFoundAndIncomplete()::migrateSfdrData,
        )
    }

    @Test
    fun `check migration script for moving all no evidence found values to the quality bucket`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkSfdr,
            "V19/originalSfdrDatabaseEntryWithNoEvidenceFoundValue.json",
            "V19/expectedSfdrDatabaseEntryWithNoEvidenceFoundValue.json",
            V19__MigrateNoEvidenceFoundAndIncomplete()::migrateSfdrData,
        )
    }

    @Test
    fun `check migration script also works in file with mixed replacements`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkSfdr,
            "V19/originalSfdrDatabaseEntry.json",
            "V19/expectedSfdrDatabaseEntry.json",
            V19__MigrateNoEvidenceFoundAndIncomplete()::migrateSfdrData,
        )
    }
}
