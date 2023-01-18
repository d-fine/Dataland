package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.CompanySearchFilter
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.utils.TestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatalandBackend::class])
@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class CompanyManagerTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val testCompanyManager: CompanyManager
) {
    val testDataProvider = TestDataProvider(objectMapper)
    val testCompanyList = testDataProvider.getCompanyInformation(4)

    @BeforeEach
    fun addTestCompanies() {
        for (company in testCompanyList) {
            testCompanyManager.addCompany(company)
        }
    }

    @Test
    fun `add sample company and check if it can be retrieved by using the company ID that is returned`() {
        val testCompanyData = testDataProvider.getCompanyInformationWithoutIdentifiers(1).last()
        val testCompanyId = testCompanyManager.addCompany(testCompanyData).companyId
        assertEquals(
            StoredCompany(testCompanyId, testCompanyData, mutableListOf()),
            testCompanyManager.getCompanyApiModelById(testCompanyId),
            "The company behind the company ID in the post-response " +
                "does not contain company information of the posted company."
        )
    }

    @Test
    fun `retrieve companies as a list and check for each company if it can be found as expected`() {
        val allCompaniesInStore = testCompanyManager.searchCompaniesAndGetApiModel(CompanySearchFilter())
        assertTrue(
            allCompaniesInStore.all {
                testCompanyList.any { testCompany -> testCompany.companyName == it.companyInformation.companyName }
            },
            "Not all the companyInformation of the posted companies could be found in the stored companies."
        )
    }

    @Test
    fun `search for them one by one by using their names`() {
        for (company in testCompanyList) {
            val searchResponse = testCompanyManager.searchCompaniesAndGetApiModel(
                CompanySearchFilter(
                    searchString = company.companyName,
                    onlyCompanyNames = true,
                )
            )
            assertTrue(
                searchResponse.any { it.companyInformation.companyName == company.companyName },
                "The posted company could not be retrieved by searching for its name."
            )
        }
    }

    private fun testThatSearchForCompanyIdentifierWorks(identifier: CompanyIdentifier) {
        val searchResponse = testCompanyManager.searchCompaniesAndGetApiModel(
            CompanySearchFilter(
                searchString = identifier.identifierValue,
                onlyCompanyNames = false,
            )
        )
            .toMutableList()
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
            searchResponse.all { results -> results.companyInformation.identifiers.any { it == identifier } },
            "The search by identifier returns at least one company that does not contain the looked" +
                "for value $identifier."
        )
    }

    @Test
    fun `search for all identifier values and check if all results contain the looked for value`() {
        for (company in testCompanyList) {
            for (identifier in company.identifiers) {
                testThatSearchForCompanyIdentifierWorks(identifier)
            }
        }
    }

    @Test
    fun `search for identifier substring to verify substring matching in company search`() {
        val searchString = testCompanyList.first().identifiers.first().identifierValue.drop(1).dropLast(1)
        var occurencesOfSearchString = 0
        for (companyInformation in testCompanyList) {
            if (companyInformation.companyName.contains(searchString)) {
                throw IllegalArgumentException(
                    "The company name ${companyInformation.companyName} includes the searchString $searchString."
                )
            }
            for (identifier in companyInformation.identifiers) {
                if (identifier.identifierValue.contains(searchString)) { occurencesOfSearchString += 1 }
            }
        }
        val searchResponse = testCompanyManager.searchCompaniesAndGetApiModel(CompanySearchFilter(searchString))
        assertEquals(
            occurencesOfSearchString, searchResponse.size,
            "There are $occurencesOfSearchString expected matches but found ${searchResponse.size}."
        )
    }

    @Test
    fun `search for name substring to verify substring matching in company search`() {
        val searchString = testCompanyList.first().companyName.drop(1).dropLast(1)
        var occurencesOfSearchString = 0
        for (companyInformation in testCompanyList) {
            if (companyInformation.companyName.contains(searchString)) {
                occurencesOfSearchString += 1
            }
        }
        val searchResponse = testCompanyManager.searchCompaniesAndGetApiModel(
            CompanySearchFilter(searchString = searchString, onlyCompanyNames = true)
        )
        assertEquals(
            occurencesOfSearchString, searchResponse.size,
            "There are $occurencesOfSearchString expected matches but found ${searchResponse.size}."
        )
    }

    @Test
    fun `search for name substring to check the ordering of results`() {
        val searchString = testCompanyList.first().companyName.take(1)
        val searchResponse = testCompanyManager.searchCompaniesAndGetApiModel(
            CompanySearchFilter(searchString = searchString, onlyCompanyNames = true)
        )
        val responsesStartingWith =
            searchResponse.takeWhile { it.companyInformation.companyName.startsWith(searchString) }
        val otherResponses = searchResponse.dropWhile { it.companyInformation.companyName.startsWith(searchString) }
        assertTrue(
            otherResponses.none { it.companyInformation.companyName.startsWith(searchString) },
            "Expected to have matches ordered by starting with search string followed by all other results." +
                "However, at least one of the matches in the other results starts with the search string " +
                "($searchString)."
        )
        assertTrue(
            responsesStartingWith.isNotEmpty(),
            "No matches starting with the search string " +
                "$searchString were returned. At least one was expected."
        )
    }
}
