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
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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

    private val mockDatasetJudgementEntityForTest = MockDatasetJudgementEntityForTest
    private val datasetJudgementEntity = mockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
    private val dummyMetaData = mockDatasetJudgementEntityForTest.createDummyMetaData()

    @BeforeEach
    fun setup() {
        reset(
            datasetJudgementRepository,
            datasetJudgementSupportService,
        )

        AuthenticationMock.mockSecurityContext(
            "data.admin@example.com",
            mockDatasetJudgementEntityForTest.dummyUserId.toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        doReturn(datasetJudgementEntity)
            .whenever(datasetJudgementSupportService)
            .getDatasetJudgementEntityById(any())

        whenever(datasetJudgementRepository.save(any<DatasetJudgementEntity>()))
            .thenAnswer { it.arguments[0] as DatasetJudgementEntity }
    }

    private fun captureSavedJudgement(): DatasetJudgementEntity {
        val captor = argumentCaptor<DatasetJudgementEntity>()
        verify(datasetJudgementRepository).save(captor.capture())
        return captor.firstValue
    }

    private fun patchAndGetDataPoint(
        source: AcceptedDataPointSource?,
        reporterUserId: String? = null,
        customValue: String? = null,
        dataPointType: String = mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
    ): DataPointJudgementEntity {
        service.patchJudgementDetails(
            UUID.randomUUID(),
            dataPointType,
            JudgementDetailsPatch(source, reporterUserId, customValue),
        )

        return captureSavedJudgement()
            .dataPoints
            .first { it.dataPointType == dataPointType }
    }

    private fun assertMatches(
        entity: DatasetJudgementEntity,
        response: DatasetJudgementResponse,
    ) {
        assertEquals(entity.dataSetJudgementId.toString(), response.dataSetJudgementId)
        assertEquals(entity.datasetId.toString(), response.datasetId)
        assertEquals(entity.companyId.toString(), response.companyId)
        assertEquals(mockDatasetJudgementEntityForTest.dummyUserId.toString(), response.qaJudgeUserId)
        assertEquals(mockDatasetJudgementEntityForTest.DUMMY_USER_NAME, response.qaJudgeUserName)
    }

    @Test
    fun `getDatasetJudgementsByDatasetId returns expected responses`() {
        doReturn(listOf(datasetJudgementEntity))
            .whenever(datasetJudgementRepository)
            .findAllByDatasetId(any())

        val result = service.getDatasetJudgementsByDatasetId(mockDatasetJudgementEntityForTest.dummyDatasetId)

        assertEquals(1, result.size)
        assertMatches(datasetJudgementEntity, result.first())
    }

    @Test
    fun `getDatasetJudgementsByDatasetId returns empty list when none exist`() {
        doReturn(emptyList<DatasetJudgementEntity>())
            .whenever(datasetJudgementRepository)
            .findAllByDatasetId(any())

        val result = service.getDatasetJudgementsByDatasetId(UUID.randomUUID())

        assertEquals(0, result.size)
    }

    @Test
    fun `getDatasetJudgementById returns expected response`() {
        val result = service.getDatasetJudgementById(mockDatasetJudgementEntityForTest.dummyDatasetId)
        assertMatches(datasetJudgementEntity, result)
    }

    @Test
    fun `getDatasetJudgementById throws ResourceNotFoundApiException if judgement object does not exist`() {
        doReturn(null)
            .whenever(datasetJudgementSupportService)
            .getDatasetJudgementEntityById(any())

        assertThrows<ResourceNotFoundApiException> {
            service.getDatasetJudgementById(mockDatasetJudgementEntityForTest.dummyDatasetId)
        }
    }

    @Test
    fun `setJudge sets judge user id and name`() {
        service.setJudge(UUID.randomUUID())

        val saved = captureSavedJudgement()
        assertEquals(mockDatasetJudgementEntityForTest.dummyUserId, saved.qaJudgeUserId)
        assertEquals(mockDatasetJudgementEntityForTest.dummyUserId.toString(), saved.qaJudgeUserName)
    }

    @Test
    fun `setJudge throws ResourceNotFound when datasetJudgement does not exist`() {
        doReturn(null)
            .whenever(datasetJudgementSupportService)
            .getDatasetJudgementEntityById(any())

        assertThrows<ResourceNotFoundApiException> {
            service.setJudge(UUID.randomUUID())
        }
    }

    @Test
    fun `setJudgementState updates status when user is judge`() {
        val newState = DatasetJudgementState.Aborted

        service.setJudgementState(UUID.randomUUID(), newState)

        val saved = captureSavedJudgement()
        assertEquals(newState, saved.judgementState)
    }

    @Test
    fun `setJudgementState throws InsufficientRights when current user is not judge`() {
        AuthenticationMock.mockSecurityContext(
            "other@example.com",
            UUID.randomUUID().toString(),
            setOf(DatalandRealmRole.ROLE_UPLOADER),
        )

        assertThrows<InsufficientRightsApiException> {
            service.setJudgementState(UUID.randomUUID(), DatasetJudgementState.Pending)
        }
    }

    @Test
    fun `postDatasetJudgement builds entity with dataPoints and qaReporterCompanies correctly`() {
        stubsForPostDatasetJudgement()
        val result = service.postDatasetJudgement(UUID.randomUUID())

        assertEquals(mockDatasetJudgementEntityForTest.dummyCompanyId.toString(), result.companyId)
        assertEquals(1, result.qaReporters.size)
        assertEquals(mockDatasetJudgementEntityForTest.DUMMY_USER_NAME, result.qaReporters[0].reporterUserName)
        assertEquals(1, result.dataPoints.size)
        assertEquals(
            mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
            result.dataPoints[mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE]
                ?.dataPointType,
        )
        assertEquals(1, result.dataPoints[mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE]?.qaReports?.size)
    }

    private fun stubsForPostDatasetJudgement() {
        doReturn(mapOf(mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE to mockDatasetJudgementEntityForTest.dummyDatapointId))
            .whenever(datasetJudgementSupportService)
            .getContainedDataPoints(any())

        val qaReportEntity =
            DataPointQaReportEntity(
                qaReportId = mockDatasetJudgementEntityForTest.qaReportId,
                dataPointId = mockDatasetJudgementEntityForTest.dummyDatapointId,
                comment = "",
                verdict = QaReportDataPointVerdict.QaAccepted,
                correctedData = null,
                dataPointType = mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
                reporterUserId = mockDatasetJudgementEntityForTest.dummyUserId.toString(),
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
                mockDatasetJudgementEntityForTest.DUMMY_USER_EMAIL,
                mockDatasetJudgementEntityForTest.dummyUserId.toString(),
                mockDatasetJudgementEntityForTest.DUMMY_USER_FIRST_NAME,
                mockDatasetJudgementEntityForTest.DUMMY_USER_LAST_NAME,
            ),
        ).whenever(keycloakUserService)
            .getUser(any())
    }

    @Test
    fun `postDatasetJudgement throws ConflictApiException when dataset judgement status is pending`() {
        val dummyDatapointId = UUID.randomUUID().toString()

        doReturn(mapOf(mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE to dummyDatapointId))
            .whenever(datasetJudgementSupportService)
            .getContainedDataPoints(any())

        doReturn(listOf(datasetJudgementEntity))
            .whenever(datasetJudgementRepository)
            .findAllByDatasetIdAndJudgementState(any(), any())

        assertThrows<ConflictApiException> {
            service.postDatasetJudgement(UUID.randomUUID())
        }
    }

    @Test
    fun `patchJudgementDetails with Original sets acceptedSource and clears AcceptedQaReport source`() {
        val saved = patchAndGetDataPoint(AcceptedDataPointSource.Original)

        assertEquals(AcceptedDataPointSource.Original, saved.acceptedSource)
        assertNull(saved.reporterUserIdOfAcceptedQaReport)
    }

    @Test
    fun `patchJudgementDetails with Qa sets acceptedSource and AcceptedQaReport source`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Qa,
                reporterUserId = mockDatasetJudgementEntityForTest.dummyUserId.toString(),
            )

        assertEquals(AcceptedDataPointSource.Qa, saved.acceptedSource)
        assertEquals(mockDatasetJudgementEntityForTest.dummyUserId, saved.reporterUserIdOfAcceptedQaReport)
    }

    @Test
    fun `patchJudgementDetails with Custom sets acceptedSource and clears AcceptedQaReport source`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Custom,
                customValue = mockDatasetJudgementEntityForTest.CUSTOM_VALUE,
            )

        assertEquals(AcceptedDataPointSource.Custom, saved.acceptedSource)
        assertNull(saved.reporterUserIdOfAcceptedQaReport)
    }

    @Test
    fun `patchJudgementDetails throws ConflictApiException when acceptedSource is custom and no customValue is provided`() {
        assertThrows<ConflictApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
                JudgementDetailsPatch(AcceptedDataPointSource.Custom, null, null),
            )
        }
    }

    @Test
    fun `patchJudgementDetails with only customValue validates and sets customValue`() {
        val saved =
            patchAndGetDataPoint(
                source = null,
                customValue = mockDatasetJudgementEntityForTest.CUSTOM_VALUE,
            )

        assertEquals(mockDatasetJudgementEntityForTest.CUSTOM_VALUE, saved.customValue)
        verify(datasetJudgementSupportService)
            .validateCustomDataPoint(
                mockDatasetJudgementEntityForTest.CUSTOM_VALUE,
                mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
            )
    }

    @Test
    fun `patchJudgementDetails with Qa without reporterUserIdOfAcceptedQaReport throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
                JudgementDetailsPatch(
                    AcceptedDataPointSource.Qa,
                    null,
                    null,
                ),
            )
        }
    }

    @Test
    fun `patchJudgementDetails throws InvalidInputApiException when dataPointType not in judgement`() {
        assertThrows<InvalidInputApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                "unknown-type",
                JudgementDetailsPatch(AcceptedDataPointSource.Original, null, null),
            )
        }
    }

    @Test
    fun `patchJudgementDetails throws InsufficientRights when user is not judge`() {
        AuthenticationMock.mockSecurityContext(
            "other@example.com",
            UUID.randomUUID().toString(),
            setOf(DatalandRealmRole.ROLE_UPLOADER),
        )

        assertThrows<InsufficientRightsApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
                JudgementDetailsPatch(AcceptedDataPointSource.Original, null, null),
            )
        }
    }

    @Test
    fun `patchJudgementDetails with Custom wraps BackendClientException into InvalidInputApiException`() {
        whenever(datasetJudgementSupportService.validateCustomDataPoint(any(), any()))
            .thenThrow(BackendClientException())

        assertThrows<InvalidInputApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
                JudgementDetailsPatch(null, null, """{"value": 1}"""),
            )
        }.also { exception ->
            assertTrue(exception.cause is BackendClientException)
        }
    }
}
