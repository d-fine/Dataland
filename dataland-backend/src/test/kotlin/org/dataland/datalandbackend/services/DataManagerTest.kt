package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.edcClient.infrastructure.ServerException
import org.dataland.datalandbackend.edcClient.model.InsertDataResponse
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.utils.TestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import javax.transaction.Transactional

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DataManagerTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val companyManager: CompanyManager,
) {

    val testDataProvider = TestDataProvider(objectMapper)
    val edcClientMock = mock(DefaultApi::class.java)
    val dataManager = DataManager(edcClientMock, objectMapper, companyManager, dataMetaInformationManager)

    private fun addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt(): StorableDataSet {
        val companyInformation = testDataProvider.getCompanyInformation(1).first()
        val companyId = companyManager.addCompany(companyInformation).companyId
        val euTaxonomyDataForNonFinancialsAsString = "someEuTaxonomyDataForNonFinancials123"
        return StorableDataSet(companyId, DataType("eutaxonomy-non-financials"), euTaxonomyDataForNonFinancialsAsString)
    }

    @Test
    fun `check that a Server Exception is thrown when the data storage reports a Server Exception during insertion`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(edcClientMock.insertData(storableDataSetAsString)).thenThrow(ServerException::class.java)
        assertThrows<ServerException> {
            dataManager.addDataSet(storableDataSet)
        }
    }

    @Test
    fun `check that a Server Exception is thrown when the data storage reports a Server Exception during selection`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(edcClientMock.insertData(storableDataSetAsString)).thenReturn(InsertDataResponse("XXXsomeUUIDXXX"))
        val dataId = dataManager.addDataSet(storableDataSet)
        `when`(edcClientMock.selectDataById(dataId)).thenThrow(ServerException::class.java)
        assertThrows<ServerException> {
            dataManager.getDataSet(dataId, DataType(storableDataSet.dataType.name))
        }
    }


    @Test
    fun `check that an exception is thrown when non matching dataId to dataType pair is requested from data storage`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(edcClientMock.insertData(storableDataSetAsString)).thenReturn(InsertDataResponse("XXXsomeUUIDXXX"))
        val dataId = dataManager.addDataSet(storableDataSet)
        val thrown = assertThrows<IllegalArgumentException> {
            dataManager.getDataSet(dataId, DataType("eutaxonomy-financials"))
        }
        assertEquals(
            "The data with the id: $dataId is registered as type ${storableDataSet.dataType.name} by " +
                    "Dataland instead of your requested type eutaxonomy-financials.",
            thrown.message
        )
    }


    @Test
    fun `check that an exception is thrown if the received data from the data storage is empty`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(edcClientMock.insertData(storableDataSetAsString)).thenReturn(InsertDataResponse("XXXsomeUUIDXXX"))
        val dataId = dataManager.addDataSet(storableDataSet)
        `when`(edcClientMock.selectDataById(dataId)).thenReturn("")
        val thrown = assertThrows<IllegalArgumentException> {
            dataManager.getDataSet(dataId, DataType("eutaxonomy-non-financials"))
        }
        assertEquals("No data set with the id: $dataId could be found in the data store.", thrown.message)
    }

    @Test
    fun `check that an exception is thrown if the received data from the data storage has an unexpected type`() {
        val storableDataSet = addCompanyAndReturnStorableEuTaxonomyDataSetForNonFinacialsForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(edcClientMock.insertData(storableDataSetAsString)).thenReturn(InsertDataResponse("XXXsomeUUIDXXX"))
        val dataId = dataManager.addDataSet(storableDataSet)
        val unexpectedDataTypeName = "eutaxonomy-financials"
        `when`(edcClientMock.selectDataById(dataId)).thenReturn(
            objectMapper.writeValueAsString(
                StorableDataSet(
                    storableDataSet.companyId,
                    DataType(unexpectedDataTypeName),
                    storableDataSet.data
                )
            )
        )
        val thrown = assertThrows<IllegalArgumentException> {
            dataManager.getDataSet(dataId, DataType(storableDataSet.dataType.name))
        }
        assertEquals(
            "The data set with the id: $dataId came back as type $unexpectedDataTypeName from thedata " +
                    "store instead of type ${storableDataSet.dataType.name} as registered by Dataland.", thrown.message
        )
    }
}
