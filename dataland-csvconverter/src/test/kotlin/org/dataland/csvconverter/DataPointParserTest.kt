package org.dataland.csvconverter

import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.utils.YesNoNaParser
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class DataPointParserTest {

    companion object {
        var page = "recipe page"
        fun emptyDataRow(): MutableMap<String, String> {
            return mutableMapOf(
                "recipe" to "",
                "recipe quality" to "",
                "recipe report" to "",
                page to "",
                "recipe comment" to "",
                "recipe tag" to ""
            )
        }
        fun fullDataRow(): MutableMap<String, String> {
            return mutableMapOf(
                "recipe" to "111",
                "recipe quality" to "Reported",
                "recipe report" to "Annual Report",
                page to "123",
                "recipe comment" to "it's great",
                "recipe tag" to "here"
            )
        }
    }

    private val companyReportParser = CompanyReportParser(YesNoNaParser())
    private val dataPointParser = DataPointParser(companyReportParser)

    @Test
    fun `test that the data point parser works when supplied with valid data`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val validDataRow = fullDataRow()
        Assertions.assertEquals(
            dataPointParser.buildDecimalDataPoint(
                csvMapping, validDataRow, "rezept", BigDecimal.ONE
            ),
            DataPoint(
                value = 111.toBigDecimal(), quality = QualityOptions.Reported,
                CompanyReportReference(report = "AnnualReport", page = 123, tagName = "here"), comment = "it's great"
            )
        )
    }

    @Test
    fun `test that the data point parser returns null when no data is supplied`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val rowWithNoData = emptyDataRow()
        Assertions.assertEquals(
            dataPointParser.buildDecimalDataPoint(
                csvMapping, rowWithNoData, "rezept", BigDecimal.ONE
            ),
            null
        )
    }

    @Test
    fun `test that the data point parser returns null when mandatory values are left out`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val rowWithValueOnly = emptyDataRow()
        rowWithValueOnly["recipe"] = "111"
        val rowWithPageOnly = emptyDataRow()
        rowWithPageOnly[page] = "123"
        assertThrows<IllegalArgumentException> {
            dataPointParser.buildDecimalDataPoint(csvMapping, rowWithValueOnly, "rezept", BigDecimal.ONE)
        }
        assertThrows<IllegalArgumentException> {
            dataPointParser.buildDecimalDataPoint(csvMapping, rowWithPageOnly, "rezept", BigDecimal.ONE)
        }
    }
}
