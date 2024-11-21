package org.dataland.datalanddataexporter.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.SfdrGeneral
import org.dataland.datalandbackend.openApiClient.model.SfdrGeneralGeneral
import org.dataland.datalandbackend.openApiClient.model.SfdrGeneralGeneralFiscalYearDeviationOptions
import org.dataland.datalanddataexporter.TestDataProvider
import org.dataland.datalanddataexporter.utils.TransformationUtils.COMPANY_ID_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.COMPANY_NAME_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.LEI_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.REPORTING_PERIOD_HEADER
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.LocalDate

class TransformationUtilsTest {
    private val inputJson = File("./src/test/resources/csv/inputs/input.json")
    private val inconsistentJson = File("./src/test/resources/csv/inputs/inconsistent.json")
    private val referencedReportJson = File("./src/test/resources/csv/inputs/referencedReport.json")
    private val minimalSfdrDataJson = File("./src/test/resources/csv/inputs/minimalSfdrData.json")
    private val expectedTransformationRules =
        mapOf(
            "presentMapping" to "presentHeader",
            "notMapped" to "",
            "mappedButNoData" to "mappedButNoDataHeader",
            "nested.nestedMapping" to "nestedHeader",
        )
    private val expectedHeaders =
        listOf("presentHeader", "mappedButNoDataHeader", "nestedHeader") +
            listOf(COMPANY_ID_HEADER, COMPANY_NAME_HEADER, REPORTING_PERIOD_HEADER, LEI_HEADER)
    private val expectedJsonPaths = listOf("presentMapping", "notMapped", "nested.nestedMapping")
    private val expectedCsvData =
        mapOf("presentHeader" to "Here", "mappedButNoDataHeader" to "", "nestedHeader" to "NestedHere")

    @Test
    fun `check that the retrieved JSON paths are as expected`() {
        val jsonNode = ObjectMapper().readTree(inputJson)
        val result = TransformationUtils.getNonArrayLeafNodeFieldNames(jsonNode, "")
        assertEquals(expectedJsonPaths, result)
    }

    @Test
    fun `check that a duplicated header entry in the transformation rules throws an error`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            TransformationUtils.getHeaders(mapOf("key1" to "header1", "key2" to "header1"))
        }
    }

    @Test
    fun `check that getHeaders returns correct headers`() {
        val headers = TransformationUtils.getHeaders(expectedTransformationRules)
        assertEquals(expectedHeaders, headers)
    }

    @Test
    fun `check that checkConsistency does not throw an exception for consistent data`() {
        val jsonNode = ObjectMapper().readTree(inputJson)
        assertDoesNotThrow { TransformationUtils.checkConsistency(jsonNode, expectedTransformationRules) }
    }

    @Test
    fun `check that checkConsistency throws an exception for inconsistent data`() {
        val jsonNode = ObjectMapper().readTree(inconsistentJson)
        assertThrows<IllegalArgumentException> {
            TransformationUtils.checkConsistency(jsonNode, expectedTransformationRules)
        }
    }

    @Test
    fun `check that referenced reports are filtered out for the consistency check`() {
        val jsonNode = ObjectMapper().readTree(referencedReportJson)
        assertDoesNotThrow { TransformationUtils.checkConsistency(jsonNode, expectedTransformationRules) }
    }

    @Test
    fun `check that null valued fields are extracted as empty strings`() {
        val jsonNode = ObjectMapper().readTree("{\"nullValued\": null}")
        assertEquals("", TransformationUtils.getValueFromJsonNode(jsonNode, "nullValued"))
    }

    @Test
    fun `check that mapJsonToCsv returns correct csv data`() {
        val jsonNode = ObjectMapper().readTree(inputJson)
        val csvData = TransformationUtils.mapJsonToCsv(jsonNode, expectedTransformationRules)
        assertEquals(expectedCsvData, csvData)
    }

    @Test
    fun `check that the data class to json conversion correctly converts the date`() {
        val expectedJson = ObjectMapper().readTree(minimalSfdrDataJson)
        val input =
            CompanyAssociatedDataSfdrData(
                data =
                    SfdrData(
                        SfdrGeneral(
                            SfdrGeneralGeneral(
                                dataDate = LocalDate.parse("2022-01-01"),
                                fiscalYearEnd = LocalDate.parse("2022-01-01"),
                                fiscalYearDeviation = SfdrGeneralGeneralFiscalYearDeviationOptions.Deviation,
                            ),
                        ),
                    ),
                companyId = "companyId",
                reportingPeriod = "reportingPeriod",
            )
        val result = TransformationUtils.convertDataToJson(input)
        assertEquals(expectedJson, result)
    }

    @Test
    fun `check that exported sfdr with transformation rule to csv is as expected`() {
        val jsonNode = TestDataProvider.getMockSfdrJsonNode()
        val transformationRule =
            FileHandlingUtils.readTransformationConfig("./transformationRules/SfdrSqlServer.config")
        val csvData = TransformationUtils.mapJsonToCsv(jsonNode, transformationRule)

        val csvFile = File("./src/test/resources/csv/output.csv")
        val mapper = CsvMapper()

        val schema = CsvSchema.emptySchema().withHeader().withColumnSeparator('|')
        val expectedCsvData =
            mapper
                .readerFor(MutableMap::class.java)
                .with(schema)
                .readValue<Map<String, String>>(csvFile)
        assertEquals(csvData, expectedCsvData)
    }
}
