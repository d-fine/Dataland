package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.messages.data.DataIdPayload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyString
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
@Suppress("LongParameterList")
class DataManagerTest(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val companyQueryManager: CompanyQueryManager,
    @Autowired val companyAlterationManager: CompanyAlterationManager,
    @Autowired val dataManagerUtils: DataManagerUtils,
    @Autowired val companyRoleChecker: CompanyRoleChecker,
    @Autowired val nonSourceableDataManager: NonSourceableDataManager,
) {
    val mockStorageClient: StorageControllerApi = mock<StorageControllerApi>()
    val mockMessageQueuePublications: MessageQueuePublications = mock<MessageQueuePublications>()
    val testDataProvider = TestDataProvider(objectMapper)
    lateinit var dataManager: DataManager
    lateinit var spyDataManager: DataManager
    lateinit var messageQueueListenerForDataManager: MessageQueueListenerForDataManager
    val correlationId = IdUtils.generateUUID()
    val dataUUID = "JustSomeUUID"

    @BeforeEach
    fun setup() {
        reset(mockStorageClient, mockMessageQueuePublications)
        dataManager =
            DataManager(
                objectMapper, companyQueryManager, dataMetaInformationManager,
                mockStorageClient, dataManagerUtils, companyRoleChecker, mockMessageQueuePublications,
            )
        spyDataManager = spy(dataManager)
        messageQueueListenerForDataManager =
            MessageQueueListenerForDataManager(
                objectMapper, dataMetaInformationManager,
                dataManager, nonSourceableDataManager,
            )
    }

    private fun addCompanyAndReturnStorableEuTaxonomyDatasetForNonFinancialsForIt(): StorableDataset {
        val companyInformation = testDataProvider.getCompanyInformation(1).first()
        val companyId = companyAlterationManager.addCompany(companyInformation).companyId
        return StorableDataset(
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
        val euTaxonomyNonFinancialDataset = addCompanyAndReturnStorableEuTaxonomyDatasetForNonFinancialsForIt()
        val dataId = dataManager.storeDataset(euTaxonomyNonFinancialDataset, false, correlationId)
        val thrown =
            assertThrows<InvalidInputApiException> {
                dataManager.getPublicDataset(dataId, DataType("eutaxonomy-financials"), correlationId)
            }
        assertEquals(
            "The data with the id: $dataId is registered as type eutaxonomy-non-financials by " +
                "Dataland instead of your requested type eutaxonomy-financials.",
            thrown.message,
        )
    }

    @Test
    fun `check that an exception is thrown if the received data from the data storage is empty`() {
        val euTaxonomyNonFinancialDataset = addCompanyAndReturnStorableEuTaxonomyDatasetForNonFinancialsForIt()
        val dataId = dataManager.storeDataset(euTaxonomyNonFinancialDataset, false, correlationId)
        `when`(mockStorageClient.selectDataById(dataId, correlationId))
            .thenThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))
        messageQueueListenerForDataManager.removeStoredItemFromTemporaryStore(
            objectMapper.writeValueAsString(DataIdPayload(dataId)), "", MessageType.DATA_STORED,
        )
        val thrown =
            assertThrows<ResourceNotFoundApiException> {
                dataManager.getPublicDataset(dataId, DataType("eutaxonomy-non-financials"), correlationId)
            }
        assertEquals("No dataset with the id: $dataId could be found in the data store.", thrown.message)
    }

    @Test
    fun `check that an exception is thrown if the received data from the data storage has an unexpected type`() {
        val euTaxonomyNonFinancialDataset = addCompanyAndReturnStorableEuTaxonomyDatasetForNonFinancialsForIt()
        val dataId = dataManager.storeDataset(euTaxonomyNonFinancialDataset, false, correlationId)
        val expectedDataTypeName = euTaxonomyNonFinancialDataset.dataType.name
        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
            objectMapper.writeValueAsString(euTaxonomyNonFinancialDataset.copy(dataType = DataType("eutaxonomy-financials"))),
        )
        messageQueueListenerForDataManager.removeStoredItemFromTemporaryStore(
            objectMapper.writeValueAsString(DataIdPayload(dataId)), "", MessageType.DATA_STORED,
        )
        val thrown =
            assertThrows<InternalServerErrorApiException> {
                dataManager.getPublicDataset(dataId, DataType(expectedDataTypeName), correlationId)
            }
        assertEquals(
            "The meta-data of dataset $dataId differs between the data store and the database", thrown.message,
        )
    }

    @Test
    fun `check that an exception is thrown if the received data from the storage has an unexpected uploading user`() {
        val storableDatasetForNonFinancials = addCompanyAndReturnStorableEuTaxonomyDatasetForNonFinancialsForIt()
        val dataId =
            dataManager.storeDataset(
                storableDatasetForNonFinancials,
                false,
                correlationId,
            )

        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
            buildReturnOfMockDataSelect(storableDatasetForNonFinancials),
        )

        messageQueueListenerForDataManager.removeStoredItemFromTemporaryStore(
            objectMapper.writeValueAsString(DataIdPayload(dataId)), "", MessageType.DATA_STORED,
        )
        val thrown =
            assertThrows<InternalServerErrorApiException> {
                dataManager.getPublicDataset(dataId, storableDatasetForNonFinancials.dataType, correlationId)
            }
        assertEquals(
            "The meta-data of dataset $dataId differs between the data store and the database", thrown.message,
        )
    }

    private fun buildReturnOfMockDataSelect(storableDatasetForNonFinancials: StorableDataset): String =
        objectMapper.writeValueAsString(
            storableDatasetForNonFinancials.copy(
                uploaderUserId = "NOT_WHATS_EXPECTED",
            ),
        )

    @Test
    fun `check an exception is thrown in logging of stored data when dataId is empty`() {
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                messageQueueListenerForDataManager.removeStoredItemFromTemporaryStore(
                    objectMapper.writeValueAsString(DataIdPayload("")), "", MessageType.DATA_STORED,
                )
            }
        assertEquals("Invalid UUID string: ", thrown.message)
    }

    @Test
    fun `check an exception is thrown during storing a dataset when sending notification to message queue fails`() {
        val storableEuTaxonomyDatasetForNonFinancials =
            addCompanyAndReturnStorableEuTaxonomyDatasetForNonFinancialsForIt()

        `when`(
            mockMessageQueuePublications.publishDatasetUploadedMessage(
                anyString(), anyBoolean(), anyString(),
            ),
        ).thenThrow(AmqpException::class.java)
        assertThrows<AmqpException> {
            spyDataManager.storeDatasetInTemporaryStoreAndSendUploadMessage(
                dataUUID, storableEuTaxonomyDatasetForNonFinancials, false, correlationId,
            )
        }
    }

    @Test
    fun `check that processing a patch event works as expected`() {
        val storableEuTaxonomyDatasetForNonFinancials =
            addCompanyAndReturnStorableEuTaxonomyDatasetForNonFinancialsForIt()

        doNothing().whenever(mockMessageQueuePublications).publishDatasetMetaInfoPatchMessage(any(), any(), anyString())

        assertDoesNotThrow {
            spyDataManager.storeDatasetInTemporaryStoreAndSendPatchMessage(
                dataUUID, storableEuTaxonomyDatasetForNonFinancials, correlationId,
            )
        }

        verify(mockMessageQueuePublications, times(1))
            .publishDatasetMetaInfoPatchMessage(
                dataUUID,
                storableEuTaxonomyDatasetForNonFinancials.uploaderUserId,
                correlationId,
            )
    }

    @Test
    fun `check a ResourceNotFoundApiException if the dataset could not be found`() {
        val mockMetaInfo =
            DataMetaInformationEntity(
                dataId = "i-only-have-meta-info-stored", dataType = "lksg", uploaderUserId = "", uploadTime = 0,
                qaStatus = QaStatus.Pending, company = testDataProvider.getEmptyStoredCompanyEntity(),
                reportingPeriod = "2023", currentlyActive = true,
            )
        val mockDataMetaInformationManager = mock<DataMetaInformationManager>()
        `when`(mockDataMetaInformationManager.getDataMetaInformationByDataId(anyString())).thenReturn(mockMetaInfo)
        `when`(mockStorageClient.selectDataById(anyString(), anyString())).thenThrow(
            ClientException(statusCode = HttpStatus.NOT_FOUND.value()),
        )
        dataManager =
            DataManager(
                objectMapper, companyQueryManager, mockDataMetaInformationManager,
                mockStorageClient, dataManagerUtils, companyRoleChecker, mockMessageQueuePublications,
            )
        assertThrows<ResourceNotFoundApiException> {
            dataManager.getPublicDataset(mockMetaInfo.dataId, DataType("lksg"), "")
        }
        assertThrows<ResourceNotFoundApiException> {
            dataManager.getPublicDataset("i-exist-by-no-means", DataType("lksg"), "")
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
                    MessageType.QA_STATUS_UPDATED,
                )
            }
        assertEquals(
            "No dataset with the id: $changedQaStatusDataId could be found in the data store.",
            thrown.message,
        )
    }

    @Test
    fun `test that data id for new active dataset can be empty`() {
        val storableEuTaxonomyDatasetForNonFinancials: StorableDataset =
            addCompanyAndReturnStorableEuTaxonomyDatasetForNonFinancialsForIt()
        val dataId =
            dataManager.storeDataset(
                storableEuTaxonomyDatasetForNonFinancials, false, correlationId,
            )
        val messageWithEmptyCurrentlyActiveDataId =
            objectMapper.writeValueAsString(
                QaStatusChangeMessage(
                    dataId = dataId,
                    updatedQaStatus = QaStatus.Accepted,
                    currentlyActiveDataId = null,
                ),
            )

        assertDoesNotThrow {
            messageQueueListenerForDataManager.changeQaStatus(
                messageWithEmptyCurrentlyActiveDataId,
                "",
                MessageType.QA_STATUS_UPDATED,
            )
        }
    }

    @Test
    fun `test that AmqpRejectAndDontRequeueException is thrown if the data id for changed QA status is empty`() {
        val messageWithEmptyDataIDs =
            objectMapper.writeValueAsString(
                QaStatusChangeMessage(
                    dataId = "",
                    updatedQaStatus = QaStatus.Accepted,
                    currentlyActiveDataId = "1273091",
                ),
            )
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                messageQueueListenerForDataManager.changeQaStatus(
                    messageWithEmptyDataIDs,
                    "",
                    MessageType.QA_STATUS_UPDATED,
                )
            }
        assertEquals(
            "Message was rejected: Provided data ID to change qa status dataset is empty",
            thrown.message,
        )
    }

    private fun createNewDataMetaInformationWithQaStatus(
        dataId: String,
        qaStatus: QaStatus,
    ) {
        val companyInformation = testDataProvider.getCompanyInformationWithoutIdentifiers(1).first()
        val company: StoredCompanyEntity = companyAlterationManager.addCompany(companyInformation)

        val datasetToBeUpdated =
            DataMetaInformationEntity(
                dataId = dataId,
                company = company,
                dataType = "sfdr",
                uploadTime = Instant.now().toEpochMilli(),
                uploaderUserId = "dummyUserId",
                qaStatus = qaStatus,
                reportingPeriod = "2023",
                currentlyActive = false,
            )

        this.dataMetaInformationManager.storeDataMetaInformation(datasetToBeUpdated)
    }

    @Test
    fun `test changing the QA status of a pending dataset to Accepted and setting it to active`() {
        val dataId = "someDataId"
        val newQaStatus = QaStatus.Accepted

        createNewDataMetaInformationWithQaStatus(dataId, QaStatus.Pending)

        val messageWithChangedQAStatus =
            objectMapper.writeValueAsString(
                QaStatusChangeMessage(dataId, newQaStatus, dataId),
            )
        assertDoesNotThrow {
            messageQueueListenerForDataManager.changeQaStatus(
                messageWithChangedQAStatus,
                "",
                MessageType.QA_STATUS_UPDATED,
            )
        }

        val updatedDataset = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)

        assertEquals(QaStatus.Accepted, updatedDataset.qaStatus)
        assertNotNull(updatedDataset.currentlyActive)
        assertEquals(true, updatedDataset.currentlyActive)
    }

    @Test
    fun `test rejecting an accepted dataset and setting it to inactive and setting a second dataset to active`() {
        val oldDataId = "oldDatasetDataId"
        val newDataId = "newDatasetDataId"

        createNewDataMetaInformationWithQaStatus(oldDataId, QaStatus.Accepted)
        createNewDataMetaInformationWithQaStatus(newDataId, QaStatus.Accepted)

        val messageWithChangedQAStatus =
            objectMapper.writeValueAsString(
                QaStatusChangeMessage(oldDataId, QaStatus.Rejected, newDataId),
            )
        assertDoesNotThrow {
            messageQueueListenerForDataManager.changeQaStatus(
                messageWithChangedQAStatus,
                "",
                MessageType.QA_STATUS_UPDATED,
            )
        }

        val rejectedInactiveDataset = dataMetaInformationManager.getDataMetaInformationByDataId(oldDataId)
        val acceptedActiveDataset = dataMetaInformationManager.getDataMetaInformationByDataId(newDataId)

        assertEquals(QaStatus.Rejected, rejectedInactiveDataset.qaStatus)
        assertEquals(false, rejectedInactiveDataset.currentlyActive)
        assertEquals(QaStatus.Accepted, acceptedActiveDataset.qaStatus)
        assertEquals(true, acceptedActiveDataset.currentlyActive)
    }
}
