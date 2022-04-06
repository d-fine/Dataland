package org.dataland.datalandbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.model.CompanyInformation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
class DataManagerTest(
    @Autowired var edcClient: DefaultApi,
    @Autowired var objectMapper: ObjectMapper
) {

    val testManager = DataManager(edcClient, objectMapper)

    val testCompanyList = listOf(
        CompanyInformation(
            companyName = "Test-Company_1",
            headquarters = "Test-Headquarters_1",
            sector = "Test-Sector_1",
            marketCap = BigDecimal(100),
            reportingDateOfMarketCap = LocalDate.now()
        ),
        CompanyInformation(
            companyName = "Test-Company_2",
            headquarters = "Test-Headquarters_2",
            sector = "Test-Sector_2",
            marketCap = BigDecimal(200),
            reportingDateOfMarketCap = LocalDate.now()
        )
    )

    @Test
    fun `add the first company and check if its name is as expected by using the return value of addCompany`() {
        val companyMetaInformation = testManager.addCompany(testCompanyList[0])
        assertEquals(
            companyMetaInformation.companyInformation.companyName, testCompanyList[0].companyName,
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
                testCompanyList[counter].companyName, storedCompany.companyInformation.companyName,
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
                company.companyName, searchResponse.first().companyInformation.companyName,
                "The posted company could not be found in the data store by searching for its name."
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
