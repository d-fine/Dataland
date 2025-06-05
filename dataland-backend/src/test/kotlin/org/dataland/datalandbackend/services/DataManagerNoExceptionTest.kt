package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.metainformation.PlainDataAndMetaInformation
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import java.time.Instant
import java.util.UUID

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@Suppress("LongParameterList")
class DataManagerNoExceptionTest
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        val dataMetaInformationManager: DataMetaInformationManager,
        val companyQueryManager: CompanyQueryManager,
        val companyAlterationManager: CompanyAlterationManager,
        val dataManagerUtils: DataManagerUtils,
        val sourceabilityDataManager: SourceabilityDataManager,
    ) {
        val mockStorageClient: StorageControllerApi = mock<StorageControllerApi>()
        val mockMessageQueuePublications: MessageQueuePublications = mock<MessageQueuePublications>()
        val testDataProvider = TestDataProvider(objectMapper)
        lateinit var dataManager: DataManager
        lateinit var spyDataManager: DataManager
        lateinit var messageQueueListenerForDataManager: MessageQueueListenerForDataManager
        val correlationId = IdUtils.generateUUID()
        val dataUUID = "JustSomeUUID"
        val euTaxonomyNonFinancialsFrameworkName = "eutaxonomy-non-financials"

        @BeforeEach
        fun setup() {
            reset(mockStorageClient, mockMessageQueuePublications)
            dataManager =
                DataManager(
                    objectMapper, companyQueryManager, dataMetaInformationManager,
                    mockStorageClient, dataManagerUtils, mockMessageQueuePublications,
                )
            spyDataManager = spy(dataManager)
            messageQueueListenerForDataManager =
                MessageQueueListenerForDataManager(
                    objectMapper, dataMetaInformationManager,
                    dataManager, sourceabilityDataManager,
                )
        }

        @Test
        fun `check that processing a patch event works as expected`() {
            val storableEuTaxonomyDatasetForNonFinancials =
                testDataProvider.addCompanyAndReturnStorableDatasetForIt(
                    companyAlterationManager, euTaxonomyNonFinancialsFrameworkName,
                )

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
        fun `test that data id for new active dataset can be empty`() {
            val storableEuTaxonomyDatasetForNonFinancials: StorableDataset =
                testDataProvider.addCompanyAndReturnStorableDatasetForIt(
                    companyAlterationManager, euTaxonomyNonFinancialsFrameworkName,
                )
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

        @Test
        fun `check that no exception is thrown when a search for company data and metainformation yields no results`() {
            val euTaxonomyNonFinancialDataset =
                testDataProvider.addCompanyAndReturnStorableDatasetForIt(
                    companyAlterationManager, euTaxonomyNonFinancialsFrameworkName,
                )
            val dataMetaInformationSearchFilterWithoutSearchResults =
                DataMetaInformationSearchFilter(
                    companyId = euTaxonomyNonFinancialDataset.companyId,
                    dataType = DataType(euTaxonomyNonFinancialsFrameworkName),
                    reportingPeriod = "dummyReportingPeriod",
                    onlyActive = true,
                )
            assertDoesNotThrow {
                assertEquals(
                    emptyList<PlainDataAndMetaInformation>(),
                    dataManager.getAllDatasetsAndMetaInformation(
                        dataMetaInformationSearchFilterWithoutSearchResults,
                        UUID.randomUUID().toString(),
                    ),
                )
            }
        }
    }
