package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointReviewDetailsEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewSupportService
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
import java.util.Optional
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

class DatasetReviewServiceTest {
    private val mockDatasetReviewRepository = mock<DatasetReviewRepository>()
    private val mockDatasetReviewSupportService = mock<DatasetReviewSupportService>()
    private val mockInheritedRolesControllerApi = mock<InheritedRolesControllerApi>()
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()

    private val datasetReviewService =
        DatasetReviewService(
            mockDatasetReviewRepository,
            mockDatasetReviewSupportService,
            mockInheritedRolesControllerApi,
            mockCompanyDataControllerApi,
        )

    private val dummyUserId = UUID.randomUUID()
    private val dummyDataPointType = "dummy-datapoint-type"
    private val dummyCompanyId = UUID.randomUUID()
    private val dummyDatasetId = UUID.randomUUID()

    private val dummyDataPointReviewDetails =
        DataPointReviewDetailsEntity(
            dataPointType = dummyDataPointType,
            dataPointId = UUID.randomUUID(),
            acceptedSource = null,
            companyIdOfAcceptedQaReport = null,
            customValue = null,
            datasetReview = null,
        )

    private val datasetReviewEntity =
        DatasetReviewEntity(
            dataSetReviewId = UUID.randomUUID(),
            datasetId = dummyDatasetId,
            companyId = dummyCompanyId,
            dataType = "sfdr",
            reportingPeriod = "2026",
            reviewerUserId = dummyUserId,
            reviewerUserName = "Dummy User",
            qaReporterCompanies = mutableListOf(),
            dataPoints = mutableListOf(dummyDataPointReviewDetails),
        )

    private val dummyMetaData =
        DataMetaInformation(
            dataId = UUID.randomUUID().toString(),
            companyId = dummyCompanyId.toString(),
            dataType = DataTypeEnum.sfdr,
            uploadTime = 0L,
            reportingPeriod = "2026",
            currentlyActive = true,
            qaStatus = QaStatus.Pending,
        )

    @BeforeEach
    fun setup() {
        reset(
            mockDatasetReviewRepository,
            mockDatasetReviewSupportService,
            mockInheritedRolesControllerApi,
            mockCompanyDataControllerApi,
        )

        AuthenticationMock.mockSecurityContext(
            "data.admin@example.com",
            dummyUserId.toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        doReturn(Optional.of(datasetReviewEntity))
            .whenever(mockDatasetReviewRepository)
            .findById(any())

        whenever(mockDatasetReviewRepository.save(any<DatasetReviewEntity>()))
            .thenAnswer { it.arguments[0] as DatasetReviewEntity }
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
        assertEquals(dummyUserId.toString(), response.qaJudgeUserId)
        assertEquals("Dummy User", response.qaJudgeUserName)
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
    fun `postDatasetReview builds entity with dataPoints and qaReporterCompanies correctly`() {
        val dummyDatapointId = UUID.randomUUID().toString()
        val reporterCompanyId = UUID.randomUUID().toString()
        val reporterCompanyName = "Reporter Company"
        val qaReportId = UUID.randomUUID().toString()

        doReturn(mapOf(dummyDataPointType to dummyDatapointId))
            .whenever(mockDatasetReviewSupportService)
            .getContainedDataPoints(any())

        val qaReportEntity =
            DataPointQaReportEntity(
                qaReportId = qaReportId,
                dataPointId = dummyDatapointId,
                comment = "",
                verdict = QaReportDataPointVerdict.QaAccepted,
                correctedData = null,
                dataPointType = dummyDataPointType,
                reporterUserId = dummyUserId.toString(),
                uploadTime = 1000L,
                active = true,
            )

        doReturn(listOf(qaReportEntity))
            .whenever(mockDatasetReviewSupportService)
            .findQaReportsWithDetails(any())

        doReturn(dummyMetaData)
            .whenever(mockDatasetReviewSupportService)
            .getDataMetaInfo(any())

        doReturn(mapOf(reporterCompanyId to listOf("Role")))
            .whenever(mockInheritedRolesControllerApi)
            .getInheritedRoles(any())

        whenever(mockCompanyDataControllerApi.postCompanyValidation(any())).thenReturn(
            listOf(
                CompanyIdentifierValidationResult(
                    identifier = reporterCompanyId,
                    companyInformation =
                        BasicCompanyInformation(
                            companyId = reporterCompanyId,
                            companyName = reporterCompanyName,
                            headquarters = "Berlin",
                            countryCode = "DE",
                        ),
                ),
            ),
        )

        val result = datasetReviewService.postDatasetReview(UUID.randomUUID())

        assertEquals(dummyCompanyId.toString(), result.companyId)
        assertEquals(1, result.qaReporterCompanies.size)
        assertEquals(reporterCompanyName, result.qaReporterCompanies[0].reportCompanyName)
        assertEquals(1, result.dataPoints.size)
        assertEquals(dummyDataPointType, result.dataPoints[dummyDataPointType]?.dataPointType)
        assertEquals(1, result.dataPoints[dummyDataPointType]?.qaReports?.size)
    }

    @Test
    fun `setReviewer sets reviewer user id and name`() {
        datasetReviewService.setReviewer(UUID.randomUUID())

        val captor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(captor.capture())
        assertEquals(dummyUserId, captor.firstValue.reviewerUserId)
        assertEquals(dummyUserId.toString(), captor.firstValue.reviewerUserName)
    }

    @Test
    fun `setReviewState updates status when user is reviewer`() {
        val newState = DatasetReviewState.Aborted

        datasetReviewService.setReviewState(UUID.randomUUID(), newState)

        val captor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(captor.capture())
        assertEquals(newState, captor.firstValue.reviewState)
    }

    @Test
    fun `setReviewState throws InsufficientRights when current user is not reviewer`() {
        AuthenticationMock.mockSecurityContext(
            "other@example.com",
            UUID.randomUUID().toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        assertThrows<InsufficientRightsApiException> {
            datasetReviewService.setReviewState(UUID.randomUUID(), DatasetReviewState.Pending)
        }
    }

    @Test
    fun `setAcceptedSource with Original sets acceptedSource and clears companyIdOfAcceptedQaReport`() {
        datasetReviewService.setAcceptedSource(UUID.randomUUID(), dummyDataPointType, AcceptedDataPointSource.Original, null, null)

        val captor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(captor.capture())
        val saved = captor.firstValue.dataPoints.first { it.dataPointType == dummyDataPointType }
        assertEquals(AcceptedDataPointSource.Original, saved.acceptedSource)
        assertNull(saved.companyIdOfAcceptedQaReport)
    }

    @Test
    fun `setAcceptedSource with Qa sets acceptedSource and companyIdOfAcceptedQaReport`() {
        val reporterCompanyId = UUID.randomUUID()

        datasetReviewService.setAcceptedSource(
            UUID.randomUUID(),
            dummyDataPointType,
            AcceptedDataPointSource.Qa,
            reporterCompanyId.toString(),
            null,
        )

        val captor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(captor.capture())
        val saved = captor.firstValue.dataPoints.first { it.dataPointType == dummyDataPointType }
        assertEquals(AcceptedDataPointSource.Qa, saved.acceptedSource)
        assertEquals(reporterCompanyId, saved.companyIdOfAcceptedQaReport)
    }

    @Test
    fun `setAcceptedSource with Custom validates and sets customValue`() {
        val customValue = """{"value": 42}"""

        datasetReviewService.setAcceptedSource(
            UUID.randomUUID(),
            dummyDataPointType,
            AcceptedDataPointSource.Custom,
            null,
            customValue,
        )

        val captor = argumentCaptor<DatasetReviewEntity>()
        verify(mockDatasetReviewRepository).save(captor.capture())
        val saved = captor.firstValue.dataPoints.first { it.dataPointType == dummyDataPointType }
        assertEquals(AcceptedDataPointSource.Custom, saved.acceptedSource)
        assertEquals(customValue, saved.customValue)
        assertNull(saved.companyIdOfAcceptedQaReport)
        verify(mockDatasetReviewSupportService).validateCustomDataPoint(customValue, dummyDataPointType)
    }

    @Test
    fun `setAcceptedSource with Qa without companyIdOfAcceptedQaReport throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            datasetReviewService.setAcceptedSource(UUID.randomUUID(), dummyDataPointType, AcceptedDataPointSource.Qa, null, null)
        }
    }

    @Test
    fun `setAcceptedSource with Custom without customValue throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            datasetReviewService.setAcceptedSource(UUID.randomUUID(), dummyDataPointType, AcceptedDataPointSource.Custom, null, null)
        }
    }

    @Test
    fun `setAcceptedSource throws ResourceNotFound when dataPointType not in review`() {
        assertThrows<ResourceNotFoundApiException> {
            datasetReviewService.setAcceptedSource(UUID.randomUUID(), "unknown-type", AcceptedDataPointSource.Original, null, null)
        }
    }

    @Test
    fun `setAcceptedSource throws InsufficientRights when user is not reviewer`() {
        AuthenticationMock.mockSecurityContext(
            "other@example.com",
            UUID.randomUUID().toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        assertThrows<InsufficientRightsApiException> {
            datasetReviewService.setAcceptedSource(UUID.randomUUID(), dummyDataPointType, AcceptedDataPointSource.Original, null, null)
        }
    }

    @Test
    fun `setAcceptedSource with Custom wraps BackendClientException into InvalidInputApiException`() {
        whenever(mockDatasetReviewSupportService.validateCustomDataPoint(any(), any()))
            .thenThrow(BackendClientException())

        assertThrows<InvalidInputApiException> {
            datasetReviewService.setAcceptedSource(
                UUID.randomUUID(),
                dummyDataPointType,
                AcceptedDataPointSource.Custom,
                null,
                """{"value": 1}""",
            )
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
}
