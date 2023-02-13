package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.StorageHashMap
import org.dataland.datalandbackend.openApiClient.api.NonPersistedDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.services.CompanyManager
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandinternalstorage.DatalandInternalStorage
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandinternalstorage.utils.TestDataProvider
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest(classes = [DatalandInternalStorage::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DatabaseDataStoreTest(
    @Autowired val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired val nonPersistedDataClient: NonPersistedDataControllerApi,
    @Autowired val companyManager: CompanyManager,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired var dataInformationHashMap: StorageHashMap,
) {
    val mockDataItemRepository: DataItemRepository = mock(DataItemRepository::class.java)
    val testDataProvider = TestDataProvider(objectMapper)
    val databaseDataStore = DatabaseDataStore(
        mockDataItemRepository, cloudEventMessageHandler, nonPersistedDataClient,
        objectMapper,
    )
    val dataId = "JustSomeUUID"

    val storableEuTaxonomyDataSetForNonFinancials: StorableDataSet =
        addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinancialsForIt()
    val storableNFEuTaxonomyDataSetAsString: String =
        objectMapper.writeValueAsString(storableEuTaxonomyDataSetForNonFinancials)

    val messageForDataId = Message(dataId.toByteArray())

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

    @Test
    fun `check that a Server Exception is thrown when the data storage reports a Server Exception during insertion`() {
        dataInformationHashMap.map.put(dataId, storableNFEuTaxonomyDataSetAsString)
        `when`(mockDataItemRepository.save(DataItem(dataId, storableNFEuTaxonomyDataSetAsString))).thenThrow(
            ServerException::class.java,
        )
        assertThrows<InternalServerErrorApiException> {
            databaseDataStore.insertDataSet(messageForDataId)
        }
    }
}
