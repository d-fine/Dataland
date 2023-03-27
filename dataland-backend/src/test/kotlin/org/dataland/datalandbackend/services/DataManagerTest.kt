package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.enums.data.QAStatus
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import java.time.Instant

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DataManagerTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val companyManager: CompanyManager,
    @Autowired var messageUtils: MessageQueueUtils,
) {
    val mockStorageClient: StorageControllerApi = mock(StorageControllerApi::class.java)
    val mockCloudEventMessageHandler: CloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
    val testDataProvider = TestDataProvider(objectMapper)
    lateinit var dataManager: DataManager
    lateinit var spyDataManager: DataManager
    val correlationId = IdUtils.generateUUID()
    val dataUUId = "JustSomeUUID"

    @BeforeEach
    fun reset() {
        dataManager = DataManager(
            objectMapper, companyManager, dataMetaInformationManager,
            mockStorageClient, mockCloudEventMessageHandler, messageUtils,
        )
        spyDataManager = spy(dataManager)
    }

    private fun addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt(): StorableDataSet {
        val companyInformation = testDataProvider.getCompanyInformation(1).first()
        val companyId = companyManager.addCompany(companyInformation).companyId
        val euTaxonomyDataForNonFinancialsAsString = "someEuTaxonomyDataForNonFinancials123"
        return StorableDataSet(
            companyId,
            DataType("eutaxonomy-non-financials"),
            "USER_ID_OF_AN_UPLOADING_USER",
            Instant.now().toEpochMilli(),
            "",
            euTaxonomyDataForNonFinancialsAsString,
        )
    }

    @Test
    fun `check that an exception is thrown when non matching dataId to dataType pair is requested from data storage`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val dataId = dataManager.addDataSetToTemporaryStorageAndSendMessage(
            storableEuTaxonomyDataSetForNonFinancials, correlationId,
        )
        val thrown = assertThrows<InvalidInputApiException> {
            dataManager.getDataSet(dataId, DataType("eutaxonomy-financials"), correlationId)
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
        val dataId = dataManager.addDataSetToTemporaryStorageAndSendMessage(
            storableEuTaxonomyDataSetForNonFinancials, correlationId,
        )
        `when`(mockStorageClient.selectDataById(dataId, correlationId))
            .thenThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))
        dataManager.removeStoredItemFromTemporaryStore(dataId, "", MessageType.DataStored)
        val thrown = assertThrows<ResourceNotFoundApiException> {
            dataManager.getDataSet(dataId, DataType("eutaxonomy-non-financials"), correlationId)
        }
        assertEquals("No dataset with the id: $dataId could be found in the data store.", thrown.message)
    }

    @Test
    fun `check that an exception is thrown if the received data from the data storage has an unexpected type`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val dataId = dataManager.addDataSetToTemporaryStorageAndSendMessage(
            storableEuTaxonomyDataSetForNonFinancials, correlationId,
        )
        val expectedDataTypeName = getExpectedDataTypeName(
            storableEuTaxonomyDataSetForNonFinancials, dataId, "eutaxonomy-financials",
        )
        dataManager.removeStoredItemFromTemporaryStore(dataId, "", MessageType.DataStored)
        val thrown = assertThrows<InternalServerErrorApiException> {
            dataManager.getDataSet(dataId, DataType(expectedDataTypeName), correlationId)
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
        val dataId = dataManager.addDataSetToTemporaryStorageAndSendMessage(
            storableDataSetForNonFinancials,
            correlationId,
        )

        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
            buildReturnOfMockDataSelect(storableDataSetForNonFinancials),
        )

        dataManager.removeStoredItemFromTemporaryStore(dataId, "", MessageType.DataStored)
        val thrown = assertThrows<InternalServerErrorApiException> {
            dataManager.getDataSet(dataId, storableDataSetForNonFinancials.dataType, correlationId)
        }
        assertEquals(
            "The meta-data of dataset $dataId differs between the data store and the database", thrown.message,
        )
    }
    private fun buildReturnOfMockDataSelect(storableDataSetForNonFinancials: StorableDataSet): String {
        return objectMapper.writeValueAsString(
            storableDataSetForNonFinancials.copy(
                uploaderUserId = "NOT_WHATS_EXPECTED",
            ),
        )
    }

    @Test
    fun `check an exception is thrown in updating of meta data when dataId is empty`() {
        val messageWithEmptyDataID = objectMapper.writeValueAsString(
            QaCompletedMessage(
                dataId = "",
                validationResult = "By default, QA is passed",
            ),
        )
        val thrown = assertThrows<MessageQueueRejectException> {
            dataManager.updateMetaData(messageWithEmptyDataID, "", MessageType.QACompleted)
        }
        assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }

    @Test
    fun `check an exception is thrown in logging of stored data when dataId is empty`() {
        val thrown = assertThrows<AmqpRejectAndDontRequeueException> {
            dataManager.removeStoredItemFromTemporaryStore("", "", MessageType.DataStored)
        }
        assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }

    @Test
    fun `check an exception is thrown during storing a data set when sending notification to message queue fails`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()

        `when`(spyDataManager.generateRandomDataId()).thenReturn(dataUUId)

        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                dataUUId, MessageType.DataReceived, correlationId, ExchangeNames.dataReceived,
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        assertThrows<AmqpException> {
            spyDataManager.storeDataSetInTemporaryStoreAndSendMessage(
                dataUUId, storableEuTaxonomyDataSetForNonFinancials, correlationId,
            )
        }
    }

    @Test
    fun `check a ResourceNotFoundApiException if the dataset could not be found`() {
        val mockMetaInfo = DataMetaInformationEntity(
            dataId = "i-only-have-meta-info-stored", dataType = "lksg", uploaderUserId = "", uploadTime = 0,
            qaStatus = QAStatus.Pending, company = testDataProvider.getEmptyStoredCompanyEntity(),
            reportingPeriod = "2023", currentlyActive = true,
        )
        val mockDataMetaInformationManager = mock(DataMetaInformationManager::class.java)
        `when`(mockDataMetaInformationManager.getDataMetaInformationByDataId(anyString())).thenReturn(mockMetaInfo)
        `when`(mockStorageClient.selectDataById(anyString(), anyString())).thenThrow(
            ClientException(statusCode = HttpStatus.NOT_FOUND.value()),
        )
        dataManager = DataManager(
            objectMapper, companyManager, mockDataMetaInformationManager,
            mockStorageClient, mockCloudEventMessageHandler, messageUtils,
        )
        assertThrows<ResourceNotFoundApiException> { dataManager.getDataSet(mockMetaInfo.dataId, DataType("lksg"), "") }
        assertThrows<ResourceNotFoundApiException> {
            dataManager.getDataSet("i-exist-by-no-means", DataType("lksg"), "")
        }
    }
}
