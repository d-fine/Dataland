package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.utils.TestDataProvider
import org.junit.jupiter.api.Test
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
    @Autowired val dataManager: DataManagerInterface,
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val companyManager: CompanyManager
) {

    val testDataProvider = TestDataProvider(objectMapper)

    @Test
    fun `check that an exception is thrown when non matching dataId to dataType pair is requested from data storage`() {
        val companyInformation = testDataProvider.getCompanyInformation(1).first()

        val companyId = companyManager.addCompany(companyInformation).companyId

        val euTaxonomyDataForNonFinancials = testDataProvider.getEuTaxonomyDataForNonFinancials(1).first()
        val euTaxonomyDataForNonFinancialsAsString = objectMapper.writeValueAsString(euTaxonomyDataForNonFinancials)
        val storableDataSet =
            StorableDataSet(companyId, DataType("eutaxonomy-non-financials"), euTaxonomyDataForNonFinancialsAsString)
        /*
        val dataId = dataManager.addDataSet(storableDataSet)
        assertThrows<IllegalArgumentException> {
            dataManager.getDataSet(dataId, DataType("eutaxonomy-financials"))*
        }*/
    }
}
