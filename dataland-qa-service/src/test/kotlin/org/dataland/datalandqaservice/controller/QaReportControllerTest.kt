package org.dataland.datalandqaservice.controller

import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.frameworks.sfdr.model.SfdrData
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.frameworks.sfdr.SfdrDataQaReportController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportStatusPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.dataland.datalandqaservice.utils.NoBackendRequestQaReportConfiguration
import org.dataland.datalandqaservice.utils.UtilityFunctions
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.UUID

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [
        DatalandQaService::class,
        NoBackendRequestQaReportConfiguration::class,
    ],
    properties = ["spring.profiles.active=nodb"],
)
class QaReportControllerTest(
    @Autowired private val qaReportController: SfdrDataQaReportController,
    @Autowired private val qaReportRepository: QaReportRepository,
) {
    @MockBean
    private lateinit var metaDataControllerApi: MetaDataControllerApi

    private fun createMockDataIdForAnSfdrDataset(): String {
        val dataId = UUID.randomUUID().toString()
        Mockito.`when`(metaDataControllerApi.getDataMetaInfo(dataId)).thenReturn(
            DataMetaInformation(
                dataId = dataId,
                companyId = UUID.randomUUID().toString(),
                dataType = DataTypeEnum.sfdr,
                reportingPeriod = "period",
                qaStatus = QaStatus.Accepted,
                currentlyActive = true,
                uploadTime = 0,
            ),
        )
        return dataId
    }

    private fun createEmptyQaReport(existingDataId: String? = null): QaReportMetaInformation {
        val dataId = existingDataId ?: createMockDataIdForAnSfdrDataset()
        return qaReportController.postQaReport(dataId, SfdrData()).body!!
    }

    @Test
    fun `post a qa report and make sure its status can be changed`() {
        UtilityFunctions.withReviewerAuthentication {
            val qaMetaInfo = createEmptyQaReport()

            assertEquals(
                qaReportController
                    .getAllQaReportsForDataset(
                        dataId = qaMetaInfo.dataId,
                        showInactive = false,
                        reporterUserId = null,
                    ).body!!
                    .first()
                    .metaInfo.active,
                true,
            )

            qaReportController.setQaReportStatus(
                dataId = qaMetaInfo.dataId,
                qaReportId = qaMetaInfo.qaReportId,
                statusPatch = QaReportStatusPatch(false),
            )

            assertEquals(
                qaReportController
                    .getAllQaReportsForDataset(
                        dataId = qaMetaInfo.dataId,
                        showInactive = true,
                        reporterUserId = null,
                    ).body!!
                    .first()
                    .metaInfo.active,
                false,
            )
        }
    }

    @Test
    fun `make sure changing the status of a report as a reviewer other than the reporter throws an exception`() {
        var qaMetaInfo: QaReportMetaInformation? = null
        UtilityFunctions.withReviewerAuthentication {
            qaMetaInfo = createEmptyQaReport()
        }

        UtilityFunctions.withReviewerAuthentication(
            "some-other-reviewer",
        ) {
            assertThrows<InsufficientRightsApiException> {
                qaReportController.setQaReportStatus(
                    dataId = qaMetaInfo!!.dataId,
                    qaReportId = qaMetaInfo!!.qaReportId,
                    statusPatch = QaReportStatusPatch(false),
                )
            }
        }
    }

    @Test
    fun `check that requesting a non existent report id on an existing data id throws a not found error`() {
        UtilityFunctions.withReviewerAuthentication {
            val qaMetaInfo = createEmptyQaReport()
            assertThrows<ResourceNotFoundApiException> {
                qaReportController.getQaReport(
                    dataId = qaMetaInfo.dataId,
                    qaReportId = UUID.randomUUID().toString(),
                )
            }
        }
    }

    @Test
    fun `check that using a report id associated with a different data id throws an exception`() {
        UtilityFunctions.withReviewerAuthentication {
            val qaReport1 = createEmptyQaReport()
            val qaReport2 = createEmptyQaReport()

            val exception =
                assertThrows<InvalidInputApiException> {
                    qaReportController.getQaReport(
                        dataId = qaReport1.dataId,
                        qaReportId = qaReport2.qaReportId,
                    )
                }
            assertTrue(
                exception.message.contains(
                    "The requested Qa Report '${qaReport2.qaReportId}' is not associated " +
                        "with data '${qaReport1.dataId}', but with data '${qaReport2.dataId}'.",
                ),
                "The exception message should contain the expected error message",
            )
        }
    }

    @Test
    fun `check that the reviewer user id filter works`() {
        val dataId = createMockDataIdForAnSfdrDataset()
        UtilityFunctions.withReviewerAuthentication("reviewer-1") {
            createEmptyQaReport(dataId)
            assertEquals(
                1,
                qaReportController
                    .getAllQaReportsForDataset(
                        dataId, false, "reviewer-1",
                    ).body!!
                    .size,
            )
            assertEquals(
                0,
                qaReportController
                    .getAllQaReportsForDataset(
                        dataId, false, "other-reviewer",
                    ).body!!
                    .size,
            )
        }
    }

    @Test
    fun `posting a qa report for a non matching data type should fail`() {
        UtilityFunctions.withReviewerAuthentication {
            val dataId = UUID.randomUUID().toString()
            Mockito.`when`(metaDataControllerApi.getDataMetaInfo(dataId)).thenReturn(
                DataMetaInformation(
                    dataId = dataId,
                    companyId = UUID.randomUUID().toString(),
                    dataType = DataTypeEnum.eutaxonomyMinusFinancials,
                    reportingPeriod = "period",
                    qaStatus = QaStatus.Accepted,
                    currentlyActive = true,
                    uploadTime = 0,
                ),
            )
            val ex =
                assertThrows<InvalidInputApiException> {
                    qaReportController.postQaReport(dataId, SfdrData())
                }
            assertTrue(
                ex.message.contains(
                    "is of type '${DataTypeEnum.eutaxonomyMinusFinancials}', but the expected type is 'sfdr'",
                ),
                "The exception message should indicate the framework mismatch",
            )
        }
    }

    @Test
    fun `requesting a qa report for a non matching data type should fail`() {
        val dataId = UUID.randomUUID().toString()
        val reportId = UUID.randomUUID().toString()
        qaReportRepository.save(
            QaReportEntity(
                qaReportId = reportId,
                qaReport = "{}",
                dataId = dataId,
                dataType = "some-data-type",
                reporterUserId = "some-reporter",
                active = true,
                uploadTime = 0,
            ),
        )

        UtilityFunctions.withReviewerAuthentication {
            val ex =
                assertThrows<InvalidInputApiException> {
                    qaReportController.getQaReport(dataId, reportId)
                }
            assertTrue(
                ex.message.contains(
                    "is not associated with data type 'sfdr'," +
                        " but with data type 'some-data-type'",
                ),
                "Error message should indicate the framework data-type mismatch",
            )
        }
    }
}
