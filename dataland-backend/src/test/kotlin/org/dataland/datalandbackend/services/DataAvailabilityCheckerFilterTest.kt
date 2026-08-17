package org.dataland.datalandbackend.services

import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.UUID
import org.dataland.datalandbackend.utils.DEFAULT_COMPANY_ID as companyId
import org.dataland.datalandbackend.utils.DEFAULT_FRAMEWORK as framework
import org.dataland.datalandbackend.utils.DEFAULT_REPORTING_PERIOD as reportingPeriod

class DataAvailabilityCheckerFilterTest : DataAvailabilityCheckerTestBase() {
    @Test
    fun `check that an empty filter does not throw and returns an empty list`() {
        val result = assertDoesNotThrow { dataAvailabilityChecker.filterViewableDimensions(emptyList()) }
        assertTrue(result.isEmpty())
    }

    @Test
    fun `check that only the dataset stored as currentlyActive is returned`() {
        dbCreationUtils.storeDatasetMetaData(currentlyActive = true)
        dbCreationUtils.storeDatasetMetaData(currentlyActive = null)
        val results = dataAvailabilityChecker.filterViewableDimensions(listOf(datasetDimension))
        assert(results.size == 1) { EXACTLY_ONE_RESULT_MESSAGE }
        assert(results.first() == datasetDimension) { "The result should be the provided dimension." }
    }

    @Test
    fun `check that only the data point stored as currentlyActive is returned`() {
        dbCreationUtils.storeDataPointMetaData(currentlyActive = true)
        dbCreationUtils.storeDataPointMetaData(currentlyActive = null)
        val results = dataAvailabilityChecker.filterViewableDimensions(listOf(dataPointDimension))
        assert(results.size == 1) { EXACTLY_ONE_RESULT_MESSAGE }
        assert(results.first() == dataPointDimension) { "The result should be the provided dimension." }
    }

    @Test
    fun `check that in a mixed request both active dataset and active data point are returned`() {
        dbCreationUtils.storeDatasetMetaData()
        dbCreationUtils.storeDataPointMetaData()
        val results = dataAvailabilityChecker.filterViewableDimensions(listOf(datasetDimension, dataPointDimension))
        assert(results.size == 2) { BOTH_DIMENSIONS_SHOULD_BE_IN_RESULT_MESSAGE }
        assert(
            results.containsAll(
                listOf(
                    datasetDimension,
                    dataPointDimension,
                ),
            ),
        ) { BOTH_DIMENSIONS_SHOULD_BE_IN_RESULT_MESSAGE }
    }

    @ParameterizedTest
    @CsvSource(
        "1234, $framework, $reportingPeriod",
        "$companyId, unknowntype, $reportingPeriod",
        "$companyId, $framework, 12345",
    )
    fun `check that dimensions with non-existing frameworks are filtered out`(
        testCompanyId: String,
        dataType: String,
        testReportingPeriod: String,
    ) {
        dbCreationUtils.storeDatasetMetaData(dataType = dataType, reportingPeriod = testReportingPeriod)
        val results =
            dataAvailabilityChecker.filterViewableDimensions(
                listOf(
                    BasicDataDimensions(
                        companyId = testCompanyId,
                        dataType = dataType,
                        reportingPeriod = testReportingPeriod,
                    ),
                ),
            )
        assert(results.isEmpty()) { "Invalid dimensions should be filtered out." }
    }

    @Test
    fun `check that empty list is returned for a company without data`() {
        val dimensions =
            listOf(
                datasetDimension,
                datasetDimension,
                BasicDataDimensions(
                    companyId = companyId,
                    dataType = "lksg",
                    reportingPeriod = reportingPeriod,
                ),
                BasicDataDimensions(
                    companyId = companyId,
                    dataType = "sfdr",
                    reportingPeriod = "2025",
                ),
            )

        val results = dataAvailabilityChecker.filterViewableDimensions(dimensions)
        assert(results.isEmpty()) { "Incorrect number of dimensions found." }
    }

    @Test
    fun `check that for a mix of active, inactive and non-existent datasets the right Dimensions are returned - part 1`() {
        val otherYear = "2024"
        val otherFramework = "lksg"
        val otherId = UUID.randomUUID().toString()
        dbCreationUtils.storeDatasetMetaData()
        dbCreationUtils.storeDatasetMetaData(currentlyActive = null)
        dbCreationUtils.storeDatasetMetaData(dataType = otherFramework, reportingPeriod = otherYear)
        dbCreationUtils.storeDatasetMetaData(dataType = otherFramework)
        dbCreationUtils.storeDatasetMetaData(
            dataType = otherFramework,
            reportingPeriod = otherYear,
            currentlyActive = false,
        )

        val expectedDimensions =
            listOf(
                datasetDimension,
                BasicDataDimensions(companyId = companyId, dataType = otherFramework, reportingPeriod = otherYear),
                BasicDataDimensions(
                    companyId = companyId,
                    dataType = otherFramework,
                    reportingPeriod = reportingPeriod,
                ),
            )

        val unexpectedDimensions =
            listOf(
                BasicDataDimensions(companyId = companyId, dataType = otherFramework, reportingPeriod = otherYear),
                BasicDataDimensions(companyId = otherId, dataType = otherFramework, reportingPeriod = reportingPeriod),
                BasicDataDimensions(companyId = companyId, dataType = otherFramework, reportingPeriod = "2020"),
            )

        val results = dataAvailabilityChecker.filterViewableDimensions(expectedDimensions + unexpectedDimensions)
        assert(results.size == expectedDimensions.size) { "Incorrect number of dimensions found." }
        assert(expectedDimensions.containsAll(results)) { "Unexpected dimensions in result." }
    }

    @Test
    fun `check that for a mix of active, inactive and non-existent datasets the right Dimensions are returned - part 2`() {
        val anotherYear = "2024"
        val anotherDataPointType = "anotherDataPoint"
        val anotherId = UUID.randomUUID().toString()
        dbCreationUtils.storeDataPointMetaData()
        dbCreationUtils.storeDataPointMetaData(currentlyActive = null)
        dbCreationUtils.storeDataPointMetaData(dataPointType = anotherDataPointType, reportingPeriod = anotherYear)
        dbCreationUtils.storeDataPointMetaData(dataPointType = anotherDataPointType)
        dbCreationUtils.storeDataPointMetaData(
            dataPointType = anotherDataPointType,
            reportingPeriod = anotherYear,
            currentlyActive = false,
        )

        val expectedDimensions =
            listOf(
                dataPointDimension,
                BasicDataDimensions(
                    companyId = companyId,
                    dataType = anotherDataPointType,
                    reportingPeriod = anotherYear,
                ),
                BasicDataDimensions(
                    companyId = companyId,
                    dataType = anotherDataPointType,
                    reportingPeriod = reportingPeriod,
                ),
            )

        val unexpectedDimensions =
            listOf(
                BasicDataDimensions(
                    companyId = companyId,
                    dataType = anotherDataPointType,
                    reportingPeriod = anotherYear,
                ),
                BasicDataDimensions(
                    companyId = anotherId,
                    dataType = anotherDataPointType,
                    reportingPeriod = reportingPeriod,
                ),
                BasicDataDimensions(companyId = companyId, dataType = anotherDataPointType, reportingPeriod = "2020"),
            )

        val results = dataAvailabilityChecker.filterViewableDimensions(expectedDimensions + unexpectedDimensions)

        assert(results.size == expectedDimensions.size) { "Incorrect number of data points found." }
        val resultingDimensions = results.map { BasicDataDimensions(it.companyId, it.dataType, it.reportingPeriod) }
        assert(expectedDimensions.containsAll(resultingDimensions)) { "Resulting dimensions did not match expected dimensions." }
    }
}
