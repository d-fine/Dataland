package org.dataland.csvconverter

import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class DataPointParserTest {

    companion object {
        fun buildDataRow(
            value: String,
            quality: String,
            report: String,
            page: String,
            comment: String
        ): Map<String, String> {
            return mapOf(
                "recipe" to value,
                "recipe quality" to quality,
                "recipe report" to report,
                "recipe page" to page,
                "recipe comment" to comment
            )
        }
    }

    private val yesNoNaParser = EnumCsvParser(
        mapOf(
            "Yes" to YesNoNa.Yes,
            "No" to YesNoNa.No,
            "N/A" to YesNoNa.NA
        )
    )
    private val companyReportParser = CompanyReportParser(yesNoNaParser)
    private val dataPointParser = DataPointParser(companyReportParser)

    @Test
    fun `test that the data point parser works when supplied with valid data`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val validDataRow = buildDataRow(
            "111",
            "Reported",
            "Annual Report",
            "123",
            "it's great"
        )
        Assertions.assertEquals(
            dataPointParser.buildDecimalDataPoint(
                csvMapping, validDataRow, "rezept", BigDecimal.ONE
            ),
            DataPoint(
                value = 111.toBigDecimal(), quality = QualityOptions.Reported,
                CompanyReportReference(report = "AnnualReport", page = 123), comment = "it's great"
            )
        )
    }

    @Test
    fun `test that the data point parser returns null when no data is supplied`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val rowWithNoData = buildDataRow("", "", "", "", "")
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
        val rowWithValueOnly = buildDataRow("111", "", "", "", "")
        val rowWithPageOnly = buildDataRow("", "", "", "123", "")
        assertThrows<IllegalArgumentException> {
            dataPointParser.buildDecimalDataPoint(csvMapping, rowWithValueOnly, "rezept", BigDecimal.ONE)
        }
        assertThrows<IllegalArgumentException> {
            dataPointParser.buildDecimalDataPoint(csvMapping, rowWithPageOnly, "rezept", BigDecimal.ONE)
        }
    }
}
