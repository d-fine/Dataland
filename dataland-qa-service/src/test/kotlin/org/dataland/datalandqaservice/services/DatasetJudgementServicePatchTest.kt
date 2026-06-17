package org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.JudgementDetailsPatch
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

class DatasetJudgementServicePatchTest : DatasetJudgementServiceTestBase() {
    private fun patchAndGetDataPoint(
        source: AcceptedDataPointSource?,
        reporterUserId: String? = null,
        customValue: String? = null,
        reasonForCustomDataPoint: String? = null,
        dataPointType: String = mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
    ): DataPointJudgementEntity {
        service.patchJudgementDetails(
            UUID.randomUUID(),
            dataPointType,
            JudgementDetailsPatch(source, reporterUserId, customValue, reasonForCustomDataPoint),
        )

        return captureSavedJudgement()
            .dataPoints
            .first { it.dataPointType == dataPointType }
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
    fun `patchJudgementDetails with Custom and non-null reason stores reason on entity`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Custom,
                customValue = mockDatasetJudgementEntityForTest.CUSTOM_VALUE,
                reasonForCustomDataPoint = mockDatasetJudgementEntityForTest.REASON_FOR_CUSTOM_DATAPOINT,
            )

        assertEquals(AcceptedDataPointSource.Custom, saved.acceptedSource)
        assertEquals(mockDatasetJudgementEntityForTest.REASON_FOR_CUSTOM_DATAPOINT, saved.reasonForCustomDataPoint)
    }

    @Test
    fun `patchJudgementDetails with Custom and null reason stores null on entity`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Custom,
                customValue = mockDatasetJudgementEntityForTest.CUSTOM_VALUE,
                reasonForCustomDataPoint = null,
            )

        assertEquals(AcceptedDataPointSource.Custom, saved.acceptedSource)
        assertNull(saved.reasonForCustomDataPoint)
    }

    @Test
    fun `patchJudgementDetails with Original clears reasonForCustomDataPoint`() {
        datasetJudgementEntity.dataPoints.first().apply {
            reasonForCustomDataPoint = mockDatasetJudgementEntityForTest.REASON_FOR_CUSTOM_DATAPOINT
        }

        val saved = patchAndGetDataPoint(AcceptedDataPointSource.Original)

        assertEquals(AcceptedDataPointSource.Original, saved.acceptedSource)
        assertNull(saved.reasonForCustomDataPoint)
    }

    @Test
    fun `patchJudgementDetails with Qa clears reasonForCustomDataPoint`() {
        datasetJudgementEntity.dataPoints.first().apply {
            reasonForCustomDataPoint = mockDatasetJudgementEntityForTest.REASON_FOR_CUSTOM_DATAPOINT
        }

        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Qa,
                reporterUserId = mockDatasetJudgementEntityForTest.dummyUserId.toString(),
            )

        assertEquals(AcceptedDataPointSource.Qa, saved.acceptedSource)
        assertNull(saved.reasonForCustomDataPoint)
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
