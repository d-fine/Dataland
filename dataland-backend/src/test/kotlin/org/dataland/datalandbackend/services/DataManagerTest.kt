package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.mockito.Mockito.spy
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset

open class DataManagerTest(
    val objectMapper: ObjectMapper,
    val dataMetaInformationManager: DataMetaInformationManager,
    val companyQueryManager: CompanyQueryManager,
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

    open fun setup() {
        reset(mockStorageClient, mockMessageQueuePublications)
        dataManager =
            DataManager(
                objectMapper, companyQueryManager, dataMetaInformationManager,
                mockStorageClient, dataManagerUtils, mockMessageQueuePublications,
            )
        spyDataManager = spy(dataManager)
        messageQueueListenerForDataManager =
            MessageQueueListenerForDataManager(
                dataMetaInformationManager,
                dataManager, sourceabilityDataManager,
            )
    }
}
