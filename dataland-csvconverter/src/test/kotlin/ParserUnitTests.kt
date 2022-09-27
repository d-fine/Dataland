import org.dataland.csvconverter.csv.commonfieldparsers.AssuranceDataParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.NullPointerException

/**
 * Different functions which test the single Parser components
 */

class ParserUnitTests {

    private val yesNoNaParser = EnumCsvParser(
        mapOf(
            "Yes" to YesNoNa.Yes,
            "No" to YesNoNa.No,
            "N/A" to YesNoNa.NA
        )
    )

    private val row = mapOf(
        "assurance" to "reasonable",
        "assurance provider" to "Baker",
        "assurance report" to "AnnualReport",
        "assurance page" to "123",
        "recipe" to "111",
        "recipe quality" to "Reported",
        "recipe report" to "AnnualReport",
        "recipe page" to "222",
        "recipe comment" to "it's great"
    )

    /**
     * Tests the general EnumCsvParser
     * Arguments from the map are accepted, arguments not in the map lead to an error,
     * arguments which are null are only accepted in the parseAllowingNull function
     */
    @Test
    fun testEnumCsvParser() {
        val myParser = EnumCsvParser(
            mapOf(
                "1" to "one",
                "kekse" to "cookies"
            )
        )
        assertTrue(myParser.parse("Zahl", "1") == "one")
        assertTrue(myParser.parseAllowingNull("Kekse?", "Kekse") == "cookies")
        assertThrows<IllegalArgumentException> { myParser.parse("kekse", "keine Kekse") }
        assertThrows<IllegalArgumentException> { myParser.parse("Fehler", "") }
        assertThrows<IllegalArgumentException> { myParser.parseAllowingNull("Kekse?", "keine Kekse") }
        assertTrue(myParser.parseAllowingNull("", "") == null)
    }

    /**
     * The following functions test the other single parser components.
     * When the mandatory fields is empty, it should return null.
     * When an entry exists, but the mandatory field does not, there needs to be an error.
     */
    @Test
    fun testCompanyReferenceParser() {
        val generalMap = mapOf("rezept" to "recipe")
        val rowWithNoReport = mapOf("kekse" to "cookies")
        val rowWithPageOnly = mapOf("recipe page" to "123")
        val baseString = "rezept"
        assertEquals(
            DataPointParser(CompanyReportParser(yesNoNaParser)).buildSingleCompanyReportReference(
                generalMap, row, baseString
            ),
            CompanyReportReference(report = "Annual Report", page = 123.toBigDecimal())
        )
        assertEquals(
            DataPointParser(CompanyReportParser(yesNoNaParser)).buildSingleCompanyReportReference(
                generalMap, rowWithNoReport, baseString
            ),
            null
        )
        assertThrows<NullPointerException> {
            DataPointParser(CompanyReportParser(yesNoNaParser))
                .buildSingleCompanyReportReference(generalMap, rowWithPageOnly, baseString)
        }
    }

    @Test
    fun testDataPointParser() {
        val generalMap = mapOf("rezept" to "recipe")
        val rowWithNoQuality = mapOf("kekse" to "cookies")
        val rowWithValueOnly = mapOf("recipe" to "111")
        val rowWithPageOnly = mapOf("recipe Page" to "123")
        val baseString = "rezept"
        assertEquals(
            DataPointParser(CompanyReportParser(yesNoNaParser)).buildSingleDataPoint(
                generalMap, row, baseString
            ),
            DataPoint(
                value = 111.toBigDecimal(), quality = QualityOptions.Reported,
                CompanyReportReference(report = "Annual Report", page = 123.toBigDecimal()), comment = "it's great"
            )
        )
        assertEquals(
            DataPointParser(CompanyReportParser(yesNoNaParser)).buildSingleDataPoint(
                generalMap, rowWithNoQuality, baseString
            ),
            null
        )
        assertThrows<NullPointerException> {
            DataPointParser(CompanyReportParser(yesNoNaParser))
                .buildSingleDataPoint(generalMap, rowWithValueOnly, baseString)
        }
        assertThrows<NullPointerException> {
            DataPointParser(CompanyReportParser(yesNoNaParser))
                .buildSingleDataPoint(generalMap, rowWithPageOnly, baseString)
        }
    }

    @Test
    fun testAssuranceDataParser() {
        val rowWithNoAssurance = mapOf("kekse" to "cookies")
        val rowWithProviderOnly = mapOf("assurance provider" to "Baker")
        assertEquals(
            AssuranceDataParser(DataPointParser(CompanyReportParser(yesNoNaParser)))
                .buildSingleAssuranceData(row),
            AssuranceData(
                assurance = AssuranceOptions.ReasonableAssurance,
                provider = "Baker", CompanyReportReference(report = "Annual Report", page = 123.toBigDecimal())
            )
        )
        assertEquals(
            AssuranceDataParser(DataPointParser(CompanyReportParser(yesNoNaParser)))
                .buildSingleAssuranceData(rowWithNoAssurance),
            null
        )
        assertThrows<NullPointerException> {
            AssuranceDataParser(DataPointParser(CompanyReportParser(yesNoNaParser)))
                .buildSingleAssuranceData(rowWithProviderOnly)
        }
    }
}
