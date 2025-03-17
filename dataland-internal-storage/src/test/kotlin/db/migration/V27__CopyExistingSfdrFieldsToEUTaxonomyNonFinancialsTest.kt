package db.migration

import db.migration.utils.TestUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V27__CopyExistingSfdrFieldsToEUTaxonomyNonFinancialsTest {
    private val migrationObject = V27__CopyExistingSfdrFieldsToEUTaxonomyNonFinancials()
    private lateinit var availableSfdrDataPointsMapping: Map<Pair<String, String>, Map<String, Any?>>

    private val sfdrTestDataSets =
        listOf(
            "V27/sfdrDataSetCase1.json",
            "V27/sfdrDataSetCase2.json",
            "V27/sfdrDataSetCase3.json",
        )

    private val case1CompanyId = "27e313a2-c847-4869-90b3-52c7516ef93a"
    private val case3CompanyId = case1CompanyId

    private fun assertMigrationOfDataSet(
        originalFilePath: String,
        expectedFilePath: String,
    ) {
        val dataSets = TestUtils().readDatasetsAsStoredInDatabase(listOf(originalFilePath, expectedFilePath))
        V27__CopyExistingSfdrFieldsToEUTaxonomyNonFinancials().augmentTaxonomyNonFinancialsWithSfdrData(
            dataSets[0], availableSfdrDataPointsMapping,
        )

        assertEquals(dataSets[1], dataSets[0])
    }

    @BeforeEach
    fun parseSfdrData() {
        availableSfdrDataPointsMapping =
            migrationObject.extractSfdrFieldsAsMapping(
                TestUtils().readDatasetsAsStoredInDatabase(sfdrTestDataSets),
                setOf(
                    "social.socialAndEmployeeMatters.iloCoreLabourStandards",
                    "social.humanRights.humanRightsDueDiligence",
                ),
            )
    }

    @Test
    fun `check extraction of SFDR fields`() {
        with(availableSfdrDataPointsMapping) {
            assertEquals(this.size, 3)
            val iloField = this[Pair(case1CompanyId, "2021")]?.get("iloCoreLabourStandards") as JSONObject
            assertNotNull(iloField)
            assertEquals(iloField.getJSONObject("dataSource").getString("page"), "57")
            assertNull(this[Pair(case1CompanyId, "2021")]?.get("humanRightsDueDiligence"))
            assertNull(this[Pair(case3CompanyId, "2023")]?.get("humanRightsDueDiligence"))
        }
    }

    @Test
    fun `check that taxonomy data remains unchanged if no matching SFDR data set is found`() {
        assertMigrationOfDataSet(
            "V27/taxonomyNonFinancialsCase1Original.json",
            "V27/taxonomyNonFinancialsCase1Expected.json",
        )
    }

    @Test
    fun `check migration if SFDR data is available`() {
        assertMigrationOfDataSet(
            "V27/taxonomyNonFinancialsCase2Original.json",
            "V27/taxonomyNonFinancialsCase2Expected.json",
        )
    }

    @Test
    fun `check that null is inserted in taxonomy data even if SFDR field is missing completely`() {
        assertMigrationOfDataSet(
            "V27/taxonomyNonFinancialsCase3Original.json",
            "V27/taxonomyNonFinancialsCase3Expected.json",
        )
    }
}
