package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.TestUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V29__MigratePlainDatesToExtendedDatesTest {
    @Test
    fun `check conversion of plainDateFiscalYearEnd to extendedDateFiscalYearEnd`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "plainDateFiscalYearEnd",
            "extendedDateFiscalYearEnd",
            "V29/dummy_data_point.json",
            "V29/dummy_data_point.json",
            { dataPoint ->
                V29__MigratePlainDatesToExtendedDates().convertPlainToExtended(
                    dataPoint,
                    "extendedDateFiscalYearEnd",
                )
            },
        )
    }

    @Test
    fun `check conversion of plainEnumFiscalYearDeviation to extendedEnumFiscalYearDeviation`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "plainEnumFiscalYearDeviation",
            "extendedEnumFiscalYearDeviation",
            "V29/dummy_data_point.json",
            "V29/dummy_data_point.json",
            { dataPoint ->
                V29__MigratePlainDatesToExtendedDates().convertPlainToExtended(
                    dataPoint,
                    "extendedEnumFiscalYearDeviation",
                )
            },
        )
    }

    @Test
    fun `tuple matching identifies conflicts correctly`() {
        val conflicts =
            setOf(
                V29__MigratePlainDatesToExtendedDates.DataPointTuple(
                    companyId = "company-1",
                    reportingPeriod = "2024",
                    framework = "sfdr",
                ),
            )

        val matchingTuple =
            V29__MigratePlainDatesToExtendedDates.DataPointTuple(
                companyId = "company-1",
                reportingPeriod = "2024",
                framework = "sfdr",
            )

        val nonMatchingTuple =
            V29__MigratePlainDatesToExtendedDates.DataPointTuple(
                companyId = "company-2",
                reportingPeriod = "2024",
                framework = "sfdr",
            )

        assertTrue(matchingTuple in conflicts, "Matching tuple should be found in conflicts")
        assertFalse(nonMatchingTuple in conflicts, "Non-matching tuple should not be found in conflicts")
    }

    @Test
    fun `DataPointTuple equality works correctly`() {
        val tuple1 =
            V29__MigratePlainDatesToExtendedDates.DataPointTuple(
                companyId = "company-1",
                reportingPeriod = "2024",
                framework = "sfdr",
            )

        val tuple2 =
            V29__MigratePlainDatesToExtendedDates.DataPointTuple(
                companyId = "company-1",
                reportingPeriod = "2024",
                framework = "sfdr",
            )

        val tuple3 =
            V29__MigratePlainDatesToExtendedDates.DataPointTuple(
                companyId = "company-1",
                reportingPeriod = "2024",
                framework = "pcaf",
            )

        assertEquals(tuple1, tuple2, "Tuples with same values should be equal")
        assertNotEquals(tuple1, tuple3, "Tuples with different frameworks should not be equal")

        val set = setOf(tuple1)
        assertTrue(tuple2 in set, "Equal tuple should be found in set")
        assertFalse(tuple3 in set, "Different tuple should not be found in set")
    }

    @Test
    fun `convertPlainToExtended modifies dataPointType`() {
        val dataPoint =
            DataPointTableEntity(
                dataPointId = "test-123",
                companyId = "company-1",
                dataPoint = JSONObject("""{"value": "2024-12-31", "quality": "Reported"}"""),
                dataPointType = "plainDateFiscalYearEnd",
                reportingPeriod = "2024",
                framework = "sfdr",
                currentlyActive = true,
            )

        V29__MigratePlainDatesToExtendedDates().convertPlainToExtended(
            dataPoint,
            "extendedDateFiscalYearEnd",
        )

        assertEquals("extendedDateFiscalYearEnd", dataPoint.dataPointType)
    }
}
