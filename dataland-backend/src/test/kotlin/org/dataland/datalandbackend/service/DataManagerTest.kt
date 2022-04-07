package org.dataland.datalandbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.junit.jupiter.api.Assertions.assertEquals
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
            companyName = "Test-Company_1",
            headquarters = "Test-Headquarters_1",
            sector = "Test-Sector_1",
            marketCap = BigDecimal(100),
            reportingDateOfMarketCap = LocalDate.now(),
            indices = listOf(CompanyInformation.StockIndex.MDAX),
            identifiers = mapOf(
                Pair(CompanyInformation.Identifier.ISIN, "DE0987654321"),
                Pair(CompanyInformation.Identifier.LEI, "BLA")
            )
        ),
        CompanyInformation(
            companyName = "Test-Company_2",
            headquarters = "Test-Headquarters_2",
            sector = "Test-Sector_2",
            marketCap = BigDecimal(200),
            reportingDateOfMarketCap = LocalDate.now(),
            indices = listOf(CompanyInformation.StockIndex.DAX),
            identifiers = mapOf(
                Pair(CompanyInformation.Identifier.ISIN, "DE1337"),
                Pair(CompanyInformation.Identifier.LEI, "BLUB")
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

        val allCompaniesInStore = testManager.listCompaniesByName("")
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
            val searchResponse = testManager.listCompaniesByName(company.companyName)
            assertEquals(
                company.companyName, searchResponse.first().companyInformation.companyName,
                "The posted company could not be retrieved by searching for its name."
            )
        }
    }

    @Test
    fun `get the data sets for a company id that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testManager.searchDataMetaInfo(companyId = "error")
        }
    }
}
