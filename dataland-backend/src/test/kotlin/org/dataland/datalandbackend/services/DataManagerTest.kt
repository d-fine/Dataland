package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import org.springframework.amqp.AmqpException
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import java.time.Instant

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DataManagerTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val companyQueryManager: CompanyQueryManager,
    @Autowired val companyAlterationManager: CompanyAlterationManager,
    @Autowired val dataManagerUtils: DataManagerUtils,
    @Autowired val companyRoleChecker: CompanyRoleChecker,
) {
    val mockStorageClient: StorageControllerApi = mock(StorageControllerApi::class.java)
    val mockCloudEventMessageHandler: CloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
    val testDataProvider = TestDataProvider(objectMapper)
    lateinit var dataManager: DataManager
    lateinit var spyDataManager: DataManager
    lateinit var messageQueueListenerForDataManager: MessageQueueListenerForDataManager
    val correlationId = IdUtils.generateUUID()
    val dataUUID = "JustSomeUUID"

    @BeforeEach
    fun reset() {
        dataManager =
            DataManager(
                objectMapper, companyQueryManager, dataMetaInformationManager,
                mockStorageClient, mockCloudEventMessageHandler, dataManagerUtils, companyRoleChecker,
            )
        spyDataManager = spy(dataManager)
        messageQueueListenerForDataManager =
            MessageQueueListenerForDataManager(
                objectMapper, dataMetaInformationManager,
                MessageQueueUtils(), dataManager,
            )
    }

    private fun addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt(): StorableDataSet {
        val companyInformation = testDataProvider.getCompanyInformation(1).first()
        val companyId = companyAlterationManager.addCompany(companyInformation).companyId
        return StorableDataSet(
            companyId,
            DataType("eutaxonomy-non-financials"),
            "USER_ID_OF_AN_UPLOADING_USER",
            Instant.now().toEpochMilli(),
            "",
            "someEuTaxonomyDataForNonFinancials123",
        )
    }

    @Test
    fun `check that an exception is thrown when non matching dataId to dataType pair is requested from data storage`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val dataId =
            dataManager.processDataStorageRequest(
                storableEuTaxonomyDataSetForNonFinancials, false, correlationId,
            )
        val thrown =
            assertThrows<InvalidInputApiException> {
                dataManager.getPublicDataSet(dataId, DataType("eutaxonomy-financials"), correlationId)
            }
        assertEquals(
            "The data with the id: $dataId is registered as type eutaxonomy-non-financials by " +
                "Dataland instead of your requested type eutaxonomy-financials.",
            thrown.message,
        )
    }

    @Test
    fun `check that an exception is thrown if the received data from the data storage is empty`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val dataId =
            dataManager.processDataStorageRequest(
                storableEuTaxonomyDataSetForNonFinancials, false, correlationId,
            )
        `when`(mockStorageClient.selectDataById(dataId, correlationId))
            .thenThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))
        messageQueueListenerForDataManager.removeStoredItemFromTemporaryStore(dataId, "", MessageType.DATA_STORED)
        val thrown =
            assertThrows<ResourceNotFoundApiException> {
                dataManager.getPublicDataSet(dataId, DataType("eutaxonomy-non-financials"), correlationId)
            }
        assertEquals("No dataset with the id: $dataId could be found in the data store.", thrown.message)
    }

    @Test
    fun `check that an exception is thrown if the received data from the data storage has an unexpected type`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val dataId =
            dataManager.processDataStorageRequest(
                storableEuTaxonomyDataSetForNonFinancials, false, correlationId,
            )
        val expectedDataTypeName =
            getExpectedDataTypeName(
                storableEuTaxonomyDataSetForNonFinancials, dataId, "eutaxonomy-financials",
            )
        messageQueueListenerForDataManager.removeStoredItemFromTemporaryStore(dataId, "", MessageType.DATA_STORED)
        val thrown =
            assertThrows<InternalServerErrorApiException> {
                dataManager.getPublicDataSet(dataId, DataType(expectedDataTypeName), correlationId)
            }
        assertEquals(
            "The meta-data of dataset $dataId differs between the data store and the database", thrown.message,
        )
    }

    private fun getExpectedDataTypeName(
        storableDataSet: StorableDataSet,
        dataId: String,
        unexpectedDataTypeName: String,
    ): String {
        val expectedDataTypeName = storableDataSet.dataType.name
        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
            objectMapper.writeValueAsString(storableDataSet.copy(dataType = DataType(unexpectedDataTypeName))),
        )
        return expectedDataTypeName
    }

    @Test
    fun `check that an exception is thrown if the received data from the storage has an unexpected uploading user`() {
        val storableDataSetForNonFinancials = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val dataId =
            dataManager.processDataStorageRequest(
                storableDataSetForNonFinancials,
                false,
                correlationId,
            )

        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
            buildReturnOfMockDataSelect(storableDataSetForNonFinancials),
        )

        messageQueueListenerForDataManager.removeStoredItemFromTemporaryStore(dataId, "", MessageType.DATA_STORED)
        val thrown =
            assertThrows<InternalServerErrorApiException> {
                dataManager.getPublicDataSet(dataId, storableDataSetForNonFinancials.dataType, correlationId)
            }
        assertEquals(
            "The meta-data of dataset $dataId differs between the data store and the database", thrown.message,
        )
    }

    private fun buildReturnOfMockDataSelect(storableDataSetForNonFinancials: StorableDataSet): String =
        objectMapper.writeValueAsString(
            storableDataSetForNonFinancials.copy(
                uploaderUserId = "NOT_WHATS_EXPECTED",
            ),
        )

    @Test
    fun `check an exception is thrown in updating of meta data when dataId is empty`() {
        val messageWithEmptyDataID =
            objectMapper.writeValueAsString(
                QaCompletedMessage(
                    identifier = "",
                    validationResult = QaStatus.Accepted,
                    reviewerId = "",
                    message = null,
                ),
            )
        val thrown =
            assertThrows<MessageQueueRejectException> {
                messageQueueListenerForDataManager.updateMetaData(messageWithEmptyDataID, "", MessageType.QA_COMPLETED)
            }
        assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }

    @Test
    fun `check an exception is thrown in logging of stored data when dataId is empty`() {
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                messageQueueListenerForDataManager.removeStoredItemFromTemporaryStore("", "", MessageType.DATA_STORED)
            }
        assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }

    @Test
    fun `check an exception is thrown during storing a data set when sending notification to message queue fails`() {
        val storableEuTaxonomyDataSetForNonFinancials =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()

        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                anyString(), anyString(), anyString(), anyString(), anyString(),
            ),
        ).thenThrow(AmqpException::class.java)
        assertThrows<AmqpException> {
            spyDataManager.storeDataSetInTemporaryStoreAndSendMessage(
                dataUUID, storableEuTaxonomyDataSetForNonFinancials, false, correlationId,
            )
        }
    }

    @Test
    fun `check a ResourceNotFoundApiException if the dataset could not be found`() {
        val mockMetaInfo =
            DataMetaInformationEntity(
                dataId = "i-only-have-meta-info-stored", dataType = "lksg", uploaderUserId = "", uploadTime = 0,
                qaStatus = QaStatus.Pending, company = testDataProvider.getEmptyStoredCompanyEntity(),
                reportingPeriod = "2023", currentlyActive = true,
            )
        val mockDataMetaInformationManager = mock(DataMetaInformationManager::class.java)
        `when`(mockDataMetaInformationManager.getDataMetaInformationByDataId(anyString())).thenReturn(mockMetaInfo)
        `when`(mockStorageClient.selectDataById(anyString(), anyString())).thenThrow(
            ClientException(statusCode = HttpStatus.NOT_FOUND.value()),
        )
        dataManager =
            DataManager(
                objectMapper, companyQueryManager, mockDataMetaInformationManager,
                mockStorageClient, mockCloudEventMessageHandler, dataManagerUtils, companyRoleChecker,
            )
        assertThrows<ResourceNotFoundApiException> {
            dataManager.getPublicDataSet(
                mockMetaInfo.dataId,
                DataType("lksg"), "",
            )
        }
        assertThrows<ResourceNotFoundApiException> {
            dataManager.getPublicDataSet("i-exist-by-no-means", DataType("lksg"), "")
        }
    }

    @Test
    fun `check a MessageQueueRejectException if there does not exist any data for given data Id`() {
        val changedQaStatusDataId = "453545"
        val updatedQaStatus = QaStatus.Accepted
        val currentlyActiveDataId = "1273091"
        val messageWithChangedQAStatus =
            objectMapper.writeValueAsString(
                QaStatusChangeMessage(
                    changedQaStatusDataId,
                    updatedQaStatus,
                    currentlyActiveDataId,
                ),
            )
        val thrown =
            assertThrows<MessageQueueRejectException> {
                messageQueueListenerForDataManager.changeQaStatus(
                    messageWithChangedQAStatus,
                    "",
                    MessageType.QA_STATUS_CHANGED,
                )
            }
        assertEquals(
            "No dataset with the id: $changedQaStatusDataId could be found in the data store.",
            thrown.message,
        )
    }

    @Test
    fun `test that AmqpRejectAndDontRequeueException is thrown if data id for new active dataset is empty`() {
        val messageWithEmptyDataIDs =
            objectMapper.writeValueAsString(
                QaStatusChangeMessage(
                    changedQaStatusDataId = "1273091",
                    updatedQaStatus = QaStatus.Accepted,
                    currentlyActiveDataId = "",
                ),
            )
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                messageQueueListenerForDataManager.changeQaStatus(
                    messageWithEmptyDataIDs,
                    "",
                    MessageType.QA_STATUS_CHANGED,
                )
            }
        assertEquals(
            "Message was rejected: Provided data ID to newly active dataset is empty",
            thrown.message,
        )
    }

    @Test
    fun `test that AmqpRejectAndDontRequeueException is thrown if the data id for changed QA status is empty`() {
        val messageWithEmptyDataIDs =
            objectMapper.writeValueAsString(
                QaStatusChangeMessage(
                    changedQaStatusDataId = "",
                    updatedQaStatus = QaStatus.Accepted,
                    currentlyActiveDataId = "1273091",
                ),
            )
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                messageQueueListenerForDataManager.changeQaStatus(
                    messageWithEmptyDataIDs,
                    "",
                    MessageType.QA_STATUS_CHANGED,
                )
            }
        assertEquals(
            "Message was rejected: Provided data ID to change qa status dataset is empty",
            thrown.message,
        )
    }

    @Test
    fun `test changing the QA status of an existing dataset to Accepted and making it active does not throw error`() {
        val newQaStatus = QaStatus.Accepted
        val storableDataSetForNonFinancials = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val dataId =
            dataManager.processDataStorageRequest(
                storableDataSetForNonFinancials,
                false,
                correlationId,
            )

        val messageWithChangedQAStatus =
            objectMapper.writeValueAsString(
                QaStatusChangeMessage(
                    dataId,
                    newQaStatus,
                    dataId,
                ),
            )
        assertDoesNotThrow {
            messageQueueListenerForDataManager.changeQaStatus(messageWithChangedQAStatus, "", MessageType.QA_STATUS_CHANGED)
        }
    }
}
