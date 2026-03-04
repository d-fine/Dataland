package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataPointMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataPointToValidate
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewSupportService
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.datalandspecificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.datalandspecificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DatasetReviewSupportServiceTest {
    private val dataPointControllerApi: DataPointControllerApi = mock()
    private val metaDataControllerApi: MetaDataControllerApi = mock()
    private val specificationControllerApi: SpecificationControllerApi = mock()
    private val dataPointQaReportRepository: DataPointQaReportRepository = mock()

    private val dummyDatapoint = """{"foo":"bar"}"""

    private val service =
        DatasetReviewSupportService(
            dataPointControllerApi,
            metaDataControllerApi,
            specificationControllerApi,
            dataPointQaReportRepository,
        )

    @Test
    fun `getDataMetaDataInfo delegates to MetaDataControllerApi and returns its result`() {
        val datasetId = UUID.randomUUID().toString()
        val expected =
            DataMetaInformation(
                dataId = datasetId,
                companyId = UUID.randomUUID().toString(),
                dataType = DataTypeEnum.sfdr,
                uploadTime = 1L,
                reportingPeriod = "2026",
                currentlyActive = false,
                qaStatus = QaStatus.Pending,
            )

        whenever(metaDataControllerApi.getDataMetaInfo(datasetId))
            .thenReturn(expected)

        val result = service.getDataMetaInfo(datasetId)

        assertEquals(expected, result)
        verify(metaDataControllerApi).getDataMetaInfo(datasetId)
    }

    @Test
    fun `getContainedDataPoints delegates to MetaDataControllerApi and returns its result`() {
        val datasetId = UUID.randomUUID().toString()
        val expected = mapOf("type1" to "dp1", "type2" to "dp2")

        whenever(metaDataControllerApi.getContainedDataPoints(datasetId))
            .thenReturn(expected)

        val result = service.getContainedDataPoints(datasetId)

        assertEquals(expected, result)
        verify(metaDataControllerApi).getContainedDataPoints(datasetId)
    }

    @Test
    fun `getDataPointType returns type from DataPointControllerApi`() {
        val dataPointId = UUID.randomUUID()
        val expectedType = "dummyType"

        val metaInfo =
            DataPointMetaInformation(
                dataPointId = dataPointId.toString(),
                dataPointType = expectedType,
                companyId = "dummyCompanyId",
                reportingPeriod = "2024",
                uploaderUserId = "dummyUserId",
                uploadTime = 1234567890L,
                currentlyActive = true,
                qaStatus = QaStatus.Pending,
            )

        whenever(dataPointControllerApi.getDataPointMetaInfo(dataPointId.toString()))
            .thenReturn(metaInfo)

        val result = service.getDataPointType(dataPointId)

        assertEquals(expectedType, result)
        verify(dataPointControllerApi).getDataPointMetaInfo(dataPointId.toString())
    }

    @Test
    fun `validateCustomDataPoint calls DataPointControllerApi with correct payload`() {
        val dataPoint = dummyDatapoint
        val dataPointType = "dummyType"

        service.validateCustomDataPoint(dataPoint, dataPointType)

        val captor = argumentCaptor<DataPointToValidate>()
        verify(dataPointControllerApi).validateDataPoint(captor.capture())

        val sent = captor.firstValue
        assertEquals(dataPoint, sent.dataPoint)
        assertEquals(dataPointType, sent.dataPointType)
    }

    @Test
    fun `getFrameworksForDataPointType maps usedBy ids from SpecificationControllerApi`() {
        val dataPointType = "dummyType"
        val ref = "dummy ref"
        val expectedFrameworks = listOf("sfdr", "eutaxonomy-financials")

        val spec =
            DataPointTypeSpecification(
                dataPointType =
                    IdWithRef(
                        id = dataPointType,
                        ref = ref,
                    ),
                name = "Some name",
                businessDefinition = "Some business definition",
                dataPointBaseType =
                    IdWithRef(
                        id = "baseType",
                        ref = ref,
                    ),
                usedBy =
                    listOf(
                        IdWithRef(id = "sfdr", ref = ref),
                        IdWithRef(id = "eutaxonomy-financials", ref = ref),
                    ),
                constraints = null,
            )

        whenever(specificationControllerApi.getDataPointTypeSpecification(dataPointType))
            .thenReturn(spec)

        val result = service.getFrameworksForDataPointType(dataPointType)

        assertEquals(expectedFrameworks, result)
        verify(specificationControllerApi).getDataPointTypeSpecification(dataPointType)
    }

    @Test
    fun `findQaReportsWithDetails returns entities from repository including inactive`() {
        val dataPointIds = listOf("dp1", "dp2")

        val entity1 =
            DataPointQaReportEntity(
                qaReportId = "qa1",
                dataPointId = dataPointIds[0],
                comment = "test comment",
                verdict = QaReportDataPointVerdict.QaAccepted,
                correctedData = dummyDatapoint,
                dataPointType = "dummyType",
                reporterUserId = "dummyUserId",
                uploadTime = 1623456789L,
                active = true,
            )
        val entity2 =
            DataPointQaReportEntity(
                qaReportId = "qa2",
                dataPointId = dataPointIds[1],
                comment = "test comment",
                verdict = QaReportDataPointVerdict.QaAccepted,
                correctedData = dummyDatapoint,
                dataPointType = "dummyType",
                reporterUserId = "dummyUserId",
                uploadTime = 1623456789L,
                active = false,
            )

        whenever(
            dataPointQaReportRepository.searchQaReportMetaInformation(
                dataPointIds = dataPointIds,
                showInactive = true,
                reporterUserId = null,
            ),
        ).thenReturn(listOf(entity1, entity2))

        val result = service.findQaReportsWithDetails(dataPointIds)

        assertEquals(listOf(entity1, entity2), result)

        verify(dataPointQaReportRepository).searchQaReportMetaInformation(
            dataPointIds = dataPointIds,
            showInactive = true,
            reporterUserId = null,
        )
    }

    @Test
    fun `findDataPointTypeUsingQaReportId delegates to repository and returns type`() {
        val qaReportId = UUID.randomUUID()
        val expectedType = "dummyType"

        whenever(dataPointQaReportRepository.findDataPointTypeUsingId(qaReportId.toString()))
            .thenReturn(expectedType)

        val result = service.findDataPointTypeUsingQaReportId(qaReportId)

        assertEquals(expectedType, result)
        verify(dataPointQaReportRepository).findDataPointTypeUsingId(qaReportId.toString())
    }
}
