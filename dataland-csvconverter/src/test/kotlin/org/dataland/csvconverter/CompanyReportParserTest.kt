package org.dataland.csvconverter

import org.dataland.csvconverter.DataPointParserTest.Companion.emptyDataRow
import org.dataland.csvconverter.DataPointParserTest.Companion.fullDataRow
import org.dataland.csvconverter.DataPointParserTest.Companion.page
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
        val validDataRow = fullDataRow()
        Assertions.assertEquals(
            dataPointParser.buildSingleCompanyReportReference(
                csvMapping, validDataRow, "rezept"
            ),
            CompanyReportReference(report = "AnnualReport", page = 123, tagName = "here")
        )
    }

    @Test
    fun `test that the company report parser returns null if no fields have been specified`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val rowWithNoReport = emptyDataRow()
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
        val rowWithNoReportButWithPage = emptyDataRow()
        rowWithNoReportButWithPage[page] = "123"
        assertThrows<IllegalArgumentException> {
            dataPointParser.buildSingleCompanyReportReference(csvMapping, rowWithNoReportButWithPage, "rezept")
        }
    }
}
