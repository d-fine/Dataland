package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V8__MigratePercentagesTest {
    @Test
    fun `test that the eu taxonomy for financials migration script works as expected`() {
        val dataType = "eutaxonomy-financials"
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V8/originalEuTaxonomyFinancialsData.json",
            "V8/transformedEuTaxonomyFinancialsData.json",
            V8__MigratePercentages()::migrateEuTaxonomyFinancials,
        )
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V8/originalEuTaxonomyFinancialsData2.json",
            "V8/transformedEuTaxonomyFinancialsData2.json",
            V8__MigratePercentages()::migrateEuTaxonomyFinancials,
        )
    }

    @Test
    fun `test that the eu taxonomy for non financials migration script works as expected`() {
        TestUtils().testMigrationOfSingleDataset(
            "eutaxonomy-non-financials",
            "V8/originalEuTaxonomyNonFinancialsData.json",
            "V8/transformedEuTaxonomyNonFinancialsData.json",
            V8__MigratePercentages()::migrateEuTaxonomyNonFinancials,
        )
    }

    @Test
    fun `test that the lksg migration script works as expected`() {
        val dataType = "lksg"
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V8/originalLksgData.json",
            "V8/transformedLksgData.json",
            V8__MigratePercentages()::migrateLksg,
        )
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V8/originalLksgData2.json",
            "V8/transformedLksgData2.json",
            V8__MigratePercentages()::migrateLksg,
        )
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V8/originalLksgData3.json",
            "V8/transformedLksgData3.json",
            V8__MigratePercentages()::migrateLksg,
        )
    }
}
