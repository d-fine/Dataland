package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.ManualQaRequestedMessage
import org.dataland.datalandmessagequeueutils.messages.data.DataMetaInfoPatchPayload
import org.dataland.datalandmessagequeueutils.messages.data.DataUploadedPayload
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.amqp.AmqpException
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [DatalandQaService::class],
    properties = ["spring.profiles.active=nodb"],
)
class QaEventListenerQaServiceTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val testQaReviewRepository: QaReviewRepository,
) {
    private val mockCloudEventMessageHandler: CloudEventMessageHandler = mock<CloudEventMessageHandler>()
    private val mockQaReviewManager: QaReviewManager = mock<QaReviewManager>()
    private val mockDataPointQaReviewManager: DataPointQaReviewManager = mock<DataPointQaReviewManager>()
    private val mockQaReportManager: QaReportManager = mock<QaReportManager>()
    private val mockMetaDataControllerApi: MetaDataControllerApi = mock<MetaDataControllerApi>()
    private val mockCompanyDataControllerApi: CompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private val mockDataPointControllerApi: DataPointControllerApi = mock<DataPointControllerApi>()

    private lateinit var qaEventListenerQaService: QaEventListenerQaService

    val dataId = UUID.randomUUID().toString()
    val uploaderUserId = "UploaderUserId"
    val correlationId = "correlationId"

    @BeforeEach
    fun setup() {
        reset(
            mockCloudEventMessageHandler,
            mockMetaDataControllerApi,
            mockCompanyDataControllerApi,
            mockQaReviewManager,
            mockDataPointQaReviewManager,
            mockQaReportManager,
            mockDataPointControllerApi,
        )
        qaEventListenerQaService =
            QaEventListenerQaService(
                mockCloudEventMessageHandler,
                objectMapper,
                mockQaReviewManager,
                mockDataPointQaReviewManager,
                mockQaReportManager,
                mockMetaDataControllerApi,
                mockDataPointControllerApi,
            )
    }

    private fun setupMockMessage(routingKey: String): Message {
        val mockMessageProperties =
            mock<MessageProperties> { on { receivedRoutingKey } doReturn routingKey }
        return mock<Message> { on { messageProperties } doReturn mockMessageProperties }
    }

    private fun getDataUploadPayload(
        dataId: String,
        bypassQa: Boolean,
    ): String = objectMapper.writeValueAsString(DataUploadedPayload(dataId, bypassQa))

    private fun getDataMetaInfoPatchPayload(
        dataId: String,
        uploaderUserId: String?,
    ): String = objectMapper.writeValueAsString(DataMetaInfoPatchPayload(dataId, uploaderUserId))

    private fun getManualQaRequestedMessage(
        resourceId: String,
        bypassQa: Boolean,
    ): String = objectMapper.writeValueAsString(ManualQaRequestedMessage(resourceId, bypassQa))

    @Test
    fun `check that an exception is thrown in reading out message from data stored queue when dataId is empty`() {
        val mockMessage = setupMockMessage(RoutingKeyNames.DATASET_UPLOAD)
        val noIdPayload = getDataUploadPayload("", false)
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                qaEventListenerQaService
                    .processBackendDatasetEvents(
                        mockMessage,
                        noIdPayload,
                        correlationId,
                        MessageType.PUBLIC_DATA_RECEIVED,
                    )
            }
        Assertions.assertEquals("Invalid UUID string: ", thrown.message)
    }

    @Test
    fun `check that processing upload event works`() {
        val mockMessage = setupMockMessage(RoutingKeyNames.DATASET_UPLOAD)
        val payload = getDataUploadPayload(dataId, bypassQa = true)
        doNothing().whenever(mockQaReviewManager).addDatasetToQaReviewRepository(any(), any(), any())

        assertDoesNotThrow {
            qaEventListenerQaService.processBackendDatasetEvents(
                mockMessage,
                payload,
                correlationId,
                MessageType.PUBLIC_DATA_RECEIVED,
            )
        }

        verify(mockQaReviewManager, times(1)).addDatasetToQaReviewRepository(dataId, bypassQa = true, correlationId)
    }

    @Test
    fun `check that processing patch event works`() {
        val mockMessage = setupMockMessage(RoutingKeyNames.METAINFORMATION_PATCH)
        val payload = getDataMetaInfoPatchPayload(dataId, uploaderUserId)
        doNothing().whenever(mockQaReviewManager).patchUploaderUserIdInQaReviewEntry(any(), any(), any())

        assertDoesNotThrow {
            qaEventListenerQaService.processBackendDatasetEvents(
                mockMessage,
                payload,
                correlationId,
                MessageType.METAINFO_UPDATED,
            )
        }

        verify(mockQaReviewManager, times(1)).patchUploaderUserIdInQaReviewEntry(dataId, uploaderUserId, correlationId)
    }

    @Test
    fun `check that processing method correctly throws if routing key is unknown`() {
        val receivedRoutingKey = "someWeirdRoutingKey"
        val mockMessage = setupMockMessage(receivedRoutingKey)

        val messageQueueRejectException =
            assertThrows<MessageQueueRejectException> {
                qaEventListenerQaService.processBackendDatasetEvents(
                    mockMessage,
                    "payload",
                    correlationId,
                    MessageType.METAINFO_UPDATED,
                )
            }
        Assertions.assertEquals(
            "Message was rejected: Routing Key '$receivedRoutingKey' unknown. " +
                "Expected Routing Key ${RoutingKeyNames.DATASET_UPLOAD} or ${RoutingKeyNames.METAINFORMATION_PATCH}",
            messageQueueRejectException.message,
        )
    }

    @Test
    fun `check that an exception is thrown when sending a success notification to message queue fails`() {
        val mockMessage = setupMockMessage("")
        val payload = getManualQaRequestedMessage(dataId, bypassQa = false)

        doThrow(AmqpException::class).whenever(mockCloudEventMessageHandler).buildCEMessageAndSendToQueue(
            payload,
            MessageType.QA_STATUS_UPDATED,
            correlationId,
            ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS,
            RoutingKeyNames.DATA,
        )

        assertThrows<AmqpException> {
            qaEventListenerQaService.processBackendDatasetEvents(
                mockMessage,
                payload,
                correlationId,
                MessageType.DATA_STORED,
            )
        }
    }

    @Test
    fun `check that an exception is thrown in reading out message from document stored queue when documentId is empty`() {
        val noIdPayload = objectMapper.writeValueAsString(ManualQaRequestedMessage("", false))
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                qaEventListenerQaService.assureQualityOfDocument(
                    noIdPayload, correlationId, MessageType.QA_REQUESTED,
                )
            }
        Assertions.assertEquals(
            "Message was rejected: Provided document ID is empty (correlationId: $correlationId)",
            thrown.message,
        )
    }

    @Test
    fun `check that a bypassQA result is stored correctly in the QA review repository`() {
        val mockMessage = setupMockMessage(RoutingKeyNames.DATASET_UPLOAD)
        val dataUploadedPayload = getDataUploadPayload(dataId, bypassQa = true)

        val acceptedStoredCompanyJson = "json/services/StoredCompanyAccepted.json"
        val acceptedStoredCompany =
            objectMapper.readValue<StoredCompany>(getJsonString(acceptedStoredCompanyJson))
        val acceptedDataMetaInformation: DataMetaInformation = acceptedStoredCompany.dataRegisteredByDataland[0]
        val acceptedDataId = acceptedDataMetaInformation.dataId

        doReturn(acceptedDataMetaInformation).whenever(mockMetaDataControllerApi).getDataMetaInfo(dataId)
        doReturn(acceptedStoredCompany)
            .whenever(mockCompanyDataControllerApi)
            .getCompanyById(acceptedDataMetaInformation.companyId)

        qaEventListenerQaService.processBackendDatasetEvents(
            mockMessage, dataUploadedPayload, correlationId, MessageType.PUBLIC_DATA_RECEIVED,
        )

        testQaReviewRepository.findFirstByDataIdOrderByTimestampDesc(acceptedDataId)?.let {
            Assertions.assertEquals("", it.triggeringUserId)
            Assertions.assertEquals(acceptedDataId, it.dataId)
            Assertions.assertEquals(QaStatus.Accepted, it.qaStatus)
            Assertions.assertEquals("Automatically QA approved", it.comment)
        }
    }

    private fun getJsonString(resourceFile: String): String =
        objectMapper
            .readTree(
                this.javaClass.classLoader.getResourceAsStream(resourceFile)
                    ?: throw IllegalArgumentException("Could not load the resource file"),
            ).toString()
}
