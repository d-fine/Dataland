package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V29__RenameAndFixPcafEntriesTest {
    @Test
    fun `check migration of customEnumPcafMainSector`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "customEnumPcafMainSector",
            "extendedEnumPcafMainSector",
            "V29/original.json",
            "V29/expected.json",
            V29__RenameAndFixPcafEntries()::updateDataTableEntity,
        )
    }

    @Test
    fun `check migration of customEnumCompanyExchangeStatus`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "customEnumCompanyExchangeStatus",
            "extendedEnumCompanyExchangeStatus",
            "V29/original.json",
            "V29/expected.json",
            V29__RenameAndFixPcafEntries()::updateDataTableEntity,
        )
    }

    @Test
    fun `check no renaming of other data point types`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "dummy",
            "dummy",
            "V29/original.json",
            "V29/expected.json",
            V29__RenameAndFixPcafEntries()::updateDataTableEntity,
        )
    }
}
