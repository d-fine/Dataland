package org.dataland.csvconverter

import org.dataland.csvconverter.csv.commonfieldparsers.AssuranceDataParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Different functions which test the single Parser components
 */

class AssuranceDataParserTest {

    private val yesNoNaParser = EnumCsvParser(
        mapOf(
            "Yes" to YesNoNa.Yes,
            "No" to YesNoNa.No,
            "N/A" to YesNoNa.NA
        )
    )
    private val assuranceParser = AssuranceDataParser(DataPointParser(CompanyReportParser(yesNoNaParser)))

    @Test
    fun `test AssuranceDataParser`() {
        val row = mapOf(
            "assurance" to "reasonable",
            "assurance provider" to "Baker",
            "assurance report" to "Annual Report",
            "assurance page" to "123"
        )
        assertEquals(
            AssuranceData(
                assurance = AssuranceOptions.ReasonableAssurance,
                provider = "Baker", CompanyReportReference(report = "AnnualReport", page = 123)
            ),
            assuranceParser
                .buildSingleAssuranceData(row),
        )
    }

    @Test
    fun `test AssuranceDataParser with empty row`() {
        val rowWithNoAssurance = mapOf(
            "assurance" to "",
            "assurance provider" to "",
            "assurance report" to "",
            "assurance page" to "",
        )
        assertEquals(
            null,
            assuranceParser.buildSingleAssuranceData(rowWithNoAssurance),
        )
    }

    @Test
    fun `test AssuranceDataParser with missing arguments`() {
        val rowWithProviderOnly = mapOf(
            "assurance" to "",
            "assurance provider" to "Baker",
            "assurance report" to "",
            "assurance page" to ""
        )

        assertThrows<IllegalArgumentException> {
            assuranceParser.buildSingleAssuranceData(rowWithProviderOnly)
        }
    }
}
