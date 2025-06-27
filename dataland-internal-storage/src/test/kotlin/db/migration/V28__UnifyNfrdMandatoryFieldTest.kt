package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V28__UnifyNfrdMandatoryFieldTest {
    @Test
    fun `check renaming of extendedEnumYesNoNfrdMandatory`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "extendedEnumYesNoNfrdMandatory",
            "extendedEnumYesNoIsNfrdMandatory",
            "V28/dummy_data_point.json",
            "V28/dummy_data_point.json",
            V28__UnifyNfrdMandatoryField()
                ::updateNfrdMandatory,
        )
    }
}
