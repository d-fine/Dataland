package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
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
    @Autowired val testCompanyManager: CompanyManager,
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
                "does not contain company information of the posted company.",
        )
    }

    @Test
    fun `retrieve companies as a list and check for each company if it can be found as expected`() {
        val allCompaniesInStore = testCompanyManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
            "",
        )
        assertTrue(
            allCompaniesInStore.all {
                testCompanyList.any { testCompany -> testCompany.companyName == it.companyName }
            },
            "Not all the companyInformation of the posted companies could be found in the stored companies.",
        )
    }

    @Test
    fun `search for them one by one by using their names`() {
        for (company in testCompanyList) {
            val searchResponse = testCompanyManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
                searchString = company.companyName,
            )
            assertTrue(
                searchResponse.any { it.companyName == company.companyName },
                "The posted company could not be retrieved by searching for its name.",
            )
        }
    }

    @Test
    fun `search for identifier substring to verify substring matching in company search`() {
        val searchString = testCompanyList.first().identifiers.values
            .first { it.isNotEmpty() }.first().drop(1).dropLast(1)
        var occurencesOfSearchString = 0
        for (companyInformation in testCompanyList) {
            require(!(companyInformation.companyName.contains(searchString))) {
                "The company name " +
                    "${companyInformation.companyName} includes the searchString $searchString."
            }
            for (identifier in companyInformation.identifiers.flatMap { it.value }) {
                if (identifier.contains(searchString)) { occurencesOfSearchString += 1 }
            }
        }
        val searchResponse = testCompanyManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
            searchString,
        )
        assertEquals(
            occurencesOfSearchString,
            searchResponse.size,
            "There are $occurencesOfSearchString expected matches but found ${searchResponse.size}.",
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
        val searchResponse = testCompanyManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
            searchString = searchString,
        )
        assertEquals(
            occurencesOfSearchString,
            searchResponse.size,
            "There are $occurencesOfSearchString expected matches but found ${searchResponse.size}.",
        )
    }

    @Test
    fun `search for name substring to check the ordering of results`() {
        val searchString = testCompanyList.first().companyName.take(1)
        val searchResponse = testCompanyManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
            searchString = searchString,
        )
        val responsesStartingWith =
            searchResponse.takeWhile { it.companyName.startsWith(searchString) }
        val otherResponses = searchResponse.dropWhile { it.companyName.startsWith(searchString) }
        assertTrue(
            otherResponses.none { it.companyName.startsWith(searchString) },
            "Expected to have matches ordered by starting with search string followed by all other results." +
                "However, at least one of the matches in the other results starts with the search string " +
                "($searchString).",
        )
        assertTrue(
            responsesStartingWith.isNotEmpty(),
            "No matches starting with the search string " +
                "$searchString were returned. At least one was expected.",
        )
    }
}
