package org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.JudgementDetailsPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetJudgementRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementCreationService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementSupportService
import org.dataland.datalandqaservice.utils.MockDatasetReviewEntityForTest
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

class DatasetJudgementServiceTest {
    private val datasetJudgementRepository = mock<DatasetJudgementRepository>()
    private val datasetJudgementSupportService = mock<DatasetJudgementSupportService>()
    private val keycloakUserService = mock<KeycloakUserService>()

    private val creationServiceClass =
        DatasetJudgementCreationService(
            datasetJudgementSupportService,
            keycloakUserService,
        )

    private val service =
        DatasetJudgementService(
            datasetJudgementRepository,
            datasetJudgementSupportService,
            creationServiceClass,
        )

    private val mockDatasetReviewEntityForTest = MockDatasetReviewEntityForTest
    private val datasetReviewEntity = mockDatasetReviewEntityForTest.createDummyDatasetReviewEntity()
    private val dummyMetaData = mockDatasetReviewEntityForTest.createDummyMetaData()

    @BeforeEach
    fun setup() {
        reset(
            datasetJudgementRepository,
            datasetJudgementSupportService,
        )

        AuthenticationMock.mockSecurityContext(
            "data.admin@example.com",
            mockDatasetReviewEntityForTest.dummyUserId.toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        doReturn(datasetReviewEntity)
            .whenever(datasetJudgementSupportService)
            .getDatasetJudgementEntityById(any())

        whenever(datasetJudgementRepository.save(any<DatasetJudgementEntity>()))
            .thenAnswer { it.arguments[0] as DatasetJudgementEntity }
    }

    private fun captureSavedReview(): DatasetJudgementEntity {
        val captor = argumentCaptor<DatasetJudgementEntity>()
        verify(datasetJudgementRepository).save(captor.capture())
        return captor.firstValue
    }

    private fun patchAndGetDataPoint(
        source: AcceptedDataPointSource?,
        reporterUserId: String? = null,
        customValue: String? = null,
        dataPointType: String = mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE,
    ): DataPointJudgementEntity {
        service.patchJudgementDetails(
            UUID.randomUUID(),
            dataPointType,
            JudgementDetailsPatch(source, reporterUserId, customValue),
        )

        return captureSavedReview()
            .dataPoints
            .first { it.dataPointType == dataPointType }
    }

    private fun assertMatches(
        entity: DatasetJudgementEntity,
        response: DatasetJudgementResponse,
    ) {
        assertEquals(entity.dataSetJudgementId.toString(), response.dataSetReviewId)
        assertEquals(entity.datasetId.toString(), response.datasetId)
        assertEquals(entity.companyId.toString(), response.companyId)
        assertEquals(mockDatasetReviewEntityForTest.dummyUserId.toString(), response.qaJudgeUserId)
        assertEquals(mockDatasetReviewEntityForTest.DUMMY_USER_NAME, response.qaJudgeUserName)
    }

    @Test
    fun `getDatasetReviewsByDatasetId returns expected responses`() {
        doReturn(listOf(datasetReviewEntity))
            .whenever(datasetJudgementRepository)
            .findAllByDatasetId(any())

        val result = service.getDatasetJudgementsByDatasetId(mockDatasetReviewEntityForTest.dummyDatasetId)

        assertEquals(1, result.size)
        assertMatches(datasetReviewEntity, result.first())
    }

    @Test
    fun `getDatasetReviewsByDatasetId returns empty list when none exist`() {
        doReturn(emptyList<DatasetJudgementEntity>())
            .whenever(datasetJudgementRepository)
            .findAllByDatasetId(any())

        val result = service.getDatasetJudgementsByDatasetId(UUID.randomUUID())

        assertEquals(0, result.size)
    }

    @Test
    fun `getDatasetReviewById returns expected response`() {
        val result = service.getDatasetJudgementById(mockDatasetReviewEntityForTest.dummyDatasetId)
        assertMatches(datasetReviewEntity, result)
    }

    @Test
    fun `getDatasetReviewById throws ResourceNotFoundApiException if review object does not exist`() {
        doReturn(null)
            .whenever(datasetJudgementSupportService)
            .getDatasetJudgementEntityById(any())

        assertThrows<ResourceNotFoundApiException> {
            service.getDatasetJudgementById(mockDatasetReviewEntityForTest.dummyDatasetId)
        }
    }

    @Test
    fun `setReviewer sets reviewer user id and name`() {
        service.setJudge(UUID.randomUUID())

        val saved = captureSavedReview()
        assertEquals(mockDatasetReviewEntityForTest.dummyUserId, saved.qaJudgeUserId)
        assertEquals(mockDatasetReviewEntityForTest.dummyUserId.toString(), saved.qaJudgeUserName)
    }

    @Test
    fun `setReviewer throws ResourceNotFound when datasetReview does not exist`() {
        doReturn(null)
            .whenever(datasetJudgementSupportService)
            .getDatasetJudgementEntityById(any())

        assertThrows<ResourceNotFoundApiException> {
            service.setJudge(UUID.randomUUID())
        }
    }

    @Test
    fun `setReviewState updates status when user is reviewer`() {
        val newState = DatasetJudgementState.Aborted

        service.setJudgementState(UUID.randomUUID(), newState)

        val saved = captureSavedReview()
        assertEquals(newState, saved.reviewState)
    }

    @Test
    fun `setReviewState throws InsufficientRights when current user is not reviewer`() {
        AuthenticationMock.mockSecurityContext(
            "other@example.com",
            UUID.randomUUID().toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        assertThrows<InsufficientRightsApiException> {
            service.setJudgementState(UUID.randomUUID(), DatasetJudgementState.Pending)
        }
    }

    @Test
    fun `postDatasetReview builds entity with dataPoints and qaReporterCompanies correctly`() {
        stubsForPostDatasetReview()
        val result = service.postDatasetJudgement(UUID.randomUUID())

        assertEquals(mockDatasetReviewEntityForTest.dummyCompanyId.toString(), result.companyId)
        assertEquals(1, result.qaReporters.size)
        assertEquals(mockDatasetReviewEntityForTest.DUMMY_USER_NAME, result.qaReporters[0].reporterUserName)
        assertEquals(1, result.dataPoints.size)
        assertEquals(
            mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE,
            result.dataPoints[mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE]
                ?.dataPointType,
        )
        assertEquals(1, result.dataPoints[mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE]?.qaReports?.size)
    }

    private fun stubsForPostDatasetReview() {
        doReturn(mapOf(mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE to mockDatasetReviewEntityForTest.dummyDatapointId))
            .whenever(datasetJudgementSupportService)
            .getContainedDataPoints(any())

        val qaReportEntity =
            DataPointQaReportEntity(
                qaReportId = mockDatasetReviewEntityForTest.qaReportId,
                dataPointId = mockDatasetReviewEntityForTest.dummyDatapointId,
                comment = "",
                verdict = QaReportDataPointVerdict.QaAccepted,
                correctedData = null,
                dataPointType = mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE,
                reporterUserId = mockDatasetReviewEntityForTest.dummyUserId.toString(),
                uploadTime = 1000L,
                active = true,
            )

        doReturn(listOf(qaReportEntity))
            .whenever(datasetJudgementSupportService)
            .findQaReports(any())

        doReturn(dummyMetaData)
            .whenever(datasetJudgementSupportService)
            .getDataMetaInfo(any())

        doReturn(
            KeycloakUserInfo(
                mockDatasetReviewEntityForTest.DUMMY_USER_EMAIL,
                mockDatasetReviewEntityForTest.dummyUserId.toString(),
                mockDatasetReviewEntityForTest.DUMMY_USER_FIRST_NAME,
                mockDatasetReviewEntityForTest.DUMMY_USER_LAST_NAME,
            ),
        ).whenever(keycloakUserService)
            .getUser(any())
    }

    @Test
    fun `postDatasetReview throws ConflictApiException when dataset review status is pending`() {
        val dummyDatapointId = UUID.randomUUID().toString()

        doReturn(mapOf(mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE to dummyDatapointId))
            .whenever(datasetJudgementSupportService)
            .getContainedDataPoints(any())

        doReturn(listOf(datasetReviewEntity))
            .whenever(datasetJudgementRepository)
            .findAllByDatasetIdAndJudgementState(any(), any())

        assertThrows<ConflictApiException> {
            service.postDatasetJudgement(UUID.randomUUID())
        }
    }

    @Test
    fun `patchReviewDetails with Original sets acceptedSource and clears AcceptedQaReport source`() {
        val saved = patchAndGetDataPoint(AcceptedDataPointSource.Original)

        assertEquals(AcceptedDataPointSource.Original, saved.acceptedSource)
        assertNull(saved.reporterUserIdOfAcceptedQaReport)
    }

    @Test
    fun `patchReviewDetails with Qa sets acceptedSource and AcceptedQaReport source`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Qa,
                reporterUserId = mockDatasetReviewEntityForTest.dummyUserId.toString(),
            )

        assertEquals(AcceptedDataPointSource.Qa, saved.acceptedSource)
        assertEquals(mockDatasetReviewEntityForTest.dummyUserId, saved.reporterUserIdOfAcceptedQaReport)
    }

    @Test
    fun `patchReviewDetails with Custom sets acceptedSource and clears AcceptedQaReport source`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Custom,
                customValue = mockDatasetReviewEntityForTest.CUSTOM_VALUE,
            )

        assertEquals(AcceptedDataPointSource.Custom, saved.acceptedSource)
        assertNull(saved.reporterUserIdOfAcceptedQaReport)
    }

    @Test
    fun `patchReviewDetails throws ConflictApiException when acceptedSource is custom and no customValue is provided`() {
        assertThrows<ConflictApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE,
                JudgementDetailsPatch(AcceptedDataPointSource.Custom, null, null),
            )
        }
    }

    @Test
    fun `patchReviewDetails with only customValue validates and sets customValue`() {
        val saved =
            patchAndGetDataPoint(
                source = null,
                customValue = mockDatasetReviewEntityForTest.CUSTOM_VALUE,
            )

        assertEquals(mockDatasetReviewEntityForTest.CUSTOM_VALUE, saved.customValue)
        verify(datasetJudgementSupportService)
            .validateCustomDataPoint(mockDatasetReviewEntityForTest.CUSTOM_VALUE, mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE)
    }

    @Test
    fun `patchReviewDetails with Qa without reporterUserIdOfAcceptedQaReport throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE,
                JudgementDetailsPatch(AcceptedDataPointSource.Qa, null, null),
            )
        }
    }

    @Test
    fun `patchReviewDetails throws InvalidInputApiException when dataPointType not in review`() {
        assertThrows<InvalidInputApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                "unknown-type",
                JudgementDetailsPatch(AcceptedDataPointSource.Original, null, null),
            )
        }
    }

    @Test
    fun `patchReviewDetails throws InsufficientRights when user is not reviewer`() {
        AuthenticationMock.mockSecurityContext(
            "other@example.com",
            UUID.randomUUID().toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        assertThrows<InsufficientRightsApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE,
                JudgementDetailsPatch(AcceptedDataPointSource.Original, null, null),
            )
        }
    }

    @Test
    fun `patchReviewDetails with Custom wraps BackendClientException into InvalidInputApiException`() {
        whenever(datasetJudgementSupportService.validateCustomDataPoint(any(), any()))
            .thenThrow(BackendClientException())

        assertThrows<InvalidInputApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                mockDatasetReviewEntityForTest.DUMMY_DATA_POINT_TYPE,
                JudgementDetailsPatch(null, null, """{"value": 1}"""),
            )
        }
    }
}
