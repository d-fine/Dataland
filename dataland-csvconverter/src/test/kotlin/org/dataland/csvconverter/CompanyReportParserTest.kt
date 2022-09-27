package org.dataland.csvconverter

import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CompanyReportParserTest {
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
    fun `test that the company report reference parser works on valid data`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val validDataRow = mapOf(
            "recipe" to "111",
            "recipe quality" to "Reported",
            "recipe report" to "Annual Report",
            "recipe page" to "123",
            "recipe comment" to "it's great"
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
        val rowWithNoReport = mapOf(
            "recipe" to "",
            "recipe quality" to "",
            "recipe report" to "",
            "recipe page" to "",
            "recipe comment" to ""
        )
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
        val roWithNoReportButWithPage = mapOf(
            "recipe" to "",
            "recipe quality" to "",
            "recipe report" to "",
            "recipe page" to "123",
            "recipe comment" to ""
        )
        assertThrows<IllegalArgumentException> {
            dataPointParser.buildSingleCompanyReportReference(csvMapping, roWithNoReportButWithPage, "rezept")
        }
    }
}
