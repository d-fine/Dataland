package org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReview
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportIdWithUploaderCompanyId
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewSupportService
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.util.Optional
import java.util.UUID

class DatasetReviewServiceTest {
    private val mockDatasetReviewRepository = mock<DatasetReviewRepository>()
    private val mockDatasetReviewSupportService = mock<DatasetReviewSupportService>()
    private val mockInheritedRolesControllerApi = mock<InheritedRolesControllerApi>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()

    private val datasetReviewService =
        DatasetReviewService(
            mockDatasetReviewRepository,
            mockDatasetReviewSupportService,
            mockInheritedRolesControllerApi,
            mockKeycloakUserService,
        )

    private val dummyUserId = UUID.randomUUID()
    private val dummyDataPointType = "dummy datapoint type"
    private val dummyQaReportId = UUID.randomUUID()
    private val dummyCompanyId = UUID.randomUUID()
    private val dummyDatasetId = UUID.randomUUID()

    private val datasetReviewEntity =
        DatasetReviewEntity(
            dataSetReviewId = UUID.randomUUID(),
            datasetId = dummyDatasetId,
            companyId = dummyCompanyId,
            dataType = "sfdr",
            reportingPeriod = "2026",
            reviewerUserId = dummyUserId,
            qaReports = setOf(QaReportIdWithUploaderCompanyId(dummyQaReportId, null)),
        )

    @BeforeEach
    fun setup() {
        reset(
            mockDatasetReviewRepository,
            mockDatasetReviewSupportService,
            mockInheritedRolesControllerApi,
            mockKeycloakUserService,
        )

        AuthenticationMock.mockSecurityContext(
            "data.admin@example.com",
            dummyUserId.toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        doReturn(Optional.of(datasetReviewEntity)).whenever(mockDatasetReviewRepository).findById(any())
        whenever(mockDatasetReviewRepository.save(any())).thenAnswer { it.arguments[0] }

        doReturn(dummyDataPointType)
            .whenever(mockDatasetReviewSupportService)
            .findDataPointTypeUsingQaReportId(any())

        whenever(mockKeycloakUserService.getUser(any())).thenReturn(
            KeycloakUserInfo(
                email = "reviewer@example.com",
                userId = dummyUserId.toString(),
                firstName = "Dummy",
                lastName = "User",
            ),
        )
    }

    @Test
    fun `getDatasetReviewsByDatasetId returns expected responses`() {
        doReturn(listOf(datasetReviewEntity))
            .whenever(mockDatasetReviewRepository)
            .findAllByDatasetId(any())

        val result: List<DatasetReviewResponse> =
            datasetReviewService.getDatasetReviewsByDatasetId(dummyDatasetId)

        assertEquals(1, result.size)
        val response = result.first()
        assertEquals(datasetReviewEntity.dataSetReviewId.toString(), response.dataSetReviewId)
        assertEquals(datasetReviewEntity.datasetId.toString(), response.datasetId)
        assertEquals(datasetReviewEntity.companyId.toString(), response.companyId)
        assertEquals(dummyUserId.toString(), response.reviewerUserId)
        assertEquals("Dummy User", response.reviewerUserName)
    }

    @Test
    fun `getDatasetReviewsByDatasetId returns empty list when none exist`() {
        doReturn(emptyList<DatasetReviewEntity>())
            .whenever(mockDatasetReviewRepository)
            .findAllByDatasetId(any())

        val result = datasetReviewService.getDatasetReviewsByDatasetId(UUID.randomUUID())

        assertEquals(0, result.size)
    }

    @Test
    fun `createDatasetReview builds entity correctly`() {
        val dummyDatapointId = UUID.randomUUID().toString()
        doReturn(mapOf(dummyDataPointType to dummyDatapointId))
            .whenever(mockDatasetReviewSupportService)
            .getContainedDataPoints(any())

        val dummyQaReportIds = listOf(dummyQaReportId.toString())
        doReturn(dummyQaReportIds)
            .whenever(mockDatasetReviewSupportService)
            .findQaReportIdsForDataPoints(any())

        val uploaderCompanyId = UUID.randomUUID()
        doReturn(mapOf(uploaderCompanyId.toString() to listOf("Role")))
            .whenever(mockInheritedRolesControllerApi)
            .getInheritedRoles(any())

        val createdDatasetReview =
            datasetReviewService.createDatasetReview(
                DatasetReview(
                    datasetId = UUID.randomUUID().toString(),
                    companyId = dummyCompanyId.toString(),
                    dataType = "sfdr",
                    reportingPeriod = "2026",
                ),
            )

        assertEquals(dummyCompanyId.toString(), createdDatasetReview.companyId)
        assertEquals(
            setOf(QaReportIdWithUploaderCompanyId(dummyQaReportId, uploaderCompanyId)),
            createdDatasetReview.qaReports,
        )
    }

    @Test
    fun `setReviewer sets reviewer to current user`() {
        val newReviewId = UUID.randomUUID()

        datasetReviewService.setReviewer(newReviewId)

        val captor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(captor.capture())
        assertEquals(dummyUserId, captor.firstValue.reviewerUserId)
    }

    @Test
    fun `acceptOriginalDatapoint approves datapoint and clears others`() {
        val dummyDatapointId = UUID.randomUUID()
        doReturn(mapOf(dummyDataPointType to dummyDatapointId.toString()))
            .whenever(mockDatasetReviewSupportService)
            .getContainedDataPoints(any())
        doReturn(dummyDataPointType)
            .whenever(mockDatasetReviewSupportService)
            .getDataPointType(any())

        datasetReviewService.acceptOriginalDatapoint(UUID.randomUUID(), dummyDatapointId)

        val captor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(captor.capture())
        val captured = captor.firstValue

        assertTrue(dummyDataPointType in captured.approvedDataPointIds)
        assertFalse(dummyDataPointType in captured.approvedQaReportIds)
        assertFalse(dummyDataPointType in captured.approvedCustomDataPointIds)
    }

    @Test
    fun `acceptOriginalDatapoint throws when datapoint not part of dataset`() {
        val dummyDatapointId = UUID.randomUUID()
        doReturn(mapOf(dummyDataPointType to UUID.randomUUID().toString()))
            .whenever(mockDatasetReviewSupportService)
            .getContainedDataPoints(any())

        assertThrows<ResourceNotFoundApiException> {
            datasetReviewService.acceptOriginalDatapoint(UUID.randomUUID(), dummyDatapointId)
        }
    }

    @Test
    fun `acceptQaReport approves qa report and clears others`() {
        datasetReviewService.acceptQaReport(UUID.randomUUID(), dummyQaReportId)

        val captor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(captor.capture())
        val captured = captor.firstValue

        assertTrue(dummyDataPointType in captured.approvedQaReportIds)
        assertFalse(dummyDataPointType in captured.approvedDataPointIds)
        assertFalse(dummyDataPointType in captured.approvedCustomDataPointIds)
    }

    @Test
    fun `acceptQaReport throws when qa report not part of dataset review`() {
        val newDummyQaReportId = UUID.randomUUID()

        assertThrows<ResourceNotFoundApiException> {
            datasetReviewService.acceptQaReport(UUID.randomUUID(), newDummyQaReportId)
        }
    }

    @Test
    fun `acceptCustomDataPoint approves custom datapoint and clears others`() {
        val datasetReviewId = UUID.randomUUID()
        val entity =
            DatasetReviewEntity(
                dataSetReviewId = datasetReviewId,
                datasetId = dummyDatasetId,
                companyId = dummyCompanyId,
                dataType = "sfdr",
                reportingPeriod = "2026",
                reviewerUserId = dummyUserId,
                qaReports = emptySet(),
            ).apply {
                approvedDataPointIds[dummyDataPointType] = UUID.randomUUID()
                approvedQaReportIds[dummyDataPointType] = UUID.randomUUID()
            }

        doReturn(Optional.of(entity))
            .whenever(mockDatasetReviewRepository)
            .findById(datasetReviewId)

        val dataPointJson = """{"value": 1}"""
        val dataPointType = dummyDataPointType

        doReturn(listOf("sfdr"))
            .whenever(mockDatasetReviewSupportService)
            .getFrameworksForDataPointType(dataPointType)

        datasetReviewService.acceptCustomDataPoint(datasetReviewId, dataPointJson, dataPointType)

        val captor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(captor.capture())
        val savedDatasetReview = captor.firstValue

        assertEquals(dataPointJson, savedDatasetReview.approvedCustomDataPointIds[dataPointType])
        assertFalse(savedDatasetReview.approvedDataPointIds.containsKey(dataPointType))
        assertFalse(savedDatasetReview.approvedQaReportIds.containsKey(dataPointType))
        verify(mockDatasetReviewSupportService).validateCustomDataPoint(dataPointJson, dataPointType)
    }

    @Test
    fun `acceptCustomDataPoint wraps HttpClientErrorException from framework lookup into InvalidInputApiException`() {
        val datasetReviewId = UUID.randomUUID()
        val entity =
            DatasetReviewEntity(
                dataSetReviewId = datasetReviewId,
                datasetId = dummyDatasetId,
                companyId = dummyCompanyId,
                dataType = "sfdr",
                reportingPeriod = "2026",
                reviewerUserId = dummyUserId,
                qaReports = emptySet(),
            )

        doReturn(Optional.of(entity))
            .whenever(mockDatasetReviewRepository)
            .findById(datasetReviewId)

        val type = "unknown-type"

        whenever(mockDatasetReviewSupportService.getFrameworksForDataPointType(type))
            .thenThrow(HttpClientErrorException(HttpStatus.NOT_FOUND))

        assertThrows<InvalidInputApiException> {
            datasetReviewService.acceptCustomDataPoint(datasetReviewId, "{}", type)
        }
    }

    @Test
    fun `acceptCustomDataPoint throws InvalidInputApiException when type not part of framework`() {
        val datasetReviewId = UUID.randomUUID()
        val entity =
            DatasetReviewEntity(
                dataSetReviewId = datasetReviewId,
                datasetId = dummyDatasetId,
                companyId = dummyCompanyId,
                dataType = "sfdr",
                reportingPeriod = "2026",
                reviewerUserId = dummyUserId,
                qaReports = emptySet(),
            )

        doReturn(Optional.of(entity))
            .whenever(mockDatasetReviewRepository)
            .findById(datasetReviewId)

        val type = dummyDataPointType

        doReturn(listOf("other-framework"))
            .whenever(mockDatasetReviewSupportService)
            .getFrameworksForDataPointType(type)

        assertThrows<InvalidInputApiException> {
            datasetReviewService.acceptCustomDataPoint(datasetReviewId, "{}", type)
        }
    }

    @Test
    fun `setReviewer throws ResourceNotFound when datasetReview does not exist`() {
        doReturn(Optional.empty<DatasetReviewEntity>())
            .whenever(mockDatasetReviewRepository)
            .findById(any())

        assertThrows<ResourceNotFoundApiException> {
            datasetReviewService.setReviewer(UUID.randomUUID())
        }
    }

    @Test
    fun `acceptOriginalDatapoint throws InsufficientRights when current user is not reviewer`() {
        val datasetReviewId = UUID.randomUUID()
        val entity =
            DatasetReviewEntity(
                dataSetReviewId = datasetReviewId,
                datasetId = dummyDatasetId,
                companyId = dummyCompanyId,
                dataType = "sfdr",
                reportingPeriod = "2026",
                reviewerUserId = UUID.randomUUID(),
                qaReports = emptySet(),
            )

        doReturn(Optional.of(entity))
            .whenever(mockDatasetReviewRepository)
            .findById(datasetReviewId)

        AuthenticationMock.mockSecurityContext(
            "other@example.com",
            dummyUserId.toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        assertThrows<InsufficientRightsApiException> {
            datasetReviewService.acceptOriginalDatapoint(datasetReviewId, UUID.randomUUID())
        }
    }
}
