package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ServerException
import org.dataland.datalandinternalstorage.openApiClient.model.InsertDataResponse
import org.dataland.datalandinternalstorage.openApiClient.model.Message
import org.dataland.datalandinternalstorage.openApiClient.model.MessageProperties
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import org.springframework.amqp.AmqpException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import org.springframework.amqp.core.Message as AMQPMessage

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DataManagerTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val companyManager: CompanyManager,
) {
    val mockStorageClient: StorageControllerApi = mock(StorageControllerApi::class.java)
    val mockCloudEventMessageHandler: CloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
    val testDataProvider = TestDataProvider(objectMapper)
    val dataManager = DataManager(
        objectMapper, companyManager, dataMetaInformationManager,
        mockStorageClient, mockCloudEventMessageHandler, "dummy-queue-name", "dummy-queue-name",
    )
    val spyDataManager: DataManager = spy(dataManager)
    val correlationId = IdUtils.generateUUID()
    val dataUUId = "JustSomeUUID"

    private fun addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt(): StorableDataSet {
        val companyInformation = testDataProvider.getCompanyInformation(1).first()
        val companyId = companyManager.addCompany(companyInformation).companyId
        val euTaxonomyDataForNonFinancialsAsString = "someEuTaxonomyDataForNonFinancials123"
        return StorableDataSet(
            companyId,
            DataType("eutaxonomy-non-financials"),
            "USER_ID_OF_AN_UPLOADING_USER",
            Instant.now().epochSecond,
            euTaxonomyDataForNonFinancialsAsString,
        )
    }
    private fun buildDummyMessage(storableEuTaxonomyDataSetForNonFinancials: StorableDataSet): Message {
        val storableNFEuTaxonomyDataSetAsString: String =
            objectMapper.writeValueAsString(storableEuTaxonomyDataSetForNonFinancials)
        return Message(
            messageProperties = MessageProperties(correlationId = correlationId),
            body = listOf(storableNFEuTaxonomyDataSetAsString.toByteArray()),
        )
    }

    @Test
    fun `check that a Server Exception is thrown when the data storage reports a Server Exception during selection`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val messageForDataSetAndCorrelationId = buildDummyMessage(storableEuTaxonomyDataSetForNonFinancials)
        `when`(mockStorageClient.insertData(messageForDataSetAndCorrelationId)).thenReturn(
            InsertDataResponse(dataUUId),
        )
        val dataId = dataManager.addDataSetToTemporaryStorageAndSendRequestQAMessage(
            storableEuTaxonomyDataSetForNonFinancials, correlationId,
        )
        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenThrow(ServerException::class.java)
        assertThrows<ServerException> {
            dataManager.getDataSet(
                dataId, DataType(storableEuTaxonomyDataSetForNonFinancials.dataType.name),
                correlationId,
            )
        }
    }

    @Test
    fun `check that an exception is thrown when non matching dataId to dataType pair is requested from data storage`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val messageForDataSetAndCorrelationId = buildDummyMessage(storableEuTaxonomyDataSetForNonFinancials)
        `when`(mockStorageClient.insertData(messageForDataSetAndCorrelationId)).thenReturn(
            InsertDataResponse(dataUUId),
        )
        val dataId = dataManager.addDataSetToTemporaryStorageAndSendRequestQAMessage(
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
        val messageForDataSetAndCorrelationId = buildDummyMessage(storableEuTaxonomyDataSetForNonFinancials)
        `when`(mockStorageClient.insertData(messageForDataSetAndCorrelationId)).thenReturn(
            InsertDataResponse(dataUUId),
        )
        val dataId = dataManager.addDataSetToTemporaryStorageAndSendRequestQAMessage(
            storableEuTaxonomyDataSetForNonFinancials, correlationId,
        )
        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn("")
        val thrown = assertThrows<ResourceNotFoundApiException> {
            dataManager.getDataSet(dataId, DataType("eutaxonomy-non-financials"), correlationId)
        }
        assertEquals("No dataset with the id: $dataId could be found in the data store.", thrown.message)
    }

    @Test
    fun `check that an exception is thrown if the received data from the data storage has an unexpected type`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val messageForDataSetAndCorrelationId = buildDummyMessage(storableEuTaxonomyDataSetForNonFinancials)
        `when`(mockStorageClient.insertData(messageForDataSetAndCorrelationId)).thenReturn(
            InsertDataResponse(dataUUId),
        )
        val dataId = dataManager.addDataSetToTemporaryStorageAndSendRequestQAMessage(
            storableEuTaxonomyDataSetForNonFinancials, correlationId,
        )
        val expectedDataTypeName = getExpectedDataTypeName(
            storableEuTaxonomyDataSetForNonFinancials, dataId, "eutaxonomy-financials",
        )
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
        val messageForDataSetAndCorrelationId = buildDummyMessage(storableDataSetForNonFinancials)
        `when`(mockStorageClient.insertData(messageForDataSetAndCorrelationId)).thenReturn(
            InsertDataResponse(dataUUId),
        )
        val dataId = dataManager.addDataSetToTemporaryStorageAndSendRequestQAMessage(
            storableDataSetForNonFinancials,
            correlationId,
        )

        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
            buildReturnOfMockDataSelect(storableDataSetForNonFinancials),
        )

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
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val storableNFEuTaxonomyDataSetAsString: String =
            objectMapper.writeValueAsString(storableEuTaxonomyDataSetForNonFinancials)
        val thrown = assertThrows<InternalServerErrorApiException> {
            dataManager.listenToMessageQueueAndUpdateMetaDataAfterQA(
                AMQPMessage(
                    storableNFEuTaxonomyDataSetAsString.toByteArray(),
                ),
            )
        }
        assertEquals("The update of the metadataset failed", thrown.publicMessage)
    }

    @Test
    fun `check an exception is thrown in logging of stored data when dataId is empty`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val storableNFEuTaxonomyDataSetAsString: String =
            objectMapper.writeValueAsString(storableEuTaxonomyDataSetForNonFinancials)
        val thrown = assertThrows<InternalServerErrorApiException> {
            dataManager.listenToStoredQueueAndRemoveStoredItemFromTemporaryStore(
                AMQPMessage(
                    storableNFEuTaxonomyDataSetAsString.toByteArray(),
                ),
            )
        }
        assertEquals("The storing of the dataset failed", thrown.publicMessage)
    }

    @Test
    fun `check an exception is thrown during storing a data set when sending notification to message queue fails`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val company = companyManager.getCompanyById(storableEuTaxonomyDataSetForNonFinancials.companyId)

        `when`(spyDataManager.generateRandomDataId()).thenReturn(dataUUId)

        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                dataUUId, "Data to be stored", correlationId, "storage_queue",
            ),
        ).thenThrow(
            AmqpException::class.java,
        )

        assertThrows<AmqpException> {
            spyDataManager.storeDataSetInTemporaryStoreAndSendDataReceivedMessage(
                storableEuTaxonomyDataSetForNonFinancials, company.companyName, correlationId,
            )
        }
    }

    @Test
    fun `check an exception is thrown during adding a data set when sending notification to message queue fails`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val company = companyManager.getCompanyById(storableEuTaxonomyDataSetForNonFinancials.companyId)
        `when`(
            spyDataManager.storeDataSetInTemporaryStoreAndSendDataReceivedMessage(
                storableEuTaxonomyDataSetForNonFinancials, company.companyName, correlationId,
            ),
        ).thenReturn(dataUUId)
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                dataUUId, "New data - QA necessary", correlationId, "upload_queue",
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        assertThrows<AmqpException> {
            spyDataManager.addDataSetToTemporaryStorageAndSendRequestQAMessage(
                storableEuTaxonomyDataSetForNonFinancials, correlationId,
            )
        }
    }
}
