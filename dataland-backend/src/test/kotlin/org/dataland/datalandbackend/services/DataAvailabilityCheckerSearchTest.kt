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
    fun `searchViewableDimensions with filters - filter matching active dataset returns correct dimensions`() {
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
    fun `searchViewableDimensions with filters - filter matching active data point returns correct dimensions`() {
        dbCreationUtils.storeDataPointMetaData()
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(dataPointType),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.size == 1) { EXACTLY_ONE_RESULT_MESSAGE }
        assert(results.first() == dataPointDimension) { "The result should match the stored data point dimension." }
    }

    @Test
    fun `searchViewableDimensions with filters - no matching data returns empty result`() {
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
    fun `searchViewableDimensions with filters - inactive data is excluded`() {
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
    fun `searchViewableDimensions with filters - empty lists act as wildcards`() {
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
    fun `searchViewableDimensions with filters - multiple frameworks filter returns only matching framework`() {
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
    fun `searchViewableDimensions with filters - multiple periods filter returns only matching period`() {
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
    fun `searchViewableDimensions with filters - mixed datasets and data points with cross-cutting filter`() {
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
    fun `searchViewableDimensions - data points with only ignored fields do not yield a dimension`() {
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
