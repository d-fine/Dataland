package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.DataDimensionFilter
import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.datapoints.DataPointCalculator
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.dataland.specificationservice.openApiClient.model.SimpleFrameworkSpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DataPointUtilsTest {
    private val specificationClient = mock<SpecificationControllerApi>()
    private val metaDataManager = mock<DataPointMetaInformationManager>()
    private val dataCompositionService = mock<DataCompositionService>()
    private val dataPointCalculator = mock<DataPointCalculator>()

    private lateinit var dataPointUtils: DataPointUtils

    private val companyId = "company-id"
    private val framework = "test-framework"
    private val directDataPointType = "directDataPointType"
    private val calculatedSourceType = "calculatedSourceType"
    private val ignoredDataPointType = "extendedDateFiscalYearEnd"

    private fun makeMetaData(
        dataPointType: String,
        companyId: String = this.companyId,
        reportingPeriod: String,
    ) = DataPointMetaInformationEntity(
        dataPointId = "$companyId-$reportingPeriod-$dataPointType",
        companyId = companyId,
        dataPointType = dataPointType,
        reportingPeriod = reportingPeriod,
        uploaderUserId = "test-user-id",
        uploadTime = 0,
        currentlyActive = true,
        qaStatus = QaStatus.Accepted,
    )

    @BeforeEach
    fun setUp() {
        dataPointUtils =
            DataPointUtils(
                specificationClient = specificationClient,
                metaDataManager = metaDataManager,
                dataCompositionService = dataCompositionService,
                dataPointCalculator = dataPointCalculator,
            )
        doReturn(mock<FrameworkSpecification>()).whenever(specificationClient).getFrameworkSpecification(framework)
        doReturn(
            listOf(SimpleFrameworkSpecification(framework = IdWithRef(id = framework, ref = "dummy"), name = "Test")),
        ).whenever(specificationClient).listFrameworkSpecifications()
        doReturn(listOf(directDataPointType, calculatedSourceType, ignoredDataPointType))
            .whenever(dataCompositionService)
            .getRelevantDataPointTypes(framework)
        doReturn(emptyList<DataPointMetaInformationEntity>())
            .whenever(metaDataManager)
            .getActiveDataPointMetaInformation(any(), any())
        doReturn(emptyList<DataPointMetaInformationEntity>())
            .whenever(metaDataManager)
            .getActiveDataPointMetaInformationList(any())
        doReturn(emptySet<BasicDataPointDimensions>())
            .whenever(dataPointCalculator)
            .getActiveSourceDataPointDimensions(any<Collection<String>>(), any<String>())
        doReturn(emptySet<BasicDataPointDimensions>())
            .whenever(dataPointCalculator)
            .getActiveSourceDataPointDimensions(any<Collection<String>>(), any<DataDimensionFilter>())
    }

    @Test
    fun `check that reporting periods include direct and calculatable data points and are deduplicated`() {
        doReturn(
            listOf(
                makeMetaData(directDataPointType, reportingPeriod = "2022"),
                makeMetaData(directDataPointType, reportingPeriod = "2023"),
            ),
        ).whenever(metaDataManager).getActiveDataPointMetaInformation(any(), any())
        doReturn(
            setOf(
                BasicDataPointDimensions(companyId, calculatedSourceType, "2023"),
                BasicDataPointDimensions(companyId, calculatedSourceType, "2024"),
            ),
        ).whenever(dataPointCalculator).getActiveSourceDataPointDimensions(any<Collection<String>>(), any<String>())

        val result = dataPointUtils.getAllReportingPeriodsWithActiveDataPoints(companyId, framework)

        assertEquals(setOf("2022", "2023", "2024"), result)
        verify(metaDataManager).getActiveDataPointMetaInformation(
            dataPointTypes = setOf(directDataPointType, calculatedSourceType),
            companyId = companyId,
        )
        verify(dataPointCalculator).getActiveSourceDataPointDimensions(
            dataPointTypes = setOf(directDataPointType, calculatedSourceType),
            companyId = companyId,
        )
    }

    @Test
    fun `check that reporting period lookup returns empty set for unknown framework`() {
        val unknownFramework = "unknown-framework"
        doThrow(ClientException()).whenever(specificationClient).getFrameworkSpecification(unknownFramework)

        val result = dataPointUtils.getAllReportingPeriodsWithActiveDataPoints(companyId, unknownFramework)

        assertEquals(emptySet<String>(), result)
    }

    @Test
    fun `check that active data dimensions include calculatable framework dimensions`() {
        val sourceFramework = "framework-a"
        val targetFramework = "framework-b"
        val sourceDataPointType = "sourceDataPointType"
        val targetDataPointType = "targetDataPointType"
        val filter =
            DataDimensionFilter(
                companyIds = listOf(companyId),
                dataTypes = listOf(targetFramework),
                reportingPeriods = listOf("2024"),
            )
        doReturn(
            listOf(
                SimpleFrameworkSpecification(framework = IdWithRef(id = sourceFramework, ref = "dummy"), name = "Source"),
                SimpleFrameworkSpecification(framework = IdWithRef(id = targetFramework, ref = "dummy"), name = "Target"),
            ),
        ).whenever(specificationClient).listFrameworkSpecifications()
        doReturn(listOf(targetDataPointType))
            .whenever(dataCompositionService)
            .getRelevantDataPointTypes(targetFramework)
        doReturn(emptyList<DataPointMetaInformationEntity>())
            .whenever(metaDataManager)
            .getActiveDataPointMetaInformationList(
                argThat {
                    companyIds == filter.companyIds &&
                        dataTypes == listOf(targetDataPointType) &&
                        reportingPeriods == filter.reportingPeriods
                },
            )
        doReturn(setOf(BasicDataPointDimensions(companyId, sourceDataPointType, "2024")))
            .whenever(dataPointCalculator)
            .getActiveSourceDataPointDimensions(listOf(targetDataPointType), filter)

        val result = dataPointUtils.getActiveDataDimensionsFromDataPoints(filter).toSet()

        assertEquals(
            setOf(
                BasicDataDimensions(companyId, targetFramework, "2024"),
            ),
            result,
        )
        verify(dataPointCalculator).getActiveSourceDataPointDimensions(listOf(targetDataPointType), filter)
    }

    @Test
    fun `check that ignored-only direct metadata does not create framework dimensions`() {
        val filter = DataDimensionFilter(companyIds = listOf(companyId), reportingPeriods = listOf("2024"))
        doReturn(listOf(makeMetaData(ignoredDataPointType, reportingPeriod = "2024")))
            .whenever(metaDataManager)
            .getActiveDataPointMetaInformationList(
                argThat { companyIds == filter.companyIds && dataTypes == null && reportingPeriods == filter.reportingPeriods },
            )
        doReturn(listOf(makeMetaData(ignoredDataPointType, reportingPeriod = "2024")))
            .whenever(metaDataManager)
            .getActiveDataPointMetaInformationList(
                argThat {
                    companyIds == filter.companyIds &&
                        dataTypes == listOf(directDataPointType, calculatedSourceType, ignoredDataPointType) &&
                        reportingPeriods == filter.reportingPeriods
                },
            )

        val result = dataPointUtils.getActiveDataDimensionsFromDataPoints(filter).toSet()

        assertEquals(setOf(BasicDataDimensions(companyId, ignoredDataPointType, "2024")), result)
    }
}
