package org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataPointMetaInformation
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointQaReviewInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager
import org.dataland.datalandqaservice.utils.UtilityFunctions
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.datalandspecificationservice.openApiClient.infrastructure.ClientException
import org.dataland.datalandspecificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.datalandspecificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.model.QaStatus as OpenApiClientQaStatus
import org.dataland.datalandbackendutils.model.QaStatus as BackendUtilsQaStatus

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [
        DatalandQaService::class,
    ],
    properties = ["spring.profiles.active=nodb"],
)
class QaControllerTest(
    @Autowired private val qaController: QaController,
    @Autowired private val dataPointQaReviewManager: DataPointQaReviewManager,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dataPointQaReviewRepository: DataPointQaReviewRepository,
) {
    @MockitoBean
    private lateinit var dataPointControllerApi: DataPointControllerApi

    @MockitoBean
    private lateinit var cloudEventMessageHandler: CloudEventMessageHandler

    @MockitoBean
    lateinit var specificationClient: SpecificationControllerApi

    val dataId = UUID.randomUUID().toString()
    val originalActiveDataId = UUID.randomUUID().toString()
    val dataPointType = "some-type"
    val reportingPeriod = "2022"
    val companyId = UUID.randomUUID().toString()
    val companyName = "some-company"
    val firstComment = "OriginalActive"
    val dataTypeTestFramework = "test framework"
    val testFrameworkSpecification =
        FrameworkSpecification(
            framework = IdWithRef(id = "testID", ref = "testFrameworkRef"),
            name = "Test Framework",
            businessDefinition = "Defines the business rules for the test framework.",
            schema =
                """
                {
                  "general": {
                    "key1": {
                      "id": "some-type",
                      "ref": "ref1"
                    },
                    "key2": {
                      "id": "another-data-Point-type",
                      "ref": "ref2"
                    }
                  }
                }
                """.trimIndent(),
            referencedReportJsonPath = "/reports/testReport.json",
        )

    private fun specifyMocks() {
        `when`(dataPointControllerApi.getDataPointMetaInfo(any())).thenReturn(
            DataPointMetaInformation(
                dataPointId = "dummy",
                dataPointType = dataPointType,
                companyId = companyId,
                reportingPeriod = reportingPeriod,
                qaStatus = OpenApiClientQaStatus.Accepted,
                currentlyActive = true,
                uploadTime = 0,
                uploaderUserId = "",
            ),
        )
        whenever(specificationClient.doesFrameworkSpecificationExist(anyString())).thenAnswer { invocation ->
            val frameworkName = invocation.arguments[0] as String
            if (frameworkName == dataTypeTestFramework) {
                true
            } else {
                throw ClientException("Simulated failure for framework: $frameworkName")
            }
        }
        `when`(specificationClient.getFrameworkSpecification(dataTypeTestFramework)).thenReturn(
            testFrameworkSpecification,
        )
        `when`(
            cloudEventMessageHandler
                .buildCEMessageAndSendToQueue(any(), any(), any(), any(), any()),
        ).thenAnswer { println("Sending message to queue") }
    }

    private fun createMockDataPoints() {
        for (dataId in listOf(dataId, originalActiveDataId)) {
            dataPointQaReviewRepository.save(
                DataPointQaReviewEntity(
                    dataPointId = dataId,
                    companyId = companyId,
                    companyName = companyName,
                    dataPointType = dataPointType,
                    reportingPeriod = reportingPeriod,
                    timestamp = 0,
                    qaStatus = QaStatus.Pending,
                    triggeringUserId = "some-reviewer",
                    comment = "comment",
                ),
            )
        }
        dataPointQaReviewRepository.save(
            DataPointQaReviewEntity(
                dataPointId = "another-data-point-ID",
                companyId = companyId,
                companyName = companyName,
                dataPointType = "another-data-Point-type",
                reportingPeriod = reportingPeriod,
                timestamp = 0,
                qaStatus = QaStatus.Accepted,
                triggeringUserId = "some-reviewer",
                comment = "comment",
            ),
        )
    }

    private fun createMessageBody(
        dataId: String,
        updatedQaStatus: BackendUtilsQaStatus,
        currentlyActiveDataId: String,
    ): String =
        objectMapper.writeValueAsString(
            QaStatusChangeMessage(
                dataId = dataId,
                updatedQaStatus = updatedQaStatus,
                currentlyActiveDataId = currentlyActiveDataId,
            ),
        )

    private fun getReviewEntries(
        showOnlyActive: Boolean? = true,
        dataType: String?,
        qaStatus: QaStatus? = null,
    ): List<DataPointQaReviewInformation> =
        qaController
            .getDataPointQaReviewInformation(
                companyId = companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                qaStatus = qaStatus,
                showOnlyActive = showOnlyActive,
                chunkSize = 10,
                chunkIndex = 0,
            ).body!!

    private fun verifyQaStatusChangeEmitsCorrectCloudEvents() {
        val expectedBodyForNewSetToActive = createMessageBody(dataId, BackendUtilsQaStatus.Accepted, dataId)
        val expectedBodyForOriginalSetToActive: String =
            createMessageBody(dataId, BackendUtilsQaStatus.Rejected, originalActiveDataId)

        qaController.changeDataPointQaStatus(originalActiveDataId, BackendUtilsQaStatus.Accepted, firstComment)
        qaController.changeDataPointQaStatus(dataId, BackendUtilsQaStatus.Pending, "Pending")
        qaController.changeDataPointQaStatus(dataId, BackendUtilsQaStatus.Accepted, "Accepted")
        qaController.changeDataPointQaStatus(dataId, BackendUtilsQaStatus.Rejected, "Rejected")
        verify(cloudEventMessageHandler, times(1)).buildCEMessageAndSendToQueue(
            eq(expectedBodyForNewSetToActive),
            eq(MessageType.QA_STATUS_UPDATED),
            any(),
            eq(ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS),
            eq(RoutingKeyNames.DATA_POINT_QA),
        )

        verify(cloudEventMessageHandler, times(1)).buildCEMessageAndSendToQueue(
            eq(expectedBodyForOriginalSetToActive),
            eq(MessageType.QA_STATUS_UPDATED),
            any(),
            eq(ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS),
            eq(RoutingKeyNames.DATA_POINT_QA),
        )
    }

    @Test
    fun `verify that the various endpoints return the correct order and content for data point QA review entries`() {
        createMockDataPoints()
        specifyMocks()
        for (mockDataId in listOf(dataId, originalActiveDataId)) {
            dataPointQaReviewManager.reviewDataPoints(
                listOf(
                    DataPointQaReviewManager.ReviewDataPointTask(
                        dataPointId = mockDataId,
                        qaStatus = QaStatus.Pending,
                        triggeringUserId = "some-user-id",
                        comment = "This simulates the message queue event from the backend.",
                        correlationId = "some-correlation-id",
                        timestamp = 0,
                    ),
                ),
            )
        }

        UtilityFunctions.withReviewerAuthentication {
            verifyQaStatusChangeEmitsCorrectCloudEvents()

            val reviewEntries = qaController.getDataPointQaReviewInformationByDataId(dataId).body!!
            assertEquals(5, reviewEntries.size)
            assertEquals(BackendUtilsQaStatus.Rejected, reviewEntries.first().qaStatus)

            val latestReviewEntries = getReviewEntries(dataType = dataPointType)
            assertEquals(1, latestReviewEntries.size)
            assertEquals(BackendUtilsQaStatus.Accepted, latestReviewEntries.first().qaStatus)

            val allReviewEntries = getReviewEntries(showOnlyActive = false, dataType = dataPointType)
            assertEquals(8, allReviewEntries.size)
            assertEquals(firstComment, allReviewEntries[allReviewEntries.size - 5].comment)

            val allTestFrameworkDataPoints = getReviewEntries(showOnlyActive = false, dataType = dataTypeTestFramework)
            assertEquals(9, allTestFrameworkDataPoints.size)

            val onlyActiveTestFrameworkDataPoints = getReviewEntries(dataType = dataTypeTestFramework)
            assertEquals(2, onlyActiveTestFrameworkDataPoints.size)

            val onlyActiveButRejectedFrameworkDataPoints =
                getReviewEntries(dataType = dataTypeTestFramework, qaStatus = BackendUtilsQaStatus.Rejected)
            assertEquals(0, onlyActiveButRejectedFrameworkDataPoints.size)

            val emptyDataType = getReviewEntries(dataType = null)
            assertEquals(2, emptyDataType.size)
        }
    }
}
