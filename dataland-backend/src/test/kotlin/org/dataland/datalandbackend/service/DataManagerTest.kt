package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DataManagerTest {

    val testManager = DataManager(edcClient = DefaultApi(basePath = "dummy"))
    val testCompanyNamesToStore = listOf("Imaginary-Company_I", "Fantasy-Company_II", "Dream-Company_III")

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
}
