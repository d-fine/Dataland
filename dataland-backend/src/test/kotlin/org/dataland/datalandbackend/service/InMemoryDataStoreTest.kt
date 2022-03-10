package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.datalandbackend.model.StoredDataSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

// TODO Adadpt to new functions
// TODO Add error messages??
@SpringBootTest
class InMemoryDataStoreTest {
    val testStore = InMemoryDataStore()
    val companyNamesToStore = listOf("Imaginary-Company_I", "Fantasy-Company_II", "Dream-Company_III")
    /*val dataSetsToStore = listOf(
        StoredDataSet(companyId = "1", )
        StoredDataSet(name = "StoredCompany A", payload = "Data"),
        StoredDataSet(name = "Holding B", payload = "Information"),
        StoredDataSet(name = "Group C", payload = "Inputs")
    )*/




    @Test
    fun `add the first company and check if its name is as expected by using the return value of addCompany`() {
        val companyMetaInformation = testStore.addCompany(companyNamesToStore[0])
        assertEquals(companyMetaInformation.companyName, companyNamesToStore[0])
    }

    @Test
    fun `add all companies, retrieve them as a list and check for each company if its name is as expected`() {
        for (companyName in companyNamesToStore) {
            testStore.addCompany(companyName)
        }

        val allCompaniesInStore = testStore.listAllCompanies()
        for ((counter, storedCompany) in allCompaniesInStore.withIndex()) {
            assertEquals(companyNamesToStore[counter], storedCompany.companyName)
        }
    }



    @Test
    fun `add all companies and search for them one by one by using their names`() {
        for (companyName in companyNamesToStore) {
            testStore.addCompany(companyName)
        }

        for (companyName in companyNamesToStore) {
            val searchResponse = testStore.listCompaniesByName(companyName)
            assertEquals(companyName, searchResponse.first().companyName)
        }
    }


    //Company-Methods: listDataSetsByCompany

    @Test
    fun `post the first company and all dummy data sets for it and check if all data sets of it can be retrieved`() {
        for (companyName in companyNamesToStore) {
            testStore.addCompany(companyName)
        }

        for (companyName in companyNamesToStore) {
            val searchResponse = testStore.listCompaniesByName(companyName)
            assertEquals(companyName, searchResponse.first().companyName)
        }
    }


    @Test
    fun `get companies with a name that does not exist yet in the store`() {
        assertThrows<IllegalArgumentException> {
            testStore.listCompaniesByName("error")
        }
    }

    @Test
    fun `get the data sets for a company id that does not exist yet in the store`() {
        assertThrows<IllegalArgumentException> {
            testStore.listDataSetsByCompanyId("error")
        }
    }


    /*
remaining methods to test:
Data-Methods: addDataSet, listDataSets, getDataSet
 */

/*
    @Test
    fun `add and get dataset by id`() {
        val identifier = testStore.addCompany(company = dataSets[1])
        assertEquals(dataSets[1], testStore.listCompaniesByName(identifier.id))
    }



    @Test
    fun `get dataset error message`() {
        val id = "2"
        val expectedMessage = "The id: $id does not exist."
        val exceptionThatWasThrown: Throwable = assertThrows<IllegalArgumentException> {
            testStore.listCompaniesByName(id)
        }
        assertEquals(expectedMessage, exceptionThatWasThrown.message)
    }

    @Test
    fun `check if the id of the last dataset equals the total number of all datasets after adding them all`() {
        var dataSetMetaInformation: DataSetMetaInformation? = null
        for (dataSet in dataSets) {
            dataSetMetaInformation = testStore.addCompany(company = dataSet)
        }
        assertEquals(dataSetMetaInformation!!.id, dataSets.size.toString())
    }
 */
}
