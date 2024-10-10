package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V15__MigrateGetRidOfFaultyDatasourcesTest {
    @Test
    fun `check migration script for faulty file references in lksg works properly`() {
        val instance = V15__MigrateGetRidOfFaultyDatasources()
        instance.validFileReferences.add("50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63")
        TestUtils().testMigrationOfSingleDataset(
            "lksg",
            "V15/originalLksgDatabaseEntry.json",
            "V15/expectedLksgDatabaseEntry.json",
            instance::migrateFaultyFileReferences,
        )
    }

    @Test
    fun `check migration script for faulty file references in sfdr works properly`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V15/originalSfdrDatabaseEntry.json",
            "V15/expectedSfdrDatabaseEntry.json",
            V15__MigrateGetRidOfFaultyDatasources()::migrateFaultyFileReferences,
        )
    }

    @Test
    fun `check migration script for faulty file references in sfdr with multiple CompanyReports works properly`() {
        val instance = V15__MigrateGetRidOfFaultyDatasources()
        instance.validFileReferences.add("50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63")
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V15/originalSfdrDatabaseEntryMultipleCompanyReports.json",
            "V15/expectedSfdrDatabaseEntryMultipleCompanyReports.json",
            instance::migrateFaultyFileReferences,
        )
    }

    @Test
    fun `check migration script for faulty file references in assurance data point`() {
        TestUtils().testMigrationOfSingleDataset(
            "eutaxonomy-non-financials",
            "V15/originalEuTaxonomyNonFinancialDatabaseEntry.json",
            "V15/expectedEuTaxonomyNonFinancialDatabaseEntry.json",
            V15__MigrateGetRidOfFaultyDatasources()::migrateFaultyFileReferences,
        )
    }
}
