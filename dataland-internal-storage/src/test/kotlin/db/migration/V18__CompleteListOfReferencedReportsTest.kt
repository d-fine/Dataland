package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V18__CompleteListOfReferencedReportsTest {

    @Test
    fun `check migration script for SFDR `() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V18/originalSfdr.json",
            "V18/expectedSfdr.json",
            { dataTableEntity -> V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, "sfdr") },
        )
    }

}