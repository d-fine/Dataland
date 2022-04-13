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

    val dataProvider = TestDataProvider()
    val testCompanyList = dataProvider.getCompanyInformation(4)

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
        for (company in testCompanyList) {
            testManager.addCompany(company)
        }

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
        for (company in testCompanyList) {
            testManager.addCompany(company)
        }

        for (company in testCompanyList) {
            val searchResponse = testManager.searchCompanies(company.companyName, true)
            assertEquals(
                company.companyName, searchResponse.first().companyInformation.companyName,
                "The posted company could not be retrieved by searching for its name."
            )
        }
    }

    @Test
    fun `search for identifiers and check if it can find the one`() {
        for (company in testCompanyList) {
            testManager.addCompany(company)
        }

        for (company in testCompanyList) {
            val identifiers = company.identifiers
            for (identifier in identifiers) {
                val searchResponse = testManager.searchCompanies(identifier.identifierValue, false)
                assertTrue(
                    searchResponse.all { it.companyInformation.identifiers.contains(identifier) },
                    "The posted company could not be retrieved by searching for its identifier."
                )
            }
        }
    }

    @Test
    fun `search for stock indices and check if it can find the ones`() {
        for (company in testCompanyList) {
            testManager.addCompany(company)
        }

        for (company in testCompanyList) {
            val stockIndices = company.indices
            for (stockIndex in stockIndices) {
                val searchResponse = testManager.searchCompaniesByIndex(stockIndex)
                assertTrue(
                    searchResponse.all { it.companyInformation.indices.contains(stockIndex) },
                    "The posted company could not be retrieved by searching for its stock indices."
                )
            }
        }
    }

    @Test
    fun `search for companies containing de and check if it returns three companies`() {
        for (company in testCompanyList) {
            testManager.addCompany(company)
        }
        val searchResponse = testManager.searchCompanies("de", false)
        assertEquals(
            3, searchResponse.size,
            "There are 3 companies containing 'de' (in name or identifier) but found ${searchResponse.size}."
        )
    }

    @Test
    fun `search for DAX index and check if two companies will be returned`() {
        for (company in testCompanyList) {
            testManager.addCompany(company)
        }
        val searchResponse = testManager.searchCompaniesByIndex(CompanyInformation.StockIndex.Dax)
        assertEquals(
            2, searchResponse.size,
            "There are 2 companies with DAX index but found ${searchResponse.size}."
        )
    }

    @Test
    fun `get the data sets for a company id that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testManager.searchDataMetaInfo(companyId = "error")
        }
    }
}
