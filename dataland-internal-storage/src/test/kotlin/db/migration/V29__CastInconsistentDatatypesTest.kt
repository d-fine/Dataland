package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V29__CastInconsistentDatatypesTest {
    @Test
    fun `check that inconsistent datatypes are cast correctly`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "extendedDecimalRateOfAccidents",
            "extendedDecimalRateOfAccidents",
            "V29/originalExtendedDecimalWithStringRepresentation.json",
            "V29/expectedExtendedDecimalWithStringRepresentation.json",
            V29__CastInconsistentDatatypes()::castDataPointValue,
        )
    }

    @Test
    fun `check that data points with empty strings are cast correctly`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "extendedDecimalScope1GhgEmissionsInTonnes",
            "extendedDecimalScope1GhgEmissionsInTonnes",
            "V29/originalDataPointWithEmptyStringValue.json",
            "V29/expectedDataPointWithEmptyStringValue.json",
            V29__CastInconsistentDatatypes()::castDataPointValue,
        )
    }

    @Test
    fun `check that data points with integer page number are cast correctly`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "extendedDecimalScope2GhgEmissionsInTonnes",
            "extendedDecimalScope2GhgEmissionsInTonnes",
            "V29/originalDataPointWithIntegerPageNumber.json",
            "V29/expectedDataPointWithIntegerPageNumber.json",
            V29__CastInconsistentDatatypes()::castDataPointValue,
        )
    }
}
