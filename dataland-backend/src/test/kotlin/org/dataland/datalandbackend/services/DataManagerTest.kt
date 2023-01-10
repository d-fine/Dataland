package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ServerException
import org.dataland.datalandinternalstorage.openApiClient.model.InsertDataResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.*

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DataManagerTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val companyManager: CompanyManager,
) {
    val mockStorageClient: StorageControllerApi = mock(StorageControllerApi::class.java)
    val testDataProvider = TestDataProvider(objectMapper)
    val dataManager = DataManager(objectMapper, companyManager, dataMetaInformationManager, mockStorageClient)
    val correlationId = UUID.randomUUID().toString()
    val dataUUId = "JustSomeUUID"

    private fun addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt(): StorableDataSet {
        val companyInformation = testDataProvider.getCompanyInformation(1).first()
        val companyId = companyManager.addCompany(companyInformation).companyId
        val euTaxonomyDataForNonFinancialsAsString = "someEuTaxonomyDataForNonFinancials123"
        return StorableDataSet(
            companyId,
            DataType("eutaxonomy-non-financials"),
            "ADMIN_USER_ID",
            Instant.now().epochSecond,
            euTaxonomyDataForNonFinancialsAsString
        )
    }

    @Test
    fun `check that a Server Exception is thrown when the data storage reports a Server Exception during insertion`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(mockStorageClient.insertData(correlationId, storableDataSetAsString)).thenThrow(
            ServerException::class.java
        )
        assertThrows<InternalServerErrorApiException> {
            dataManager.addDataSet(storableDataSet, correlationId)
        }
    }

    @Test
    fun `check that a Server Exception is thrown when the data storage reports a Server Exception during selection`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(mockStorageClient.insertData(correlationId, storableDataSetAsString)).thenReturn(
            InsertDataResponse(dataUUId)
        )
        val dataId = dataManager.addDataSet(storableDataSet, correlationId)
        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenThrow(ServerException::class.java)
        assertThrows<ServerException> {
            dataManager.getDataSet(dataId, DataType(storableDataSet.dataType.name), correlationId)
        }
    }

    @Test
    fun `check that an exception is thrown when non matching dataId to dataType pair is requested from data storage`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(mockStorageClient.insertData(correlationId, storableDataSetAsString)).thenReturn(
            InsertDataResponse(dataUUId)
        )
        val dataId = dataManager.addDataSet(storableDataSet, correlationId)
        val thrown = assertThrows<InvalidInputApiException> {
            dataManager.getDataSet(dataId, DataType("eutaxonomy-financials"), correlationId)
        }
        assertEquals(
            "The data with the id: $dataId is registered as type eutaxonomy-non-financials by " +
                "Dataland instead of your requested type eutaxonomy-financials.",
            thrown.message
        )
    }

    @Test
    fun `check that an exception is thrown if the received data from the data storage is empty`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(mockStorageClient.insertData(correlationId, storableDataSetAsString)).thenReturn(
            InsertDataResponse(dataUUId)
        )
        val dataId = dataManager.addDataSet(storableDataSet, correlationId)
        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn("")
        val thrown = assertThrows<ResourceNotFoundApiException> {
            dataManager.getDataSet(dataId, DataType("eutaxonomy-non-financials"), correlationId)
        }
        assertEquals("No dataset with the id: $dataId could be found in the data store.", thrown.message)
    }

    @Test
    fun `check that an exception is thrown if the received data from the data storage has an unexpected type`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(mockStorageClient.insertData(correlationId, storableDataSetAsString)).thenReturn(
            InsertDataResponse(dataUUId)
        )
        val dataId = dataManager.addDataSet(storableDataSet, correlationId)
        val expectedDataTypeName = getExpectedDataTypeName(storableDataSet, dataId, "eutaxonomy-financials")
        val thrown = assertThrows<InternalServerErrorApiException> {
            dataManager.getDataSet(dataId, DataType(expectedDataTypeName), correlationId)
        }
        assertEquals(
            "The meta-data of dataset $dataId differs between the data store and the database",
            thrown.message
        )
    }

    private fun getExpectedDataTypeName(
        storableDataSet: StorableDataSet,
        dataId: String,
        unexpectedDataTypeName: String
    ): String {
        val expectedDataTypeName = storableDataSet.dataType.name
        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
            objectMapper.writeValueAsString(storableDataSet.copy(dataType = DataType(unexpectedDataTypeName)))
        )
        return expectedDataTypeName
    }

    @Test
    fun `check that an exception is thrown if the received data from the storage has an unexpected uploading user`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(mockStorageClient.insertData(correlationId, storableDataSetAsString)).thenReturn(
            InsertDataResponse(dataUUId)
        )
        val dataId = dataManager.addDataSet(storableDataSet, correlationId)

        `when`(mockStorageClient.selectDataById(dataId, correlationId)).thenReturn(
            objectMapper.writeValueAsString(storableDataSet.copy(uploaderUserId = "NOT_WHATS_EXPECTED"))
        )

        val thrown = assertThrows<InternalServerErrorApiException> {
            dataManager.getDataSet(dataId, storableDataSet.dataType, correlationId)
        }
        assertEquals(
            "The meta-data of dataset $dataId differs between the data store and the database",
            thrown.message
        )
    }
}
