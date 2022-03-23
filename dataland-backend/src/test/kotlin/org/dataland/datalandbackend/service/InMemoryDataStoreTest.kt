package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.StorableDataSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class InMemoryDataStoreTest {

    val testStore = InMemoryDataStore()
    val testCompanyNamesToStore = listOf("Imaginary-Company_I", "Fantasy-Company_II", "Dream-Company_III")

    val testDataSetsToStore = listOf(
        StorableDataSet(companyId = "1", dataType = "SomeDataType", data = "some_data_in_specific_structure_yyy"),
        StorableDataSet(companyId = "1", dataType = "AnotherDataType", data = "some_data_in_specific_structure_iii"),
        StorableDataSet(companyId = "1", dataType = "RandomDataType", data = "some_data_in_specific_structure_aaa")
    )

    @Test
    fun `add the first company and check if its name is as expected by using the return value of addCompany`() {
        val companyMetaInformation = testStore.addCompany(testCompanyNamesToStore[0])
        assertEquals(
            companyMetaInformation.companyName, testCompanyNamesToStore[0],
            "The company name in the post-response does not match the actual name of the company to be posted."
        )
    }

    @Test
    fun `add all companies then retrieve them as a list and check for each company if its name is as expected`() {
        for (companyName in testCompanyNamesToStore) {
            testStore.addCompany(companyName)
        }

        val allCompaniesInStore = testStore.listCompaniesByName("")

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
            testStore.addCompany(companyName)
        }

        for (companyName in testCompanyNamesToStore) {
            val searchResponse = testStore.listCompaniesByName(companyName)
            assertEquals(
                companyName, searchResponse.first().companyName,
                "The posted company could not be found in the data store by searching for its name."
            )
        }
    }

    @Test
    fun `post the first company and all dummy data sets for it and check if all data sets of it are listed`() {
        val testCompanyId = testStore.addCompany(testCompanyNamesToStore[0]).companyId

        val listOfDataIds = mutableListOf<String>()
        for (dataSet in testDataSetsToStore) {
            listOfDataIds.add(testStore.addDataSet(dataSet))
        }

        val dataSetsOfTestCompany = testStore.listDataSetsByCompanyId(testCompanyId)

        for (dataId in listOfDataIds) {
            assertEquals(
                testDataSetsToStore[dataId.toInt() - 1].dataType,
                dataSetsOfTestCompany.filter { it.dataId == dataId }.first().dataType,
                "The stored data set type does not match the test data set type."
            )
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

    @Test
    fun `add and get data set by identifier`() {
        testStore.addCompany(testCompanyNamesToStore[0]).companyId

        val testDataSetId = testStore.addDataSet(testDataSetsToStore[0])
        assertEquals(
            testDataSetsToStore[0].data,
            testStore.getStorableDataSet(
                DataIdentifier(
                    dataId = testDataSetId,
                    dataType = testDataSetsToStore[0].dataType
                )
            ).data,
            "The posted data set does not match the retrieved data set."
        )
    }

    @Test
    fun `add all test data sets and check if they appear in the list of all data sets`() {
        testStore.addCompany(testCompanyNamesToStore[0]).companyId

        val listOfDataIds = mutableListOf<String>()

        for (dataSet in testDataSetsToStore) {
            listOfDataIds.add(testStore.addDataSet(dataSet))
        }

        val listOfAllDataSets = testStore.listDataSets()

        for (dataId in listOfDataIds) {
            assertEquals(
                testDataSetsToStore[dataId.toInt() - 1].dataType,
                listOfAllDataSets.filter { it.dataIdentifier.dataId == dataId }.first().dataIdentifier.dataType,
                "The data types of the test data set and the stored data set do not match."

            )
            assertEquals(
                testDataSetsToStore[dataId.toInt() - 1].companyId,
                listOfAllDataSets.filter { it.dataIdentifier.dataId == dataId }.first().companyId,
                "The company Ids of the test data set and the stored data set do not match."
            )
        }
    }

    @Test
    fun `get add data set error`() {
        val invalidDataSetToStore = StorableDataSet(companyId = "error", dataType = "someDataType", data = "some_data")
        assertThrows<IllegalArgumentException> {
            testStore.addDataSet(invalidDataSetToStore)
        }
    }

    @Test
    fun `produce get data set error for invalid data id`() {
        testStore.addCompany(testCompanyNamesToStore[0]).companyId

        testStore.addDataSet(testDataSetsToStore[0])

        assertThrows<IllegalArgumentException> {
            testStore.getStorableDataSet(
                DataIdentifier(
                    dataId = "error",
                    dataType = testDataSetsToStore[0].dataType
                )
            )
        }
    }

    @Test
    fun `produce get data set error for invalid data type`() {
        testStore.addCompany(testCompanyNamesToStore[0]).companyId

        val testDataSetId = testStore.addDataSet(testDataSetsToStore[0])

        assertThrows<IllegalArgumentException> {
            testStore.getStorableDataSet(DataIdentifier(dataId = testDataSetId, dataType = "error"))
        }
    }
}
