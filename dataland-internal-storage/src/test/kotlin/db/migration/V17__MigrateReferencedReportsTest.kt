package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V17__MigrateReferencedReportsTest {
    @Test
    fun `check migration script for SFDR `() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V17/originalSfdr.json",
            "V17/expectedSfdr.json",
            { dataTableEntity -> V17__MigrateReferencedReports().migrateReferencedReports(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for Non Financial `() {
        TestUtils().testMigrationOfSingleDataset(
            "eutaxonomy-non-financials",
            "V17/originalNonFinancial.json",
            "V17/expectedNonFinancial.json",
            { dataTableEntity ->
                V17__MigrateReferencedReports().migrateReferencedReports(
                    dataTableEntity,
                    "eutaxonomy-non-financials",
                )
            },
        )
    }

    @Test
    fun `check migration script for Financial `() {
        TestUtils().testMigrationOfSingleDataset(
            "eutaxonomy-financials",
            "V17/originalFinancial.json",
            "V17/expectedFinancial.json",
            { dataTableEntity ->
                V17__MigrateReferencedReports().migrateReferencedReports(
                    dataTableEntity,
                    "eutaxonomy-financials",
                )
            },
        )
    }
}
