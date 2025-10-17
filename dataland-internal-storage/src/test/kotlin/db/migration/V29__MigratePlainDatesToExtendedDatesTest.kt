package db.migration

import db.migration.utils.TestUtils
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
        ) { dataPoint ->
            dataPoint.dataPointType = "extendedDateFiscalYearEnd"
        }
    }

    @Test
    fun `check conversion of plainEnumFiscalYearDeviation to extendedEnumFiscalYearDeviation`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "plainEnumFiscalYearDeviation",
            "extendedEnumFiscalYearDeviation",
            "V29/dummy_data_point.json",
            "V29/dummy_data_point.json",
        ) { dataPoint ->
            dataPoint.dataPointType = "extendedEnumFiscalYearDeviation"
        }
    }
}
