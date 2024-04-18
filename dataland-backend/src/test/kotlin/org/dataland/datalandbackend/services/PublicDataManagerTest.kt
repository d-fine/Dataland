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
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.json.JSONObject
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

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class PublicDataManagerTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val companyQueryManager: CompanyQueryManager,
    @Autowired val companyAlterationManager: CompanyAlterationManager,
    @Autowired var messageUtils: MessageQueueUtils,
) {
    val mockStorageClient: StorageControllerApi = mock(StorageControllerApi::class.java)
    val mockCloudEventMessageHandler: CloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
    val testDataProvider = TestDataProvider(objectMapper)
    lateinit var publicDataManager: PublicDataManager
    lateinit var spyPublicDataManager: PublicDataManager
    val correlationId = IdUtils.generateUUID()
    val dataUUId = "JustSomeUUID"

    @BeforeEach
    fun reset() {
        publicDataManager = PublicDataManager(
            objectMapper, companyQueryManager, dataMetaInformationManager,
            mockStorageClient, mockCloudEventMessageHandler, messageUtils,
        )
        spyPublicDataManager = spy(publicDataManager)
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
        val dataId = publicDataManager.storeDataSetInMemoryAndSendReceptionMessageAndPersistMetaInfo(
            storableEuTaxonomyDataSetForNonFinancials, false, correlationId,
        )
        val thrown = assertThrows<InvalidInputApiException> {
            publicDataManager.getDataSet(dataId, DataType("eutaxonomy-financials"), correlationId)
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
        val dataId = publicDataManager.storeDataSetInMemoryAndSendReceptionMessageAndPersistMetaInfo(
            storableEuTaxonomyDataSetForNonFinancials, false, correlationId,
        )
        `when`(mockStorageClient.selectDataById(dataId, correlationId))
            .thenThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))
        publicDataManager.removeStoredItemFromTemporaryStore(dataId, "", MessageType.DataStored)
        val thrown = assertThrows<ResourceNotFoundApiException> {
            publicDataManager.getDataSet(dataId, DataType("eutaxonomy-non-financials"), correlationId)
        }
        assertEquals("No dataset with the id: $dataId could be found in the data store.", thrown.message)
    }

    @Test
    fun `check that an exception is thrown if the received data from the data storage has an unexpected type`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        val dataId = publicDataManager.storeDataSetInMemoryAndSendReceptionMessageAndPersistMetaInfo(
            storableEuTaxonomyDataSetForNonFinancials, false, correlationId,
        )
        val expectedDataTypeName = getExpectedDataTypeName(
            storableEuTaxonomyDataSetForNonFinancials, dataId, "eutaxonomy-financials",
        )
        publicDataManager.removeStoredItemFromTemporaryStore(dataId, "", MessageType.DataStored)
        val thrown = assertThrows<InternalServerErrorApiException> {
            publicDataManager.getDataSet(dataId, DataType(expectedDataTypeName), correlationId)
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
        val dataId = publicDataManager.storeDataSetInMemoryAndSendReceptionMessageAndPersistMetaInfo(
            storableDataSetForNonFinancials,
            false,
            correlationId,
        )

        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
            buildReturnOfMockDataSelect(storableDataSetForNonFinancials),
        )

        publicDataManager.removeStoredItemFromTemporaryStore(dataId, "", MessageType.DataStored)
        val thrown = assertThrows<InternalServerErrorApiException> {
            publicDataManager.getDataSet(dataId, storableDataSetForNonFinancials.dataType, correlationId)
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
                identifier = "",
                validationResult = QaStatus.Accepted,
            ),
        )
        val thrown = assertThrows<MessageQueueRejectException> {
            publicDataManager.updateMetaData(messageWithEmptyDataID, "", MessageType.QaCompleted)
        }
        assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }

    @Test
    fun `check an exception is thrown in logging of stored data when dataId is empty`() {
        val thrown = assertThrows<AmqpRejectAndDontRequeueException> {
            publicDataManager.removeStoredItemFromTemporaryStore("", "", MessageType.DataStored)
        }
        assertEquals("Message was rejected: Provided data ID is empty", thrown.message)
    }

    @Test
    fun `check an exception is thrown during storing a data set when sending notification to message queue fails`() {
        val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
            addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
        // TODO check that its fine to delete this
        // `when`(spyDataManager.generateRandomDataId()).thenReturn(dataUUId)

        val payload = JSONObject(
            mapOf(
                "dataId" to dataUUId, "bypassQa" to false,
                "actionType" to
                    ActionType.StorePublicData,
            ),
        ).toString()
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                payload, MessageType.PublicDataReceived, correlationId, ExchangeName.RequestReceived,
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        assertThrows<AmqpException> {
            spyPublicDataManager.storeDataSetInTemporaryStoreAndSendMessage(
                dataUUId, storableEuTaxonomyDataSetForNonFinancials, false, correlationId,
            )
        }
    }

    @Test
    fun `check a ResourceNotFoundApiException if the dataset could not be found`() {
        val mockMetaInfo = DataMetaInformationEntity(
            dataId = "i-only-have-meta-info-stored", dataType = "lksg", uploaderUserId = "", uploadTime = 0,
            qaStatus = QaStatus.Pending, company = testDataProvider.getEmptyStoredCompanyEntity(),
            reportingPeriod = "2023", currentlyActive = true,
        )
        val mockDataMetaInformationManager = mock(DataMetaInformationManager::class.java)
        `when`(mockDataMetaInformationManager.getDataMetaInformationByDataId(anyString())).thenReturn(mockMetaInfo)
        `when`(mockStorageClient.selectDataById(anyString(), anyString())).thenThrow(
            ClientException(statusCode = HttpStatus.NOT_FOUND.value()),
        )
        publicDataManager = PublicDataManager(
            objectMapper, companyQueryManager, mockDataMetaInformationManager,
            mockStorageClient, mockCloudEventMessageHandler, messageUtils,
        )
        assertThrows<ResourceNotFoundApiException> { publicDataManager.getDataSet(mockMetaInfo.dataId, DataType("lksg"), "") }
        assertThrows<ResourceNotFoundApiException> {
            publicDataManager.getDataSet("i-exist-by-no-means", DataType("lksg"), "")
        }
    }
}
