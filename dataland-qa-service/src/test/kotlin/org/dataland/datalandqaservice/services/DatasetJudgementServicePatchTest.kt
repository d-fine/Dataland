package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
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
        acceptedQaReportId: String? = null,
        customValue: String? = null,
        reviewerComment: String? = null,
        dataPointType: String = mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
    ): DataPointJudgementEntity {
        service.patchJudgementDetails(
            UUID.randomUUID(),
            dataPointType,
            JudgementDetailsPatch(source, null, customValue, reviewerComment, acceptedQaReportId),
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
        assertNull(saved.acceptedQaReportId)
    }

    @Test
    fun `patchJudgementDetails with Qa sets acceptedSource and AcceptedQaReport source`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Qa,
                acceptedQaReportId =
                    datasetJudgementEntity.dataPoints
                        .first()
                        .qaReports
                        .first()
                        .qaReportId,
            )

        assertEquals(AcceptedDataPointSource.Qa, saved.acceptedSource)
        assertEquals(mockDatasetJudgementEntityForTest.dummyUserId, saved.reporterUserIdOfAcceptedQaReport)
        assertEquals(saved.qaReports.first().qaReportId, saved.acceptedQaReportId)
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
        assertNull(saved.acceptedQaReportId)
    }

    @Test
    fun `patchJudgementDetails with Custom and non-null comment stores comment on entity`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Custom,
                customValue = mockDatasetJudgementEntityForTest.CUSTOM_VALUE,
                reviewerComment = mockDatasetJudgementEntityForTest.REVIEWER_COMMENT,
            )

        assertEquals(AcceptedDataPointSource.Custom, saved.acceptedSource)
        assertEquals(mockDatasetJudgementEntityForTest.REVIEWER_COMMENT, saved.reviewerComment)
    }

    @Test
    fun `patchJudgementDetails with Custom and null comment stores null on entity`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Custom,
                customValue = mockDatasetJudgementEntityForTest.CUSTOM_VALUE,
                reviewerComment = null,
            )

        assertEquals(AcceptedDataPointSource.Custom, saved.acceptedSource)
        assertNull(saved.reviewerComment)
    }

    private fun firstQaReportId() =
        datasetJudgementEntity.dataPoints
            .first()
            .qaReports
            .first()
            .qaReportId

    @Test
    fun `patchJudgementDetails with Original stores reviewerComment`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Original,
                reviewerComment = mockDatasetJudgementEntityForTest.REVIEWER_COMMENT,
            )

        assertEquals(AcceptedDataPointSource.Original, saved.acceptedSource)
        assertEquals(mockDatasetJudgementEntityForTest.REVIEWER_COMMENT, saved.reviewerComment)
    }

    @Test
    fun `patchJudgementDetails with Qa stores reviewerComment`() {
        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Qa,
                acceptedQaReportId = firstQaReportId(),
                reviewerComment = mockDatasetJudgementEntityForTest.REVIEWER_COMMENT,
            )

        assertEquals(AcceptedDataPointSource.Qa, saved.acceptedSource)
        assertEquals(mockDatasetJudgementEntityForTest.REVIEWER_COMMENT, saved.reviewerComment)
    }

    @Test
    fun `patchJudgementDetails with Original keeps existing reviewerComment when none is provided`() {
        datasetJudgementEntity.dataPoints.first().reviewerComment = mockDatasetJudgementEntityForTest.REVIEWER_COMMENT

        val saved = patchAndGetDataPoint(AcceptedDataPointSource.Original)

        assertEquals(mockDatasetJudgementEntityForTest.REVIEWER_COMMENT, saved.reviewerComment)
    }

    @Test
    fun `patchJudgementDetails with Qa keeps existing reviewerComment when none is provided`() {
        datasetJudgementEntity.dataPoints.first().reviewerComment = mockDatasetJudgementEntityForTest.REVIEWER_COMMENT

        val saved = patchAndGetDataPoint(AcceptedDataPointSource.Qa, acceptedQaReportId = firstQaReportId())

        assertEquals(mockDatasetJudgementEntityForTest.REVIEWER_COMMENT, saved.reviewerComment)
    }

    @Test
    fun `patchJudgementDetails clears reviewerComment when a blank comment is provided`() {
        datasetJudgementEntity.dataPoints.first().reviewerComment = mockDatasetJudgementEntityForTest.REVIEWER_COMMENT

        val saved = patchAndGetDataPoint(AcceptedDataPointSource.Original, reviewerComment = "   ")

        assertNull(saved.reviewerComment)
    }

    @Test
    fun `patchJudgementDetails trims the reviewerComment`() {
        val saved = patchAndGetDataPoint(AcceptedDataPointSource.Original, reviewerComment = "  some comment  ")

        assertEquals("some comment", saved.reviewerComment)
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
    fun `patchJudgementDetails with Qa without acceptedQaReportId throws InvalidInputApiException`() {
        val exception =
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
        assertEquals("Missing accepted QA report ID.", exception.summary)
    }

    @Test
    fun `patchJudgementDetails with Qa rejects a report not associated with the data point`() {
        assertThrows<InvalidInputApiException> {
            patchAndGetDataPoint(AcceptedDataPointSource.Qa, acceptedQaReportId = UUID.randomUUID().toString())
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
    fun `patchJudgementDetails with Custom creates data point without original when dataPointType not in judgement`() {
        val missingType = "missing-type"
        val dataPointCountBefore = datasetJudgementEntity.dataPoints.size

        val saved =
            patchAndGetDataPoint(
                AcceptedDataPointSource.Custom,
                customValue = "{\"value\": 42}",
                reviewerComment = "added by judge",
                dataPointType = missingType,
            )

        assertNull(saved.dataPointId)
        assertEquals("{\"value\": 42}", saved.customValue)
        assertEquals(AcceptedDataPointSource.Custom, saved.acceptedSource)
        assertEquals("added by judge", saved.reviewerComment)
        assertEquals(datasetJudgementEntity, saved.datasetJudgement)
        assertEquals(dataPointCountBefore + 1, datasetJudgementEntity.dataPoints.size)
        verify(datasetJudgementSupportService).validateCustomDataPoint("{\"value\": 42}", missingType)
    }

    @Test
    fun `patchJudgementDetails with Custom on existing dataPointType does not create a new data point`() {
        val dataPointCountBefore = datasetJudgementEntity.dataPoints.size

        val saved = patchAndGetDataPoint(AcceptedDataPointSource.Custom, customValue = "{\"value\": 1}")

        assertEquals(dataPointCountBefore, datasetJudgementEntity.dataPoints.size)
        assertTrue(saved.dataPointId != null)
    }

    @Test
    fun `patchJudgementDetails throws when dataPointType not in judgement and accepted source is not Custom`() {
        listOf(AcceptedDataPointSource.Original, AcceptedDataPointSource.Qa, null).forEach { source ->
            assertThrows<InvalidInputApiException> {
                service.patchJudgementDetails(
                    UUID.randomUUID(),
                    "unknown-type",
                    JudgementDetailsPatch(source, null, "{\"value\": 1}", null, UUID.randomUUID().toString()),
                )
            }
        }
        assertTrue(datasetJudgementEntity.dataPoints.none { it.dataPointType == "unknown-type" })
    }

    @Test
    fun `patchJudgementDetails throws when dataPointType not in judgement and custom value is missing`() {
        assertThrows<InvalidInputApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                "unknown-type",
                JudgementDetailsPatch(AcceptedDataPointSource.Custom, null, null),
            )
        }
        assertTrue(datasetJudgementEntity.dataPoints.none { it.dataPointType == "unknown-type" })
    }

    @Test
    fun `patchJudgementDetails does not add data point when custom value for missing dataPointType is invalid`() {
        whenever(datasetJudgementSupportService.validateCustomDataPoint(any(), any()))
            .thenThrow(BackendClientException(statusCode = 400))

        assertThrows<InvalidInputApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                "unknown-type",
                JudgementDetailsPatch(AcceptedDataPointSource.Custom, null, "invalid"),
            )
        }
        assertTrue(datasetJudgementEntity.dataPoints.none { it.dataPointType == "unknown-type" })
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
            .thenThrow(
                BackendClientException(
                    statusCode = 400,
                    response = ClientError<String>(body = """{"errors":[{"message":"dataSource must not set publicationDate"}]}"""),
                ),
            )

        assertThrows<InvalidInputApiException> {
            service.patchJudgementDetails(
                UUID.randomUUID(),
                mockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
                JudgementDetailsPatch(null, null, """{"value": 1}"""),
            )
        }.also { exception ->
            assertTrue(exception.cause is BackendClientException)
            assertEquals("dataSource must not set publicationDate", exception.getErrorResponse().message)
        }
    }
}
