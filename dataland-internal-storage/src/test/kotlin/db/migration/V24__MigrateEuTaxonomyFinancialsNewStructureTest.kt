package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V24__MigrateEuTaxonomyFinancialsNewStructureTest {
    private val framework = "eutaxonomy-financials"

    @Test
    fun `test with companyForAllTypes data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = framework,
            oldDataFileLocation = "V24/EuTaxonomyFinancialsOriginal.json",
            migratedDataFileLocation = "V24/EuTaxonomyFinancialsExpected.json",
            migration = V24__MigrateEuTaxonomyFinancialsNewStructure()::migrateEuTaxonomyFinancialsData,
        )
    }

    @Test
    fun `test with Asset and Investment 2021 data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = framework,
            oldDataFileLocation = "V24/originalFinancialAssetInvestment2021.json",
            migratedDataFileLocation = "V24/expectedFinancialAssetInvestment2021.json",
            migration = V24__MigrateEuTaxonomyFinancialsNewStructure()::migrateEuTaxonomyFinancialsData,
        )
    }

    @Test
    fun `test with Asset and Investment 2022 data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = framework,
            oldDataFileLocation = "V24/originalFinancialAssetInvestment2022.json",
            migratedDataFileLocation = "V24/expectedFinancialAssetInvestment2022.json",
            migration = V24__MigrateEuTaxonomyFinancialsNewStructure()::migrateEuTaxonomyFinancialsData,
        )
    }

    @Test
    fun `test with Credit 2021 data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = framework,
            oldDataFileLocation = "V24/originalFinancialCredit2021.json",
            migratedDataFileLocation = "V24/expectedFinancialCredit2021.json",
            migration = V24__MigrateEuTaxonomyFinancialsNewStructure()::migrateEuTaxonomyFinancialsData,
        )
    }

    @Test
    fun `test with Credit 2022 data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = framework,
            oldDataFileLocation = "V24/originalFinancialCredit2022.json",
            migratedDataFileLocation = "V24/expectedFinancialCredit2022.json",
            migration = V24__MigrateEuTaxonomyFinancialsNewStructure()::migrateEuTaxonomyFinancialsData,
        )
    }

    @Test
    fun `test with Insurance 2021 data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = framework,
            oldDataFileLocation = "V24/originalFinancialInsurance2021.json",
            migratedDataFileLocation = "V24/expectedFinancialInsurance2021.json",
            migration = V24__MigrateEuTaxonomyFinancialsNewStructure()::migrateEuTaxonomyFinancialsData,
        )
    }

    @Test
    fun `test with Insurance 2022 data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = framework,
            oldDataFileLocation = "V24/originalFinancialInsurance2022.json",
            migratedDataFileLocation = "V24/expectedFinancialInsurance2022.json",
            migration = V24__MigrateEuTaxonomyFinancialsNewStructure()::migrateEuTaxonomyFinancialsData,
        )
    }
}
