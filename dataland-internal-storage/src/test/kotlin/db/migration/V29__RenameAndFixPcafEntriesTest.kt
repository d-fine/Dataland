package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V29__RenameAndFixPcafEntriesTest {
    companion object {
        const val ORIGINAL_JSON = "V29/original.json"
        const val EXPECTED_JSON = "V29/expected.json"
    }

    @Test
    fun `check migration of customEnumPcafMainSector`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "customEnumPcafMainSector",
            "extendedEnumPcafMainSector",
            ORIGINAL_JSON,
            EXPECTED_JSON,
            V29__RenameAndFixPcafEntries()::updateDataTableEntity,
        )
    }

    @Test
    fun `check migration of customEnumCompanyExchangeStatus`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "customEnumCompanyExchangeStatus",
            "extendedEnumCompanyExchangeStatus",
            ORIGINAL_JSON,
            EXPECTED_JSON,
            V29__RenameAndFixPcafEntries()::updateDataTableEntity,
        )
    }

    @Test
    fun `check no renaming of other data point types`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "dummy",
            "dummy",
            ORIGINAL_JSON,
            EXPECTED_JSON,
            V29__RenameAndFixPcafEntries()::updateDataTableEntity,
        )
    }
}
