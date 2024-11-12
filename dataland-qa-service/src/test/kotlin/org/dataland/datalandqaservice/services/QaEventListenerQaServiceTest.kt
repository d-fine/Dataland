package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
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
    val noIdPayload = JSONObject(mapOf("identifier" to "", "comment" to "test")).toString()
    val correlationId = "correlationId"

    private fun getPersistAutomatedQaResultMessage(
        identifier: String,
        validationResult: QaStatus,
        message: String?,
    ): String =
        JSONObject(
            mapOf(
                "identifier" to identifier, "validationResult" to validationResult, "reviewerId" to AUTOMATED_QA,
                "resourceType" to "data", "message" to message,
            ),
        ).toString()

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
                    .addDataSetToQaReviewRepositoryWithStatusPending(noIdPayload, correlationId, MessageType.MANUAL_QA_REQUESTED)
            }
        Assertions.assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }

    @Test
    fun `check that an exception is thrown when sending a success notification to message queue fails`() {
        val message =
            objectMapper.writeValueAsString(
                QaCompletedMessage(
                    identifier = dataId,
                    validationResult = QaStatus.Accepted,
                    reviewerId = "someId",
                    message = null,
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
            qaEventListenerQaService.addDataSetToQaReviewRepositoryWithStatusPending(dummyPayload, correlationId, MessageType.DATA_STORED)
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
        Assertions.assertEquals("Message was rejected: Provided document ID is empty", thrown.message)
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
        Assertions.assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
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

        val rejectedStoredCompanyJSon = "json/services/StoredCompanyRejected.json"
        val rejectedStoredCompany = objectMapper.readValue(getJsonString(rejectedStoredCompanyJSon), StoredCompany::class.java)
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
    fun `check an that only automated qa results are stored correctly in the QA review repository`() {
        val dataId = "thisIdShouldntBeStored"
        val persistAutomatedQaResultMessage =
            JSONObject(
                mapOf(
                    "identifier" to dataId, "validationResult" to QaStatus.Accepted, "reviewerId" to "someReviewerId",
                    "resourceType" to "notData", "message" to "test message",
                ),
            ).toString()
        qaEventListenerQaService.addDataReviewFromAutomatedQaToReviewHistoryRepository(
            persistAutomatedQaResultMessage, correlationId, MessageType.PERSIST_AUTOMATED_QA_RESULT,
        )
        Assertions.assertNull(testQaReviewRepository.findByDataId(dataId))
    }

    private fun getJsonString(resourceFile: String): String =
        objectMapper
            .readTree(
                this.javaClass.classLoader.getResourceAsStream(resourceFile)
                    ?: throw IllegalArgumentException("Could not load the resource file"),
            ).toString()
}
