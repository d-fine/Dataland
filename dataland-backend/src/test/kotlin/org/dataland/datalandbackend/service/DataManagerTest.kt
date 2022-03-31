package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.model.PostCompanyRequestBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.Date

@SpringBootTest
class DataManagerTest {

    val testManager = DataManager(edcClient = DefaultApi(basePath = "dummy"))

    val testCompanyList = listOf(
        PostCompanyRequestBody(
            companyName = "Test-Company_1",
            headquarters = "Test-Headquarters_1",
            industrialSector = "Test-IndustrialSector_1",
            marketCap = BigDecimal(100),
            reportingDateOfMarketCap = Date()
        ),
        PostCompanyRequestBody(
            companyName = "Test-Company_2",
            headquarters = "Test-Headquarters_2",
            industrialSector = "Test-IndustrialSector_2",
            marketCap = BigDecimal(200),
            reportingDateOfMarketCap = Date()
        )
    )

    /*
    val testDataSetsToStore = listOf(
        StorableDataSet(companyId = "1", dataType = "SomeDataType", data = "some_data_in_specific_structure_yyy"),
        StorableDataSet(companyId = "1", dataType = "AnotherDataType", data = "some_data_in_specific_structure_iii"),
        StorableDataSet(companyId = "1", dataType = "RandomDataType", data = "some_data_in_specific_structure_aaa")
    )
    */

    /*
    ________________________________
    Tests for all data manager functionalities associated with adding and getting companies:
    ________________________________
     */

    @Test
    fun `add the first company and check if its name is as expected by using the return value of addCompany`() {
        val companyMetaInformation = testManager.addCompany(testCompanyList[0])
        assertEquals(
            companyMetaInformation.postCompanyRequestBody.companyName, testCompanyList[0].companyName,
            "The company name in the post-response does not match the actual name of the company to be posted."
        )
    }

    @Test
    fun `add all companies then retrieve them as a list and check for each company if its name is as expected`() {
        for (company in testCompanyList) {
            testManager.addCompany(company)
        }

        val allCompaniesInStore = testManager.listCompaniesByName("")
        for ((counter, storedCompany) in allCompaniesInStore.withIndex()) {
            assertEquals(
                testCompanyList[counter].companyName, storedCompany.postCompanyRequestBody.companyName,
                "The stored company name does not match the test company name."
            )
        }
    }

    @Test
    fun `add all companies and search for them one by one by using their names`() {
        for (company in testCompanyList) {
            testManager.addCompany(company)
        }

        for (company in testCompanyList) {
            val searchResponse = testManager.listCompaniesByName(company.companyName)
            assertEquals(
                company.companyName, searchResponse.first().postCompanyRequestBody.companyName,
                "The posted company could not be found in the data store by searching for its name."
            )
        }
    }

    @Test
    fun `get companies with a name that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testManager.listCompaniesByName("error")
        }
    }

    @Test
    fun `get the data sets for a company id that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testManager.searchDataMetaInfo(companyId = "error")
        }
    }

    /*
    ________________________________
    Tests for all data manager functionalities associated with adding and getting data for companies:
    ________________________________
     */
/*

The following tests require, that data is posted. To post data, a running instance of edc-dummyserver is needed.
Until now, we haven't mocked the edc-dummyserver, and therefore the following unit tests cannot run.
They stay commented out, until a decision is made.

    @Test
    fun `add data set and get data back by using its data ID`() {
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
            "The data in the posted data set does not match the retrieved data."
        )
    }

    @Test
    fun `post the first company and all dummy data sets for it and check if all data sets of it are listed`() {
        val testCompanyId = testManager.addCompany(testCompanyNamesToStore[0]).companyId

        val listOfDataIds = mutableListOf<String>()
        for (dataSet in testDataSetsToStore) {
            listOfDataIds.add(testManager.addDataSet(dataSet))
        }

        val listDataSetsByCompanyIdResponse = testManager.searchDataMetaInfo(companyId = testCompanyId)

        for (dataId in listOfDataIds) {
            assertEquals(
                DataManagerInputToGetData(dataId = dataId, dataType = testDataSetsToStore[dataId.toInt() - 1].dataType),
                listDataSetsByCompanyIdResponse.first { it.dataId == dataId },
                "The stored data set type does not match the test data set type."
            )
        }
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
        testManager.addCompany(testCompanyNamesToStore[0])

        testManager.addDataSet(testDataSetsToStore[0])

        assertThrows<IllegalArgumentException> {
            testManager.getData(DataManagerInputToGetData(dataId = "error", dataType = testDataSetsToStore[0].dataType))
        }
    }

    @Test
    fun `produce get data set error for invalid data type`() {
        testManager.addCompany(testCompanyNamesToStore[0])

        val testDataSetId = testManager.addDataSet(testDataSetsToStore[0])

        assertThrows<IllegalArgumentException> {
            testManager.getData(DataManagerInputToGetData(dataId = testDataSetId, dataType = "error"))
        }
    }

 */
}
