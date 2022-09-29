package org.dataland.csvconverter

import org.dataland.csvconverter.DataPointParserTest.Companion.buildDataRow
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.utils.YesNoNaParser
import org.dataland.datalandbackend.model.CompanyReportReference
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CompanyReportParserTest {

    private val companyReportParser = CompanyReportParser(YesNoNaParser())
    private val dataPointParser = DataPointParser(companyReportParser)

    @Test
    fun `test that the company report reference parser works on valid data`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val validDataRow = buildDataRow(
            "111",
            "Reported",
            "Annual Report",
            "123",
            "it's great"
        )
        Assertions.assertEquals(
            dataPointParser.buildSingleCompanyReportReference(
                csvMapping, validDataRow, "rezept"
            ),
            CompanyReportReference(report = "AnnualReport", page = 123)
        )
    }

    @Test
    fun `test that the company report parser returns null if no fields have been specified`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val rowWithNoReport = buildDataRow("", "", "", "", "")
        Assertions.assertEquals(
            dataPointParser.buildSingleCompanyReportReference(
                csvMapping, rowWithNoReport, "rezept"
            ),
            null
        )
    }

    @Test
    fun `test that the company report parser throws an error when only partial data is supplied`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val roWithNoReportButWithPage = buildDataRow("", "", "", "123", "")
        assertThrows<IllegalArgumentException> {
            dataPointParser.buildSingleCompanyReportReference(csvMapping, roWithNoReportButWithPage, "rezept")
        }
    }
}
