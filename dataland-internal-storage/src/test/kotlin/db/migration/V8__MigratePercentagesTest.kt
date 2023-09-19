package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V8__MigratePercentagesTest {

    @Test
    fun `test that the eu taxonomy for financials migration script works as expected`() {
        TestUtils().testMigrationOfSingleDataset(
            "V8/originalEuTaxonomyFinancialsData.json",
            "V8/transformedEuTaxonomyFinancialsData.json",
            V8__MigratePercentages()::migrateEuTaxonomyFinancials,
        )
    }

    @Test
    fun `test that the eu taxonomy for non financials migration script works as expected`() {
        TestUtils().testMigrationOfSingleDataset(
            "V8/originalEuTaxonomyNonFinancialsData.json",
            "V8/transformedEuTaxonomyNonFinancialsData.json",
            V8__MigratePercentages()::migrateEuTaxonomyNonFinancials,
        )
    }

    @Test
    fun `test that the lksg migration script works as expected`() {
        TestUtils().testMigrationOfSingleDataset(
            "V8/originalLksgData.json",
            "V8/transformedLksgData.json",
            V8__MigratePercentages()::migrateLksg,
        )
    }

    @Test
    fun `test that the sfdr migration script works as expected`() {
        TestUtils().testMigrationOfSingleDataset(
            "V8/originalSfdrData.json",
            "V8/transformedSfdrData.json",
            V8__MigratePercentages()::migrateSfdr,
        )
    }

    @Test
    fun `test that the sme migration script works as expected`() {
        TestUtils().testMigrationOfSingleDataset(
            "V8/originalSmeData.json",
            "V8/transformedSmeData.json",
            V8__MigratePercentages()::migrateSme,
        )
    }

    @Test
    fun `test that the pathways to paris migration script works as expected`() {
        TestUtils().testMigrationOfSingleDataset(
            "V8/originalP2pData.json",
            "V8/transformedP2pData.json",
            V8__MigratePercentages()::migrateP2p,
        )
    }
}
