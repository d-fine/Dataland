package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V18__CompleteListOfReferencedReportsTest {

    @Test
    fun `check migration script for SFDR referencedReports empty`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V18/originalSfdrCase1.json",
            "V18/expectedSfdr.json",
            { dataTableEntity -> V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for SFDR referencedReports null`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V18/originalSfdrCase2.json",
            "V18/expectedSfdr.json",
            { dataTableEntity -> V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for SFDR referencedReports add fileName`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V18/originalSfdrCase3.json",
            "V18/expectedSfdr.json",
            { dataTableEntity -> V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for SFDR referencedReports do not replace existing fileName`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V18/originalSfdrCase4.json",
            "V18/expectedSfdrCase4.json",
            { dataTableEntity -> V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for Non Financial with no changes`() {
        TestUtils().testMigrationOfSingleDataset(
            "eutaxonomy-non-financials",
            "V18/originalNonFinancialCase1.json",
            "V18/expectedNonFinancial.json",
            { dataTableEntity -> V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, "eutaxonomy-non-financials") },
        )
    }

    @Test
    fun `check migration script for Non Financial add missing information`() {
        TestUtils().testMigrationOfSingleDataset(
            "eutaxonomy-non-financials",
            "V18/originalNonFinancialCase2.json",
            "V18/expectedNonFinancial.json",
            { dataTableEntity -> V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, "eutaxonomy-non-financials") },
        )
    }

    @Test
    fun `check migration script for Financial with no changes`() {
        TestUtils().testMigrationOfSingleDataset(
            "eutaxonomy-non-financials",
            "V18/originalFinancialCase1.json",
            "V18/expectedFinancial.json",
            { dataTableEntity -> V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, "eutaxonomy-financials") },
        )
    }

    @Test
    fun `check migration script for Financial add missing information`() {
        TestUtils().testMigrationOfSingleDataset(
            "eutaxonomy-non-financials",
            "V18/originalFinancialCase2.json",
            "V18/expectedFinancial.json",
            { dataTableEntity -> V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, "eutaxonomy-financials") },
        )
    }
}
