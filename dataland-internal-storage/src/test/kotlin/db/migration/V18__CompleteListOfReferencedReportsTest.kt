package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

private const val V_18_EXPECTED_SFDR_JSON = "V18/expectedSfdr.json"
private const val EUTAXONOMY_NON_FINANCIALS = "eutaxonomy-non-financials"
private const val EUTAXONOMY_FINANCIALS = "eutaxonomy-financials"
private const val SFDR = "sfdr"

@Suppress("ClassName")
class V18__CompleteListOfReferencedReportsTest {
    @Test
    fun `check migration script for SFDR referencedReports empty`() {
        TestUtils().testMigrationOfSingleDataset(
            SFDR,
            "V18/originalSfdrCase1.json",
            V_18_EXPECTED_SFDR_JSON,
            { dataTableEntity ->
                V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, SFDR)
            },
        )
    }

    @Test
    fun `check migration script for SFDR referencedReports null`() {
        TestUtils().testMigrationOfSingleDataset(
            SFDR,
            "V18/originalSfdrCase2.json",
            V_18_EXPECTED_SFDR_JSON,
            { dataTableEntity ->
                V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, SFDR)
            },
        )
    }

    @Test
    fun `check migration script for SFDR referencedReports add fileName`() {
        TestUtils().testMigrationOfSingleDataset(
            SFDR,
            "V18/originalSfdrCase3.json",
            V_18_EXPECTED_SFDR_JSON,
            { dataTableEntity ->
                V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, SFDR)
            },
        )
    }

    @Test
    fun `check migration script for SFDR referencedReports do not replace existing fileName`() {
        TestUtils().testMigrationOfSingleDataset(
            SFDR,
            "V18/originalSfdrCase4.json",
            "V18/expectedSfdrCase4.json",
            { dataTableEntity ->
                V18__CompleteListOfReferencedReports().migrateReferencedReports(dataTableEntity, SFDR)
            },
        )
    }

    @Test
    fun `check migration script for Non Financial with no changes`() {
        TestUtils().testMigrationOfSingleDataset(
            EUTAXONOMY_NON_FINANCIALS,
            "V18/originalNonFinancialCase1.json",
            "V18/expectedNonFinancial.json",
            { dataTableEntity ->
                V18__CompleteListOfReferencedReports().migrateReferencedReports(
                    dataTableEntity,
                    EUTAXONOMY_NON_FINANCIALS,
                )
            },
        )
    }

    @Test
    fun `check migration script for Non Financial add missing information`() {
        TestUtils().testMigrationOfSingleDataset(
            EUTAXONOMY_NON_FINANCIALS,
            "V18/originalNonFinancialCase2.json",
            "V18/expectedNonFinancial.json",
            { dataTableEntity ->
                V18__CompleteListOfReferencedReports().migrateReferencedReports(
                    dataTableEntity,
                    EUTAXONOMY_NON_FINANCIALS,
                )
            },
        )
    }

    @Test
    fun `check migration script for Financial with no changes`() {
        TestUtils().testMigrationOfSingleDataset(
            EUTAXONOMY_FINANCIALS,
            "V18/originalFinancialCase1.json",
            "V18/expectedFinancial.json",
            { dataTableEntity ->
                V18__CompleteListOfReferencedReports().migrateReferencedReports(
                    dataTableEntity,
                    EUTAXONOMY_FINANCIALS,
                )
            },
        )
    }

    @Test
    fun `check migration script for Financial add missing information`() {
        TestUtils().testMigrationOfSingleDataset(
            EUTAXONOMY_FINANCIALS,
            "V18/originalFinancialCase2.json",
            "V18/expectedFinancial.json",
            { dataTableEntity ->
                V18__CompleteListOfReferencedReports().migrateReferencedReports(
                    dataTableEntity,
                    EUTAXONOMY_FINANCIALS,
                )
            },
        )
    }
}
