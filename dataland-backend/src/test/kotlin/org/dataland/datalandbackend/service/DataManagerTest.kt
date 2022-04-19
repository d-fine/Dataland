package org.dataland.datalandbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.TestDataProvider
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DataManagerTest(
    @Autowired val edcClient: DefaultApi,
    @Autowired val objectMapper: ObjectMapper
) {

    val testManager = DataManager(edcClient, objectMapper)

    val dataProvider = TestDataProvider(objectMapper)
    val testCompanyList = dataProvider.getCompanyInformation(4)

    private fun addAllCompanies(companies: List<CompanyInformation>) {
        for (company in companies) {
            testManager.addCompany(company)
        }
    }

    @Test
    fun `add the first company and check if it can be retrieved by using the company ID that is returned`() {
        val testCompanyId = testManager.addCompany(testCompanyList[0]).companyId
        assertEquals(
            StoredCompany(testCompanyId, testCompanyList[0], mutableListOf()),
            testManager.getCompanyById(testCompanyId),
            "The company behind the company ID in the post-response " +
                "does not contain company information of the posted company."
        )
    }

    @Test
    fun `add all companies then retrieve them as a list and check for each company if it can be found as expected`() {
        addAllCompanies(testCompanyList)
        val allCompaniesInStore = testManager.searchCompanies("", true)
        for ((index, storedCompany) in allCompaniesInStore.withIndex()) {
            val expectedCompanyId = (index + 1).toString()
            assertEquals(
                StoredCompany(expectedCompanyId, testCompanyList[index], mutableListOf()), storedCompany,
                "The stored company does not contain the company information of the posted company."
            )
        }
    }

    @Test
    fun `add all companies and search for them one by one by using their names`() {
        addAllCompanies(testCompanyList)
        for (company in testCompanyList) {
            val searchResponse = testManager.searchCompanies(company.companyName, true)
            assertTrue(
                searchResponse.any { it.companyInformation.companyName == company.companyName },
                "The posted company could not be retrieved by searching for its name."
            )
        }
    }

    @Test
    fun `search for all identifier values and check if all results contain the looked for value`() {
        addAllCompanies(testCompanyList)
        for (company in testCompanyList) {
            for (identifier in company.identifiers) {
                val searchResponse = testManager.searchCompanies(identifier.identifierValue, false).toMutableList()
                // The response list is filtered to exclude results that match in account of another identifier having
                // the required value but the looked for identifier type does not exist (This happens due to the test
                // data having non-unique identifier values for different identifier types)
                searchResponse.retainAll {
                    it.companyInformation.identifiers.any {
                        identifierInResponse ->
                        identifierInResponse.identifierType == identifier.identifierType
                    }
                }
                assertTrue(
                    searchResponse.all { it.companyInformation.identifiers.contains(identifier) },
                    "The posted company could not be retrieved by searching for its identifier."
                )
            }
        }
    }

    @Test
    fun `add all companies and verify that searching for stock indices returns correct results`() {
        val stockIndiciesInTestData = mutableSetOf<CompanyInformation.StockIndex>()
        for (company in testCompanyList) {
            testManager.addCompany(company)
            stockIndiciesInTestData += company.indices
        }

        for (stockIndex in stockIndiciesInTestData) {
            val searchResponse = testManager.searchCompaniesByIndex(stockIndex)
            assertTrue(
                searchResponse.all { it.companyInformation.indices.contains(stockIndex) },
                "The posted company could not be retrieved by searching for its stock indices."
            )
        }
    }

    @Test
    fun `upload all companies and search for identifier substring to verify substring matching in company search`() {
        addAllCompanies(testCompanyList)
        val searchResponse = testManager.searchCompanies("9900W18LQJJN6SJ3", false)
        assertEquals(
            testCompanyList.size, searchResponse.size,
            "There are ${testCompanyList.size} expected matches but found ${searchResponse.size}."
        )
    }

    @Test
    fun `upload all companies and search for name substring to verify substring matching in company search`() {
        addAllCompanies(testCompanyList)
        val searchResponse = testManager.searchCompanies("usse - Hoh", true)
        assertEquals(
            1, searchResponse.size,
            "There is 1 expected match but found ${searchResponse.size}."
        )
    }

    @Test
    fun `add all companies and check that the number of results when searching for DAX index is as expected`() {
        var expectedResult = 0
        val testIndex = CompanyInformation.StockIndex.Dax
        for (company in testCompanyList) {
            testManager.addCompany(company)
            if (company.indices.contains(testIndex)) {
                expectedResult ++
            }
        }
        val searchResponse = testManager.searchCompaniesByIndex(testIndex)
        assertEquals(
            expectedResult, searchResponse.size,
            "There are 2 companies with DAX index but found ${searchResponse.size}."
        )
    }

    @Test
    fun `check that an exception is thrown when company id is provided that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testManager.searchDataMetaInfo(companyId = "error")
        }
    }
}
