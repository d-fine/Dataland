package org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.UUID

class DatasetJudgementServiceTest : DatasetJudgementServiceTestBase() {
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
    fun `getDatasetJudgementById includes reasonForCustomDataPoint when set and null when not set`() {
        val judgementIdWithReasonForCustomDataPoint = UUID.randomUUID()
        doReturn(mockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntityWithCustomSource())
            .whenever(datasetJudgementSupportService)
            .getDatasetJudgementEntityById(judgementIdWithReasonForCustomDataPoint)

        listOf(
            Pair(UUID.randomUUID(), null),
            Pair(judgementIdWithReasonForCustomDataPoint, mockDatasetJudgementEntityForTest.REASON_FOR_CUSTOM_DATAPOINT),
        ).forEach { (judgementId, expectedReason) ->
            assertEquals(
                expectedReason,
                service
                    .getDatasetJudgementById(judgementId)
                    .dataPoints[mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE]
                    ?.reasonForCustomDataPoint,
            )
        }
    }

    @Test
    fun `setJudge sets judge user id and name`() {
        service.setJudge(UUID.randomUUID())
        val saved = captureSavedJudgement()
        assertEquals(mockDatasetJudgementEntityForTest.dummyUserId, saved.qaJudgeUserId)
        assertEquals("Dummy User", saved.qaJudgeUserName)
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
        datasetJudgementEntity.dataPoints.first().acceptedSource = AcceptedDataPointSource.Original
        service.setJudgementState(UUID.randomUUID(), DatasetJudgementState.FinishedWithDatasetAcceptance)
        val saved = captureSavedJudgement()
        assertEquals(DatasetJudgementState.FinishedWithDatasetAcceptance, saved.judgementState)
    }

    @Test
    fun `setJudgementState throws InsufficientRights when current user is not judge`() {
        AuthenticationMock.mockSecurityContext(
            "other@example.com",
            UUID.randomUUID().toString(),
            setOf(DatalandRealmRole.ROLE_UPLOADER),
        )

        assertThrows<InsufficientRightsApiException> {
            service.setJudgementState(UUID.randomUUID(), DatasetJudgementState.FinishedWithDatasetAcceptance)
        }
    }

    @Test
    fun `setJudgementState throws ConflictApiException when current judgement state is Finished`() {
        datasetJudgementEntity.judgementState = DatasetJudgementState.FinishedWithDatasetAcceptance

        assertThrows<ConflictApiException> {
            service.setJudgementState(UUID.randomUUID(), DatasetJudgementState.FinishedWithDatasetAcceptance)
        }
    }

    @Test
    fun `setJudgementState throws error when finishing judgement with unreviewed datapoints`() {
        datasetJudgementEntity.dataPoints.first().acceptedSource = null
        assertThrows<InvalidInputApiException> {
            service.setJudgementState(UUID.randomUUID(), DatasetJudgementState.FinishedWithDatasetAcceptance)
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
        assertEquals("Dummy User", result.qaJudgeUserName)
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
    }

    @Test
    fun `postDatasetJudgement throws ConflictApiException when datasetJudgementEntity already exists`() {
        val dummyDatapointId = UUID.randomUUID().toString()

        doReturn(mapOf(mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE to dummyDatapointId))
            .whenever(datasetJudgementSupportService)
            .getContainedDataPoints(any())

        doReturn(listOf(datasetJudgementEntity))
            .whenever(datasetJudgementRepository)
            .findAllByDatasetId(any())

        assertThrows<ConflictApiException> {
            service.postDatasetJudgement(UUID.randomUUID())
        }
    }
}
