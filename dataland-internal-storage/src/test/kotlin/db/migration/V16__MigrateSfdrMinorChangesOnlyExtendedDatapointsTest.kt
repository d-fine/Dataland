package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V16__MigrateSfdrMinorChangesOnlyExtendedDatapointsTest {
    private val frameworkSfdr = "sfdr"

    @Test
    fun `check migration script for moving all expected elements from waste to biodiversity`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkSfdr,
            "V16/originalSfdrDatabaseEntry.json",
            "V16/expectedSfdrDatabaseEntry.json",
            V16__MigrateSfdrMinorChangesOnlyExtendedDatapoints()::migrateSfdrData,
        )
    }

    @Test
    fun `check migration script when wastes is not empty at the end`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkSfdr,
            "V16/originalSfdrDatabaseEntryNoEmptyWaste.json",
            "V16/expectedSfdrDatabaseEntryNoEmptyWaste.json",
            V16__MigrateSfdrMinorChangesOnlyExtendedDatapoints()::migrateSfdrData,
        )
    }

    @Test
    fun `check migration script when wastes is present`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkSfdr,
            "V16/originalSfdrDatabaseEntryNoWaste.json",
            "V16/expectedSfdrDatabaseEntryNoWaste.json",
            V16__MigrateSfdrMinorChangesOnlyExtendedDatapoints()::migrateSfdrData,
        )
    }

    @Test
    fun `check migration of eu taxonomy financials`() {
        TestUtils().testMigrationOfSingleDataset(
            "eutaxonomy-financials",
            "V16/originalEuTaxonomyFinancials.json",
            "V16/expectedEuTaxonomyFinancials.json",
            V16__MigrateSfdrMinorChangesOnlyExtendedDatapoints()::migrateDataPoints,
        )
    }
}
