package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.edcClient.infrastructure.ServerException
import org.dataland.datalandbackend.edcClient.model.InsertDataResponse
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.utils.TestDataProvider
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

    private fun addOneCompanyAndReturnAStorableDataSetForIt(): StorableDataSet {
        val companyInformation = testDataProvider.getCompanyInformation(1).first()
        val companyId = companyManager.addCompany(companyInformation).companyId
        val euTaxonomyDataForNonFinancialsAsString = "someEuTaxonomyDataForNonFinancials123"
        return StorableDataSet(companyId, DataType("eutaxonomy-non-financials"), euTaxonomyDataForNonFinancialsAsString)
    }

    @Test
    fun `check that an exception is thrown when non matching dataId to dataType pair is requested from data storage`() {
        val storableDataSet = addOneCompanyAndReturnAStorableDataSetForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(edcClientMock.insertData(storableDataSetAsString)).thenReturn(InsertDataResponse("abcdefghijkl"))
        val dataId = dataManager.addDataSet(storableDataSet)
        assertThrows<IllegalArgumentException> {
            dataManager.getDataSet(dataId, DataType("eutaxonomy-financials"))
        }
    }

    @Test
    fun `check that a Server Exception is thrown when the data storage itself reports a Server Exception`() {
        val storableDataSet = addOneCompanyAndReturnAStorableDataSetForIt()
        val storableDataSetAsString = objectMapper.writeValueAsString(storableDataSet)
        `when`(edcClientMock.insertData(storableDataSetAsString)).thenThrow(ServerException::class.java)
        assertThrows<ServerException> {
            dataManager.addDataSet(storableDataSet)
        }
    }
}
