package org.dataland.datalandqaservice.controller

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataPointMetaInformation
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.DatasetReviewController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewService
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

class DatasetReviewControllerTest {
    private val mockDatasetReviewRepository = mock<DatasetReviewRepository>()
    private val mockDataPointControllerApi = mock<DataPointControllerApi>()
    private val mockDataPointQaReportRepository = mock<DataPointQaReportRepository>()
    private val mockSpecificationControllerApi = mock<SpecificationControllerApi>()
    private val mockMetaDataControllerApi = mock<MetaDataControllerApi>()

    private val datasetReviewService =
        DatasetReviewService(
            mockDatasetReviewRepository,
            mockDataPointControllerApi,
            mockDataPointQaReportRepository,
            mockSpecificationControllerApi,
            mockMetaDataControllerApi,
        )

    private val datasetReviewController = DatasetReviewController(datasetReviewService)
    private val dummyUserId = UUID.randomUUID()
    private val dummyDataPointType = "dummy datapoint type"
    private val dummyQaReportId = UUID.randomUUID()
    private val datasetReviewEntity =
        DatasetReviewEntity(
            dataSetReviewId = UUID.randomUUID(),
            datasetId = UUID.randomUUID(),
            companyId = UUID.randomUUID(),
            dataType = "sfdr",
            reportingPeriod = "2026",
            reviewerUserId = dummyUserId,
            qaReports =
                mutableSetOf(
                    DataPointQaReportEntity(
                        qaReportId = dummyQaReportId.toString(),
                        comment = "",
                        verdict = QaReportDataPointVerdict.QaAccepted,
                        correctedData = null,
                        dataPointId = "",
                        dataPointType = dummyDataPointType,
                        reporterUserId = "",
                        uploadTime = 0L,
                        active = false,
                    ),
                ),
        )

    @BeforeEach
    fun setup() {
        reset(mockDatasetReviewRepository)
        AuthenticationMock.mockSecurityContext(
            "data.admin@example.com",
            dummyUserId.toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        doReturn(Optional.of(datasetReviewEntity)).whenever(mockDatasetReviewRepository).findById(any())
        whenever(mockDatasetReviewRepository.save(any())).thenAnswer { it.arguments[0] }
    }

    @Test
    fun `check that patching reviewer works as expected`() {
        datasetReviewController.setReviewer(UUID.randomUUID().toString())
        val datasetReviewCaptor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(datasetReviewCaptor.capture())
        assertEquals(
            dummyUserId,
            datasetReviewCaptor.firstValue.reviewerUserId,
        )
    }

    @Test
    fun `check that accepting original datapoint works as expected`() {
        val dummyDatapointId = UUID.randomUUID().toString()
        doReturn(mapOf(dummyDataPointType to dummyDatapointId))
            .whenever(mockMetaDataControllerApi)
            .getContainedDataPoints(any())
        doReturn(
            DataPointMetaInformation(
                dummyDatapointId,
                dummyDataPointType,
                "companyId",
                "2026",
                "userId",
                0L,
                false,
                QaStatus.Pending,
            ),
        ).whenever(mockDataPointControllerApi)
            .getDataPointMetaInfo(any())

        datasetReviewController.acceptOriginalDatapoint(UUID.randomUUID().toString(), dummyDatapointId)

        val datasetReviewCaptor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(datasetReviewCaptor.capture())

        val capturedDatasetReview = datasetReviewCaptor.firstValue
        assert(dummyDataPointType in capturedDatasetReview.approvedDataPointIds)
        assert(dummyDataPointType !in capturedDatasetReview.approvedQaReportIds)
        assert(dummyDataPointType !in capturedDatasetReview.approvedCustomDataPointIds)
    }

    @Test
    fun `check that accepting original datapoint throws an exception when a datapoint id`() {
        val dummyDatapointId = UUID.randomUUID().toString()
        doReturn(mapOf(dummyDataPointType to UUID.randomUUID().toString()))
            .whenever(mockMetaDataControllerApi)
            .getContainedDataPoints(any())
        assertThrows<ResourceNotFoundApiException> {
            datasetReviewController.acceptOriginalDatapoint(UUID.randomUUID().toString(), dummyDatapointId)
        }
    }

    @Test
    fun `check that accepting qa report works as expected`() {
        datasetReviewController.acceptQaReport(UUID.randomUUID().toString(), dummyQaReportId.toString())

        val datasetReviewCaptor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(datasetReviewCaptor.capture())

        val capturedDatasetReview = datasetReviewCaptor.firstValue
        assert(dummyDataPointType in capturedDatasetReview.approvedQaReportIds)
        assert(dummyDataPointType !in capturedDatasetReview.approvedDataPointIds)
        assert(dummyDataPointType !in capturedDatasetReview.approvedCustomDataPointIds)
    }

    @Test
    fun `check that accepting qa report throws an exception when it is not part of qaReports`() {
        val newDummyQaReportId = UUID.randomUUID().toString()
        assertThrows<ResourceNotFoundApiException> {
            datasetReviewController.acceptQaReport(UUID.randomUUID().toString(), newDummyQaReportId)
        }
    }
}
