package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.interfaces.CompanyManagerInterface
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.company.StockIndex
import org.dataland.datalandbackend.utils.TestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import javax.transaction.Transactional

@SpringBootTest(classes = [DatalandBackend::class])
@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class CompanyManagerTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val testCompanyManager: CompanyManagerInterface
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
    @Transactional
    fun `add sample company and check if it can be retrieved by using the company ID that is returned`() {
        val testCompanyData = testDataProvider.getCompanyInformationWithoutIdentifiers(1).last()
        val testCompanyId = testCompanyManager.addCompany(testCompanyData).companyId
        assertEquals(
            StoredCompany(testCompanyId, testCompanyData, mutableListOf()),
            testCompanyManager.getCompanyById(testCompanyId).toApiModel(),
            "The company behind the company ID in the post-response " +
                "does not contain company information of the posted company."
        )
    }

    @Test
    @Transactional
    fun `retrieve companies as a list and check for each company if it can be found as expected`() {
        val allCompaniesInStore = testCompanyManager.searchCompanies("", true, setOf(), setOf())
        assertTrue(
            allCompaniesInStore.all {
                val apiModel = it.toApiModel().companyInformation
                testCompanyList.any { testCompany -> testCompany.companyName == apiModel.companyName }
            },
            "Not all the companyInformation of the posted companies could be found in the stored companies."
        )
    }

    @Test
    @Transactional
    fun `search for them one by one by using their names`() {
        for (company in testCompanyList) {
            val searchResponse = testCompanyManager.searchCompanies(company.companyName, true, setOf(), setOf())
            assertTrue(
                searchResponse.any { it.companyName == company.companyName },
                "The posted company could not be retrieved by searching for its name."
            )
        }
    }

    private fun testThatSearchForCompanyIdentifierWorks(identifier: CompanyIdentifier) {
        val searchResponse = testCompanyManager.searchCompanies(
            identifier.identifierValue,
            false,
            setOf(),
            setOf()
        )
            .toMutableList()
        // The response list is filtered to exclude results that match in account of another identifier having
        // the required value but the looked for identifier type does not exist (This happens due to the test
        // data having non-unique identifier values for different identifier types)
        searchResponse.retainAll {
            it.identifiers.any {
                    identifierInResponse ->
                identifierInResponse.identifierType == identifier.identifierType
            }
        }
        assertTrue(
            searchResponse.all { it.identifiers.any { it.toApiModel() == identifier } },
            "The search by identifier returns at least one company that does not contain the looked" +
                "for value $identifier."
        )
    }

    @Test
    @Transactional
    fun `search for all identifier values and check if all results contain the looked for value`() {
        for (company in testCompanyList) {
            for (identifier in company.identifiers) {
                testThatSearchForCompanyIdentifierWorks(identifier)
            }
        }
    }

    @Test
    @Transactional
    fun `verify that searching for stock indices returns correct results`() {
        val stockIndiciesInTestData = mutableSetOf<StockIndex>()
        for (company in testCompanyList) {
            stockIndiciesInTestData += company.indices
        }

        for (stockIndex in stockIndiciesInTestData) {
            val searchResponse = testCompanyManager.searchCompanies("", false, setOf(), setOf(stockIndex))
            assertTrue(
                searchResponse.all { it.indices.any { index -> index.toApiModel() == stockIndex } },
                "The search result for the stock index $stockIndex contains at least one company " +
                    "that does not have $stockIndex as index attribute."
            )
        }
    }

    @Test
    @Transactional
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
        val searchResponse = testCompanyManager.searchCompanies(searchString, false, setOf(), setOf())
        assertEquals(
            occurencesOfSearchString, searchResponse.size,
            "There are $occurencesOfSearchString expected matches but found ${searchResponse.size}."
        )
    }

    @Test
    @Transactional
    fun `search for name substring to verify substring matching in company search`() {
        val searchString = testCompanyList.first().companyName.drop(1).dropLast(1)
        var occurencesOfSearchString = 0
        for (companyInformation in testCompanyList) {
            if (companyInformation.companyName.contains(searchString)) {
                occurencesOfSearchString += 1
            }
        }
        val searchResponse = testCompanyManager.searchCompanies(searchString, true, setOf(), setOf())
        assertEquals(
            occurencesOfSearchString, searchResponse.size,
            "There are $occurencesOfSearchString expected matches but found ${searchResponse.size}."
        )
    }

    @Test
    @Transactional
    fun `search for name substring to check the ordering of results`() {
        val searchString = testCompanyList.first().companyName.take(1)
        val searchResponse = testCompanyManager.searchCompanies(searchString, true, setOf(), setOf())
        val responsesStartingWith =
            searchResponse.takeWhile { it.companyName.startsWith(searchString) }
        val otherResponses = searchResponse.dropWhile { it.companyName.startsWith(searchString) }
        assertTrue(
            otherResponses.none { it.companyName.startsWith(searchString) },
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

    @Test
    @Transactional
    fun `check that the number of results when searching for DAX index is as expected`() {
        var expectedResult = 0
        val testIndex = StockIndex.Dax
        for (company in testCompanyList) {
            if (company.indices.contains(testIndex)) {
                expectedResult ++
            }
        }
        val searchResponse = testCompanyManager.searchCompanies("", false, setOf(), setOf(testIndex))
        assertEquals(
            expectedResult, searchResponse.size,
            "There are $expectedResult companies with DAX index but found ${searchResponse.size}."
        )
    }
}
