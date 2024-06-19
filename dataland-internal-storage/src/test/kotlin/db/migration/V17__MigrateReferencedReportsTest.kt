package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V17__MigrateReferencedReportsTest {

    @Test
    fun `check migration script for SFDR `() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V17/originalSfdr.json",
            "V16/expectedSfdr.json",
            { dataTableEntity -> V17__MigrateReferencedReports().migrateReferencedReports(dataTableEntity, "sfdr") }
        )
    }
}