package org.dataland.datalandbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
class DataManagerTest(
    @Autowired val edcClient: DefaultApi,
    @Autowired val objectMapper: ObjectMapper
) {

    val testManager = DataManager(edcClient, objectMapper)

    val testCompanyList = listOf(
        CompanyInformation(
            companyName = "Test-Company_1de",
            headquarters = "Test-Headquarters_1",
            sector = "Test-Sector_1",
            marketCap = BigDecimal(100),
            reportingDateOfMarketCap = LocalDate.now(),
            indices = listOf(CompanyInformation.StockIndex.MDAX),
            identifiers = listOf(
                CompanyIdentifier(CompanyIdentifier.IdentifierType.Isin, "DE0987654321"),
                CompanyIdentifier(CompanyIdentifier.IdentifierType.Lei, "BLAB")
            )
        ),
        CompanyInformation(
            companyName = "Test-Company_2",
            headquarters = "Test-Headquarters_2",
            sector = "Test-Sector_2",
            marketCap = BigDecimal(200),
            reportingDateOfMarketCap = LocalDate.now(),
            indices = listOf(CompanyInformation.StockIndex.DAX),
            identifiers = listOf(
                CompanyIdentifier(CompanyIdentifier.IdentifierType.Isin, "DE1337"),
                CompanyIdentifier(CompanyIdentifier.IdentifierType.Lei, "BLUB")
            )
        ),
        CompanyInformation(
            companyName = "Test-Company_3",
            headquarters = "Test-Headquarters_3",
            sector = "Test-Sector_3",
            marketCap = BigDecimal(300),
            reportingDateOfMarketCap = LocalDate.now(),
            indices = listOf(CompanyInformation.StockIndex.DAX),
            identifiers = listOf(
                CompanyIdentifier(CompanyIdentifier.IdentifierType.Isin, "IT8765"),
                CompanyIdentifier(CompanyIdentifier.IdentifierType.Lei, "BLIB")
            )
        ),
        CompanyInformation(
            companyName = "Test-Company_4dE",
            headquarters = "Test-Headquarters_4",
            sector = "Test-Sector_3",
            marketCap = BigDecimal(400),
            reportingDateOfMarketCap = LocalDate.now(),
            indices = listOf(CompanyInformation.StockIndex.DAX_50_ESG),
            identifiers = listOf(
                CompanyIdentifier(CompanyIdentifier.IdentifierType.Isin, "FR8525"),
                CompanyIdentifier(CompanyIdentifier.IdentifierType.Lei, "BLEB")
            )
        )
    )

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

        val allCompaniesInStore = testManager.listCompanies("", null, true)
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
            val searchResponse = testManager.listCompanies(company.companyName, null, true)
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
                val searchResponse = testManager.listCompanies(identifier.value, null, false)
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
                val searchResponse = testManager.listCompanies("", stockIndex, true)
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
        val searchResponse = testManager.listCompanies("de", null, false)
        assertEquals(
            3, searchResponse.size,
            "There are 3 companies containing 'de' (in name or identifier) but found ${searchResponse.size}."
        )
    }

    @Test
    fun `search for companies containing de and DAX index and check if it returns one company`() {
        for (company in testCompanyList) {
            testManager.addCompany(company)
        }
        val searchResponse = testManager.listCompanies("de", CompanyInformation.StockIndex.DAX, false)
        assertEquals(
            1, searchResponse.size,
            "There is 1 company containing 'de' (in name or identifier) with DAX index but found ${searchResponse.size}."
        )
    }

    @Test
    fun `get the data sets for a company id that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testManager.searchDataMetaInfo(companyId = "error")
        }
    }
}
