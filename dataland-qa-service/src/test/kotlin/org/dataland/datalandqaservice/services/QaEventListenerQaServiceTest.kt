package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.AutomatedQaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.amqp.AmqpException
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

private const val AUTOMATED_QA = "automated-qa-service"

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(classes = [DatalandQaService::class])
class QaEventListenerQaServiceTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired val testQaReviewRepository: QaReviewRepository,
) {
    lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler
    lateinit var qaEventListenerQaService: QaEventListenerQaService
    lateinit var mockMetaDataControllerApi: MetaDataControllerApi
    lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi

    val dataId = "TestDataId"
    val noIdPayload =
        objectMapper.writeValueAsString(
            AutomatedQaCompletedMessage(
                resourceId = "",
                qaStatus = QaStatus.Accepted,
                reviewerId = AUTOMATED_QA,
                bypassQa = true,
                comment = "test",
            ),
        )
    val correlationId = "correlationId"

    private fun getPersistAutomatedQaResultMessage(
        resourceId: String,
        qaStatus: QaStatus,
        message: String?,
    ): String =
        objectMapper.writeValueAsString(
            AutomatedQaCompletedMessage(
                resourceId = resourceId,
                qaStatus = qaStatus,
                reviewerId = AUTOMATED_QA,
                bypassQa = false,
                comment = message,
            ),
        )

    @BeforeEach
    fun resetMocks() {
        mockCloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
        mockMetaDataControllerApi = mock(MetaDataControllerApi::class.java)
        mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        qaEventListenerQaService =
            QaEventListenerQaService(
                mockCloudEventMessageHandler,
                objectMapper,
                messageUtils,
                testQaReviewRepository,
                mockCompanyDataControllerApi,
                mockMetaDataControllerApi,
            )
    }

    @Test
    fun `check an exception is thrown in reading out message from data stored queue when dataId is empty`() {
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                qaEventListenerQaService
                    .addDatasetToQaReviewRepositoryWithStatusPending(noIdPayload, correlationId, MessageType.MANUAL_QA_REQUESTED)
            }
        Assertions.assertEquals("Message was rejected: Provided data ID is empty (correlationId: $correlationId)", thrown.message)
    }

    @Test
    fun `check that an exception is thrown when sending a success notification to message queue fails`() {
        val message =
            objectMapper.writeValueAsString(
                AutomatedQaCompletedMessage(
                    resourceId = dataId,
                    qaStatus = QaStatus.Accepted,
                    reviewerId = "someId",
                    bypassQa = false,
                    comment = null,
                ),
            )
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                message, MessageType.QA_STATUS_CHANGED, correlationId, ExchangeName.DATA_QUALITY_ASSURED, RoutingKeyNames.DATA,
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        val dummyPayload = JSONObject(mapOf("dataId" to dataId, "bypassQa" to true.toString())).toString()
        assertThrows<AmqpException> {
            qaEventListenerQaService.addDatasetToQaReviewRepositoryWithStatusPending(dummyPayload, correlationId, MessageType.DATA_STORED)
        }
    }

    @Test
    fun `check an exception is thrown in reading out message from document stored queue when dataId is empty`() {
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                qaEventListenerQaService.assureQualityOfDocument(
                    noIdPayload, correlationId, MessageType.MANUAL_QA_REQUESTED,
                )
            }
        Assertions.assertEquals("Message was rejected: Provided document ID is empty (correlationId: $correlationId)", thrown.message)
    }

    @Test
    fun `check an exception is thrown in reading out message from data quality assured queue when dataId is empty`() {
        val qaAcceptedNoIdPayload = getPersistAutomatedQaResultMessage("", QaStatus.Accepted, "test message")
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                qaEventListenerQaService.addDataReviewFromAutomatedQaToReviewHistoryRepository(
                    qaAcceptedNoIdPayload,
                    correlationId, MessageType.PERSIST_AUTOMATED_QA_RESULT,
                )
            }
        Assertions.assertEquals("Message was rejected: Provided data ID is empty (correlationId: correlationId)", thrown.message)
    }

    @Test
    fun `check that the automated qa result is stored correctly in the review repository`() {
        val acceptedStoredCompanyJSon = "json/services/StoredCompanyAccepted.json"
        val acceptedStoredCompany = objectMapper.readValue(getJsonString(acceptedStoredCompanyJSon), StoredCompany::class.java)
        val acceptedDataMetaInformation = acceptedStoredCompany.dataRegisteredByDataland[0]
        val acceptedDataId = acceptedDataMetaInformation.dataId
        `when`(
            mockMetaDataControllerApi.getDataMetaInfo(
                acceptedDataId,
            ),
        ).thenReturn(
            acceptedDataMetaInformation,
        )
        `when`(mockCompanyDataControllerApi.getCompanyById(acceptedDataMetaInformation.companyId)).thenReturn(acceptedStoredCompany)
        val automatedQaAcceptedMessage = getPersistAutomatedQaResultMessage(acceptedDataId, QaStatus.Accepted, "accepted")
        qaEventListenerQaService.addDataReviewFromAutomatedQaToReviewHistoryRepository(
            automatedQaAcceptedMessage, correlationId, MessageType.PERSIST_AUTOMATED_QA_RESULT,
        )
        testQaReviewRepository.findByDataId(acceptedDataId)?.let {
            Assertions.assertEquals(AUTOMATED_QA, it.reviewerId)
            Assertions.assertEquals(acceptedDataId, it.dataId)
            Assertions.assertEquals(QaStatus.Accepted, it.qaStatus)
            Assertions.assertEquals("accepted", it.comment)
        }

        val rejectedStoredCompanyJson = "json/services/StoredCompanyRejected.json"
        val rejectedStoredCompany = objectMapper.readValue(getJsonString(rejectedStoredCompanyJson), StoredCompany::class.java)
        val rejectedDataMetaInformation = rejectedStoredCompany.dataRegisteredByDataland[0]
        val rejectedDataId = rejectedDataMetaInformation.dataId

        `when`(
            mockMetaDataControllerApi.getDataMetaInfo(
                rejectedDataId,
            ),
        ).thenReturn(
            rejectedDataMetaInformation,
        )
        `when`(mockCompanyDataControllerApi.getCompanyById(rejectedDataMetaInformation.companyId)).thenReturn(rejectedStoredCompany)

        val automatedQaRejectedMessage = getPersistAutomatedQaResultMessage(rejectedDataId, QaStatus.Rejected, "rejected")
        qaEventListenerQaService.addDataReviewFromAutomatedQaToReviewHistoryRepository(
            automatedQaRejectedMessage, correlationId, MessageType.PERSIST_AUTOMATED_QA_RESULT,
        )
        testQaReviewRepository.findByDataId(rejectedDataId)?.let {
            Assertions.assertEquals(AUTOMATED_QA, it.reviewerId)
            Assertions.assertEquals(rejectedDataId, it.dataId)
            Assertions.assertEquals(QaStatus.Rejected, it.qaStatus)
            Assertions.assertEquals("rejected", it.comment)
        }
    }

    @Test
    fun `check that a bypassQA=true result is stored correctly in the QA review repository`() {
        val dataId = "thisIdIsStoredAsAccepted"
        val persistAutomatedQaResultMessage =
            objectMapper.writeValueAsString(
                AutomatedQaCompletedMessage(
                    resourceId = dataId,
                    qaStatus = QaStatus.Accepted,
                    reviewerId = "someReviewerId",
                    comment = "test message",
                    bypassQa = false,
                ),
            )
        val acceptedStoredCompanyJson = "json/services/StoredCompanyAccepted.json"
        val acceptedStoredCompany = objectMapper.readValue(getJsonString(acceptedStoredCompanyJson), StoredCompany::class.java)
        val acceptedDataMetaInformation: DataMetaInformation = acceptedStoredCompany.dataRegisteredByDataland[0]
        val acceptedDataId = acceptedDataMetaInformation.dataId

        `when`(mockMetaDataControllerApi.getDataMetaInfo(dataId)).thenReturn(acceptedDataMetaInformation)
        `when`(mockCompanyDataControllerApi.getCompanyById(acceptedDataMetaInformation.companyId)).thenReturn(acceptedStoredCompany)

        qaEventListenerQaService.addDataReviewFromAutomatedQaToReviewHistoryRepository(
            persistAutomatedQaResultMessage, correlationId, MessageType.PERSIST_AUTOMATED_QA_RESULT,
        )

        testQaReviewRepository.findByDataId(acceptedDataId)?.let {
            Assertions.assertEquals(AUTOMATED_QA, it.reviewerId)
            Assertions.assertEquals(acceptedDataId, it.dataId)
            Assertions.assertEquals(QaStatus.Accepted, it.qaStatus)
        }
    }

    private fun getJsonString(resourceFile: String): String =
        objectMapper
            .readTree(
                this.javaClass.classLoader.getResourceAsStream(resourceFile)
                    ?: throw IllegalArgumentException("Could not load the resource file"),
            ).toString()
}
