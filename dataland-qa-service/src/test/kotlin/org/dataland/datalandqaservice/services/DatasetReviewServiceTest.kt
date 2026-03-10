package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointReviewDetailsEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.ReviewDetailsPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewSupportService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DatasetReviewCreationUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DatasetReviewHelper
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
import java.util.Optional
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

class DatasetReviewServiceTest {
    private val datasetReviewRepository = mock<DatasetReviewRepository>()
    private val companyDataControllerApi = mock<CompanyDataControllerApi>()
    private val datasetReviewSupportService = mock<DatasetReviewSupportService>()
    private val inheritedRolesControllerApi = mock<InheritedRolesControllerApi>()
    private val keycloakUserService = mock<KeycloakUserService>()

    private val creationUtils =
        DatasetReviewCreationUtils(
            inheritedRolesControllerApi,
            companyDataControllerApi,
            datasetReviewSupportService,
            keycloakUserService,
        )

    private val helper =
        DatasetReviewHelper(
            datasetReviewRepository,
            datasetReviewSupportService,
        )

    private val service =
        DatasetReviewService(
            datasetReviewRepository,
            datasetReviewSupportService,
            creationUtils,
            helper,
        )

    private val mockDatasetReviewEntityForTest = MockDatasetReviewEntityForTest
    private val datasetReviewEntity = mockDatasetReviewEntityForTest.createDummyDatasetReviewEntity()
    private val dummyMetaData = mockDatasetReviewEntityForTest.createDummyMetaData()

    @BeforeEach
    fun setup() {
        reset(
            datasetReviewRepository,
            datasetReviewSupportService,
            inheritedRolesControllerApi,
            companyDataControllerApi,
        )

        AuthenticationMock.mockSecurityContext(
            "data.admin@example.com",
            mockDatasetReviewEntityForTest.dummyUserId.toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        doReturn(Optional.of(datasetReviewEntity))
            .whenever(datasetReviewRepository)
            .findById(any())

        whenever(datasetReviewRepository.save(any<DatasetReviewEntity>()))
            .thenAnswer { it.arguments[0] as DatasetReviewEntity }
    }

    private fun captureSavedReview(): DatasetReviewEntity {
        val captor = argumentCaptor<DatasetReviewEntity>()
        verify(datasetReviewRepository).save(captor.capture())
        return captor.firstValue
    }

    private fun patchAndGetDataPoint(
        source: AcceptedDataPointSource?,
        reporterUserId: String? = null,
        customValue: String? = null,
        dataPointType: String = mockDatasetReviewEntityForTest.dummyDataPointType,
    ): DataPointReviewDetailsEntity {
        service.patchReviewDetails(
            UUID.randomUUID(),
            dataPointType,
            ReviewDetailsPatch(source, reporterUserId, customValue),
        )

        return captureSavedReview()
            .dataPoints
            .first { it.dataPointType == dataPointType }
    }

    private fun assertMatches(
        entity: DatasetReviewEntity,
        response: DatasetReviewResponse,
    ) {
        assertEquals(entity.dataSetReviewId.toString(), response.dataSetReviewId)
        assertEquals(entity.datasetId.toString(), response.datasetId)
        assertEquals(entity.companyId.toString(), response.companyId)
        assertEquals(mockDatasetReviewEntityForTest.dummyUserId.toString(), response.qaJudgeUserId)
        assertEquals(mockDatasetReviewEntityForTest.dummyUserName, response.qaJudgeUserName)
    }

    @Test
    fun `getDatasetReviewsByDatasetId returns expected responses`() {
        doReturn(listOf(datasetReviewEntity))
            .whenever(datasetReviewRepository)
            .findAllByDatasetId(any())

        val result = service.getDatasetReviewsByDatasetId(mockDatasetReviewEntityForTest.dummyDatasetId)

        assertEquals(1, result.size)
        assertMatches(datasetReviewEntity, result.first())
    }

    @Test
    fun `getDatasetReviewsByDatasetId returns empty list when none exist`() {
        doReturn(emptyList<DatasetReviewEntity>())
            .whenever(datasetReviewRepository)
            .findAllByDatasetId(any())

        val result = service.getDatasetReviewsByDatasetId(UUID.randomUUID())

        assertEquals(0, result.size)
    }

    @Test
    fun `getDatasetReviewById returns expected response`() {
        val result = service.getDatasetReviewById(mockDatasetReviewEntityForTest.dummyDatasetId)
        assertMatches(datasetReviewEntity, result)
    }

    @Test
    fun `getDatasetReviewById throws ResourceNotFoundApiException if review object does not exist`() {
        doReturn(Optional.empty<DatasetReviewEntity>())
            .whenever(datasetReviewRepository)
            .findById(any())

        assertThrows<ResourceNotFoundApiException> {
            service.getDatasetReviewById(mockDatasetReviewEntityForTest.dummyDatasetId)
        }
    }

    @Test
    fun `setReviewer sets reviewer user id and name`() {
        service.setReviewer(UUID.randomUUID())

        val saved = captureSavedReview()
        assertEquals(mockDatasetReviewEntityForTest.dummyUserId, saved.reviewerUserId)
        assertEquals(mockDatasetReviewEntityForTest.dummyUserId.toString(), saved.reviewerUserName)
    }

    @Test
    fun `setReviewer throws ResourceNotFound when datasetReview does not exist`() {
        doReturn(Optional.empty<DatasetReviewEntity>())
            .whenever(datasetReviewRepository)
            .findById(any())

        assertThrows<ResourceNotFoundApiException> {
            service.setReviewer(UUID.randomUUID())
        }
    }

    @Test
    fun `setReviewState updates status when user is reviewer`() {
        val newState = DatasetReviewState.Aborted

        service.setReviewState(UUID.randomUUID(), newState)

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
            service.setReviewState(UUID.randomUUID(), DatasetReviewState.Pending)
        }
    }

    @Test
    fun `postDatasetReview builds entity with dataPoints and qaReporterCompanies correctly`() {
        stubsForPostDatasetReview()
        val result = service.postDatasetReview(UUID.randomUUID())

        assertEquals(mockDatasetReviewEntityForTest.dummyCompanyId.toString(), result.companyId)
        assertEquals(1, result.qaReporters.size)
        assertEquals(mockDatasetReviewEntityForTest.reporterCompanyName, result.qaReporters[0].reportCompanyName)
        assertEquals(mockDatasetReviewEntityForTest.dummyUserName, result.qaReporters[0].reporterUserName)
        assertEquals(1, result.dataPoints.size)
        assertEquals(
            mockDatasetReviewEntityForTest.dummyDataPointType,
            result.dataPoints[mockDatasetReviewEntityForTest.dummyDataPointType]
                ?.dataPointType,
        )
        assertEquals(1, result.dataPoints[mockDatasetReviewEntityForTest.dummyDataPointType]?.qaReports?.size)
    }

    private fun stubsForPostDatasetReview() {
        doReturn(mapOf(mockDatasetReviewEntityForTest.dummyDataPointType to mockDatasetReviewEntityForTest.dummyDatapointId))
            .whenever(datasetReviewSupportService)
            .getContainedDataPoints(any())

        val qaReportEntity =
            DataPointQaReportEntity(
                qaReportId = mockDatasetReviewEntityForTest.qaReportId,
                dataPointId = mockDatasetReviewEntityForTest.dummyDatapointId,
                comment = "",
                verdict = QaReportDataPointVerdict.QaAccepted,
                correctedData = null,
                dataPointType = mockDatasetReviewEntityForTest.dummyDataPointType,
                reporterUserId = mockDatasetReviewEntityForTest.dummyUserId.toString(),
                uploadTime = 1000L,
                active = true,
            )

        doReturn(listOf(qaReportEntity))
            .whenever(datasetReviewSupportService)
            .findQaReportsWithDetails(any())

        doReturn(dummyMetaData)
            .whenever(datasetReviewSupportService)
            .getDataMetaInfo(any())

        doReturn(mapOf(mockDatasetReviewEntityForTest.reporterCompanyId to listOf("Role")))
            .whenever(inheritedRolesControllerApi)
            .getInheritedRoles(any())

        whenever(companyDataControllerApi.postCompanyValidation(any()))
            .thenReturn(
                listOf(
                    CompanyIdentifierValidationResult(
                        identifier = mockDatasetReviewEntityForTest.reporterCompanyId,
                        companyInformation =
                            BasicCompanyInformation(
                                companyId = mockDatasetReviewEntityForTest.reporterCompanyId,
                                companyName = mockDatasetReviewEntityForTest.reporterCompanyName,
                                headquarters = "Berlin",
                                countryCode = "DE",
                            ),
                    ),
                ),
            )

        doReturn(
            KeycloakUserInfo(
                mockDatasetReviewEntityForTest.dummyUserEmail,
                mockDatasetReviewEntityForTest.dummyUserId.toString(),
                mockDatasetReviewEntityForTest.dummyUserFirstName,
                mockDatasetReviewEntityForTest.dummyUserLastName,
            ),
        ).whenever(keycloakUserService)
            .getUser(any())
    }

    @Test
    fun `postDatasetReview throws ConflictApiException when dataset review status is pending`() {
        val dummyDatapointId = UUID.randomUUID().toString()

        doReturn(mapOf(mockDatasetReviewEntityForTest.dummyDataPointType to dummyDatapointId))
            .whenever(datasetReviewSupportService)
            .getContainedDataPoints(any())

        doReturn(listOf(datasetReviewEntity))
            .whenever(datasetReviewRepository)
            .findAllByDatasetIdAndReviewState(any(), any())

        assertThrows<ConflictApiException> {
            service.postDatasetReview(UUID.randomUUID())
        }
    }

    @Test
    fun `patchReviewDetails with Original sets acceptedSource and clears AcceptedQaReport source`() {
        val saved = patchAndGetDataPoint(AcceptedDataPointSource.Original)

        assertEquals(AcceptedDataPointSource.Original, saved.acceptedSource)
        assertNull(saved.companyIdOfAcceptedQaReport)
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
        assertEquals(mockDatasetReviewEntityForTest.dummyReporterCompanyId, saved.companyIdOfAcceptedQaReport)
    }

    @Test
    fun `patchReviewDetails with Custom sets acceptedSource and clears AcceptedQaReport source`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Custom,
                customValue = mockDatasetReviewEntityForTest.customValue,
            )

        assertEquals(AcceptedDataPointSource.Custom, saved.acceptedSource)
        assertNull(saved.companyIdOfAcceptedQaReport)
        assertNull(saved.reporterUserIdOfAcceptedQaReport)
    }

    @Test
    fun `patchReviewDetails throws ConflictApiException when acceptedSource is custom and no customValue is provided`() {
        assertThrows<ConflictApiException> {
            service.patchReviewDetails(
                UUID.randomUUID(),
                mockDatasetReviewEntityForTest.dummyDataPointType,
                ReviewDetailsPatch(AcceptedDataPointSource.Custom, null, null),
            )
        }
    }

    @Test
    fun `patchReviewDetails with only customValue validates and sets customValue`() {
        val saved =
            patchAndGetDataPoint(
                source = null,
                customValue = mockDatasetReviewEntityForTest.customValue,
            )

        assertEquals(mockDatasetReviewEntityForTest.customValue, saved.customValue)
        verify(datasetReviewSupportService)
            .validateCustomDataPoint(mockDatasetReviewEntityForTest.customValue, mockDatasetReviewEntityForTest.dummyDataPointType)
    }

    @Test
    fun `patchReviewDetails with Qa without reporterUserIdOfAcceptedQaReport throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            service.patchReviewDetails(
                UUID.randomUUID(),
                mockDatasetReviewEntityForTest.dummyDataPointType,
                ReviewDetailsPatch(AcceptedDataPointSource.Qa, null, null),
            )
        }
    }

    @Test
    fun `patchReviewDetails throws ResourceNotFound when dataPointType not in review`() {
        assertThrows<ResourceNotFoundApiException> {
            service.patchReviewDetails(
                UUID.randomUUID(),
                "unknown-type",
                ReviewDetailsPatch(AcceptedDataPointSource.Original, null, null),
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
            service.patchReviewDetails(
                UUID.randomUUID(),
                mockDatasetReviewEntityForTest.dummyDataPointType,
                ReviewDetailsPatch(AcceptedDataPointSource.Original, null, null),
            )
        }
    }

    @Test
    fun `patchReviewDetails with Custom wraps BackendClientException into InvalidInputApiException`() {
        whenever(datasetReviewSupportService.validateCustomDataPoint(any(), any()))
            .thenThrow(BackendClientException())

        assertThrows<InvalidInputApiException> {
            service.patchReviewDetails(
                UUID.randomUUID(),
                mockDatasetReviewEntityForTest.dummyDataPointType,
                ReviewDetailsPatch(null, null, """{"value": 1}"""),
            )
        }
    }
}
