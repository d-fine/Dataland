package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.messages.data.DataIdPayload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.springframework.amqp.AmqpException
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode

/**
 * A class for testing cases where DataManager should throw an exception.
 */
@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@Suppress("LongParameterList")
class DataManagerExceptionTest
    @Autowired
    constructor(
        objectMapper: ObjectMapper,
        dataMetaInformationManager: DataMetaInformationManager,
        companyQueryManager: CompanyQueryManager,
        dataManagerUtils: DataManagerUtils,
        sourceabilityDataManager: SourceabilityDataManager,
        val companyAlterationManager: CompanyAlterationManager,
        val cloudEventsMessageHandler: CloudEventMessageHandler,
    ) : DataManagerTest(
            objectMapper = objectMapper,
            dataMetaInformationManager = dataMetaInformationManager,
            companyQueryManager = companyQueryManager,
            dataManagerUtils = dataManagerUtils,
            sourceabilityDataManager = sourceabilityDataManager,
        ) {
        @BeforeEach
        override fun setup() {
            super.setup()
        }

        private fun getSingleDataStoredMessage(dataId: String): List<Message> =
            listOf(
                cloudEventsMessageHandler.buildCEMessage(
                    objectMapper.writeValueAsString(DataIdPayload(dataId)),
                    MessageType.DATA_STORED, "",
                ),
            )

        @Test
        fun `check that an exception is thrown when non matching dataId to dataType pair is requested from data storage`() {
            val euTaxonomyNonFinancialDataset =
                testDataProvider.addCompanyAndReturnStorableDatasetForIt(
                    companyAlterationManager, euTaxonomyNonFinancialsFrameworkName,
                )
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
            val euTaxonomyNonFinancialDataset =
                testDataProvider.addCompanyAndReturnStorableDatasetForIt(
                    companyAlterationManager, euTaxonomyNonFinancialsFrameworkName,
                )
            val dataId = dataManager.storeDataset(euTaxonomyNonFinancialDataset, false, correlationId)
            `when`(mockStorageClient.selectDataById(dataId, correlationId))
                .thenThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))
            messageQueueListenerForDataManager.removeStoredItemsFromTemporaryStore(
                getSingleDataStoredMessage(dataId),
            )
            val thrown =
                assertThrows<ResourceNotFoundApiException> {
                    dataManager.getPublicDataset(dataId, DataType(euTaxonomyNonFinancialsFrameworkName), correlationId)
                }
            assertEquals("No dataset with the id: $dataId could be found in the data store.", thrown.message)
        }

        @Test
        fun `check that an exception is thrown if the received data from the data storage has an unexpected type`() {
            val euTaxonomyNonFinancialDataset =
                testDataProvider.addCompanyAndReturnStorableDatasetForIt(
                    companyAlterationManager, euTaxonomyNonFinancialsFrameworkName,
                )
            val dataId = dataManager.storeDataset(euTaxonomyNonFinancialDataset, false, correlationId)
            val expectedDataTypeName = euTaxonomyNonFinancialDataset.dataType.name
            `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
                objectMapper.writeValueAsString(euTaxonomyNonFinancialDataset.copy(dataType = DataType("eutaxonomy-financials"))),
            )
            messageQueueListenerForDataManager.removeStoredItemsFromTemporaryStore(
                getSingleDataStoredMessage(dataId),
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
            val storableDatasetForNonFinancials =
                testDataProvider.addCompanyAndReturnStorableDatasetForIt(
                    companyAlterationManager, euTaxonomyNonFinancialsFrameworkName,
                )
            val dataId =
                dataManager.storeDataset(
                    storableDatasetForNonFinancials,
                    false,
                    correlationId,
                )

            `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
                buildReturnOfMockDataSelect(storableDatasetForNonFinancials),
            )

            messageQueueListenerForDataManager.removeStoredItemsFromTemporaryStore(
                getSingleDataStoredMessage(dataId),
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
                    messageQueueListenerForDataManager.removeStoredItemsFromTemporaryStore(
                        getSingleDataStoredMessage(""),
                    )
                }
            assertEquals("Invalid UUID string: ", thrown.message)
        }

        @Test
        fun `check an exception is thrown during storing a dataset when sending notification to message queue fails`() {
            val storableEuTaxonomyDatasetForNonFinancials =
                testDataProvider.addCompanyAndReturnStorableDatasetForIt(
                    companyAlterationManager, euTaxonomyNonFinancialsFrameworkName,
                )

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
                    mockStorageClient, dataManagerUtils, mockMessageQueuePublications,
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
    }
