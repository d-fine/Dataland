package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.model.DataManagerInputToGetData
import org.dataland.datalandbackend.model.StorableDataSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DataManagerTest {

    val testManager = DataManager(dataStore = InMemoryDataStore())
    val testCompanyNamesToStore = listOf("Imaginary-Company_I", "Fantasy-Company_II", "Dream-Company_III")

    val testDataSetsToStore = listOf(
        StorableDataSet(companyId = "1", dataType = "SomeDataType", data = "some_data_in_specific_structure_yyy"),
        StorableDataSet(companyId = "1", dataType = "AnotherDataType", data = "some_data_in_specific_structure_iii"),
        StorableDataSet(companyId = "1", dataType = "RandomDataType", data = "some_data_in_specific_structure_aaa")
    )

    @Test
    fun `add the first company and check if its name is as expected by using the return value of addCompany`() {
        val companyMetaInformation = testManager.addCompany(testCompanyNamesToStore[0])
        assertEquals(
            companyMetaInformation.companyName, testCompanyNamesToStore[0],
            "The company name in the post-response does not match the actual name of the company to be posted."
        )
    }

    @Test
    fun `add all companies then retrieve them as a list and check for each company if its name is as expected`() {
        for (companyName in testCompanyNamesToStore) {
            testManager.addCompany(companyName)
        }

        val allCompaniesInStore = testManager.listCompaniesByName("")
        for ((counter, storedCompany) in allCompaniesInStore.withIndex()) {
            assertEquals(
                testCompanyNamesToStore[counter], storedCompany.companyName,
                "The stored company name does not match the test company name."
            )
        }
    }

    @Test
    fun `add all companies and search for them one by one by using their names`() {
        for (companyName in testCompanyNamesToStore) {
            testManager.addCompany(companyName)
        }

        for (companyName in testCompanyNamesToStore) {
            val searchResponse = testManager.listCompaniesByName(companyName)
            assertEquals(
                companyName, searchResponse.first().companyName,
                "The posted company could not be found in the data store by searching for its name."
            )
        }
    }

    @Test
    fun `post the first company and all dummy data sets for it and check if all data sets of it are listed`() {
        val testCompanyId = testManager.addCompany(testCompanyNamesToStore[0]).companyId

        val listOfDataIds = mutableListOf<String>()
        for (dataSet in testDataSetsToStore) {
            listOfDataIds.add(testManager.addDataSet(dataSet))
        }

        val listDataSetsByCompanyIdResponse = testManager.listDataSetsByCompanyId(testCompanyId)

        for (dataId in listOfDataIds) {
            assertEquals(
                DataManagerInputToGetData(dataId = dataId, dataType = testDataSetsToStore[dataId.toInt() - 1].dataType),
                listDataSetsByCompanyIdResponse.first { it.dataId == dataId },
                "The stored data set type does not match the test data set type."
            )
        }
    }

    @Test
    fun `get companies with a name that does not exist yet in the store`() {
        assertThrows<IllegalArgumentException> {
            testManager.listCompaniesByName("error")
        }
    }

    @Test
    fun `get the data sets for a company id that does not exist yet in the store`() {
        assertThrows<IllegalArgumentException> {
            testManager.listDataSetsByCompanyId("error")
        }
    }

    @Test
    fun `add and get data set by identifier`() {
        testManager.addCompany(testCompanyNamesToStore[0]).companyId

        val testDataSetId = testManager.addDataSet(testDataSetsToStore[0])
        assertEquals(
            testDataSetsToStore[0].data,
            testManager.getData(
                DataManagerInputToGetData(
                    dataId = testDataSetId,
                    dataType = testDataSetsToStore[0].dataType
                )
            ),
            "The posted data set does not match the retrieved data set."
        )
    }

    @Test
    fun `get add data set error`() {
        val invalidDataSetToStore = StorableDataSet(companyId = "error", dataType = "someDataType", data = "some_data")
        assertThrows<IllegalArgumentException> {
            testManager.addDataSet(invalidDataSetToStore)
        }
    }

    @Test
    fun `produce get data set error for invalid data id`() {
        testManager.addCompany(testCompanyNamesToStore[0]).companyId

        testManager.addDataSet(testDataSetsToStore[0])

        assertThrows<IllegalArgumentException> {
            testManager.getData(DataManagerInputToGetData(dataId = "error", dataType = testDataSetsToStore[0].dataType))
        }
    }

    @Test
    fun `produce get data set error for invalid data type`() {
        testManager.addCompany(testCompanyNamesToStore[0]).companyId

        val testDataSetId = testManager.addDataSet(testDataSetsToStore[0])

        assertThrows<IllegalArgumentException> {
            testManager.getData(DataManagerInputToGetData(dataId = testDataSetId, dataType = "error"))
        }
    }
}
