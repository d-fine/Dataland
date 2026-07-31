package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.utils.DataAvailabilityIgnoredFieldsUtils
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.dataland.datalandbackend.utils.DEFAULT_COMPANY_ID as companyId
import org.dataland.datalandbackend.utils.DEFAULT_DATA_POINT_TYPE as dataPointType
import org.dataland.datalandbackend.utils.DEFAULT_FRAMEWORK as framework
import org.dataland.datalandbackend.utils.DEFAULT_REPORTING_PERIOD as reportingPeriod

class DataAvailabilityCheckerSearchTest : DataAvailabilityCheckerTestBase() {
    @Test
    fun `check that filter matching an active dataset returns its dimensions`() {
        dbCreationUtils.storeDatasetMetaData()
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.size == 1) { EXACTLY_ONE_RESULT_MESSAGE }
        assert(results.first() == datasetDimension) { "The result should match the stored dataset dimension." }
    }

    @Test
    fun `check that empty list is returned for a company without data`() {
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.isEmpty()) { "No matching data should return empty result." }
    }

    @Test
    fun `check that dimensions with non-existing frameworks are filtered out`() {
        dbCreationUtils.storeDatasetMetaData(currentlyActive = null)
        dbCreationUtils.storeDatasetMetaData(currentlyActive = false)
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.isEmpty()) { "Inactive data should be excluded." }
    }

    @Test
    fun `check that an empty lists in the DataDimensionQuery is treated as a wildcards`() {
        dbCreationUtils.storeDatasetMetaData()
        dbCreationUtils.storeDataPointMetaData()
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = emptyList(),
                    reportingPeriods = emptyList(),
                ),
            )
        assert(
            results.containsAll(
                listOf(
                    datasetDimension,
                    dataPointDimension,
                ),
            ),
        ) { BOTH_DIMENSIONS_SHOULD_BE_IN_RESULT_MESSAGE }
    }

    @Test
    fun `check that filter returns only matching framework from a company with multiple available frameworks`() {
        val otherFramework = "lksg"
        dbCreationUtils.storeDatasetMetaData(dataType = framework)
        dbCreationUtils.storeDatasetMetaData(dataType = otherFramework)
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.size == 1) { "Only one framework's data should be returned." }
        assert(results.first() == datasetDimension) { "Only the filtered framework should be in the result." }
    }

    @Test
    fun `check that filter returns only matching period from a company with multiple available periods`() {
        val otherPeriod = "2024"
        dbCreationUtils.storeDatasetMetaData(reportingPeriod = reportingPeriod)
        dbCreationUtils.storeDatasetMetaData(reportingPeriod = otherPeriod)
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.size == 1) { "Only one period's data should be returned." }
        assert(results.first() == datasetDimension) { "Only the filtered period should be in the result." }
    }

    @Test
    fun `check that in a mixed request both active dataset and active data point are returned`() {
        dbCreationUtils.storeDatasetMetaData(dataType = framework)
        dbCreationUtils.storeDataPointMetaData(dataPointType = dataPointType)
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework, dataPointType),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.size == 2) { "Both dataset and data point dimensions should be returned." }
        assert(
            results.containsAll(
                listOf(
                    datasetDimension,
                    dataPointDimension,
                ),
            ),
        ) { BOTH_DIMENSIONS_SHOULD_BE_IN_RESULT_MESSAGE }
    }

    @Test
    fun `check that a dataset with only ignored active data points do not yield a dimension`() {
        val ignoredDataPointType = DataAvailabilityIgnoredFieldsUtils.getIgnoredFields().first()
        doReturn(null).whenever(specificationClient).getDataPointTypeSpecification(ignoredDataPointType)
        dbCreationUtils.storeDataPointMetaData(dataPointType = ignoredDataPointType)
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(ignoredDataPointType),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.none { it.dataType == framework }) { "Ignored-only data points must not produce a framework dimension." }
    }
}
