package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.companies.CompanyIdentifierValidationResult
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.utils.TestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class CompanyManagerTest
    @Autowired
    constructor(
        objectMapper: ObjectMapper,
        private val testCompanyAlterationManager: CompanyAlterationManager,
        private val testCompanyQueryManager: CompanyQueryManager,
    ) {
        private val testDataProvider = TestDataProvider(objectMapper)
        private val testCompanyList = testDataProvider.getCompanyInformation(4)
        private val resultLimit = 100

        @BeforeEach
        fun addTestCompanies() {
            for (company in testCompanyList) {
                testCompanyAlterationManager.addCompany(company)
            }
        }

        @Test
        fun `add sample company and check if it can be retrieved by using the company ID that is returned`() {
            val testCompanyData = testDataProvider.getCompanyInformationWithoutIdentifiers(1).last()
            val testCompanyId = testCompanyAlterationManager.addCompany(testCompanyData).companyId
            assertEquals(
                StoredCompany(testCompanyId, testCompanyData, mutableListOf()),
                testCompanyQueryManager.getCompanyApiModelById(testCompanyId),
                "The company behind the company ID in the post-response " +
                    "does not contain company information of the posted company.",
            )
        }

        @Test
        fun `retrieve companies as a list and check for each company if it can be found as expected`() {
            val allCompaniesInStore =
                testCompanyQueryManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
                    "", resultLimit,
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
                val searchResponse =
                    testCompanyQueryManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
                        searchString = company.companyName, resultLimit,
                    )
                assertTrue(
                    searchResponse.any { it.companyName == company.companyName },
                    "The posted company could not be retrieved by searching for its name.",
                )
            }
        }

        @Test
        fun `search for identifier substring to verify substring matching in company search`() {
            val searchString =
                testCompanyList
                    .first()
                    .identifiers.values
                    .first { it.isNotEmpty() }
                    .first()
                    .drop(1)
                    .dropLast(1)
            var occurencesOfSearchString = 0
            for (companyInformation in testCompanyList) {
                require(!(companyInformation.companyName.contains(searchString))) {
                    "The company name " +
                        "${companyInformation.companyName} includes the searchString $searchString."
                }
                for (identifier in companyInformation.identifiers.flatMap { it.value }) {
                    if (identifier.contains(searchString)) {
                        occurencesOfSearchString += 1
                    }
                }
            }
            val searchResponse =
                testCompanyQueryManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
                    searchString, resultLimit,
                )
            assertEquals(
                occurencesOfSearchString,
                searchResponse.size,
                "There are $occurencesOfSearchString expected matches but found ${searchResponse.size}.",
            )
        }

        @Test
        fun `search for name substring to verify substring matching in company search`() {
            val searchString =
                testCompanyList
                    .first()
                    .companyName
                    .drop(1)
                    .dropLast(1)
            var occurencesOfSearchString = 0
            for (companyInformation in testCompanyList) {
                if (companyInformation.companyName.contains(searchString)) {
                    occurencesOfSearchString += 1
                }
            }
            val searchResponse =
                testCompanyQueryManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
                    searchString = searchString, resultLimit,
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
            val searchResponse =
                testCompanyQueryManager.searchCompaniesByNameOrIdentifierAndGetApiModel(
                    searchString = searchString, resultLimit,
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

        @Test
        fun `check if the companies search via name and ids sorts the results as expected`() {
            val testString = "unique-test-string-${UUID.randomUUID()}"
            uploadCompaniesInReverseToExpectedOrder(testString)
            val sortedCompanyNames =
                testCompanyQueryManager
                    .searchCompaniesByNameOrIdentifierAndGetApiModel(
                        searchString = testString, resultLimit,
                    ).map { it.companyName }
            assertEquals(
                listOf(testString, company2, "${testString}2", company5, company6, "3$testString", company9),
                sortedCompanyNames.filter { it != company8 && it != company3 },
            )
            assertEquals(
                listOf(company3, company2, "${testString}2", company5, company6, company8, company9),
                sortedCompanyNames.filter { it != "3$testString" && it != testString },
            )

            val otherCompanyNames =
                testCompanyQueryManager
                    .searchCompaniesByNameOrIdentifierAndGetApiModel(
                        searchString = "other_name", resultLimit,
                    ).map { it.companyName }
            assertTrue(otherCompanyNames.contains(company8))
            Assertions.assertFalse(otherCompanyNames.contains("Company 7"))
        }

        private val company2 = "Company 2"
        private val company3 = "Company 3"
        private val company5 = "Company 5"
        private val company6 = "Company 6"
        private val company8 = "Company 8"
        private val company9 = "Company 9"
        val baseCompanyInformation =
            CompanyInformation(
                companyName = "Placholder Inc.",
                countryCode = "DE",
                companyAlternativeNames = null,
                companyContactDetails = null,
                companyLegalForm = null,
                headquarters = "Frankfurt",
                headquartersPostalCode = "60313",
                sector = null,
                sectorCodeWz = null,
                website = null,
                identifiers = mapOf(),
                isTeaserCompany = false,
                parentCompanyLei = "dummyParentCompanyLei",
            )

        private fun uploadCompaniesInReverseToExpectedOrder(expectedSearchString: String) {
            uploadModifiedBaseCompany(company9, null, "3$expectedSearchString")
            uploadModifiedBaseCompany(company8, listOf("3$expectedSearchString", "other_name"), null)
            uploadModifiedBaseCompany("3$expectedSearchString", null, null)
            uploadModifiedBaseCompany(company6, null, "${expectedSearchString}2")
            uploadModifiedBaseCompany(company5, listOf("${expectedSearchString}2"), null)
            uploadModifiedBaseCompany("${expectedSearchString}2", null, null)
            uploadModifiedBaseCompany(company3, null, expectedSearchString)
            uploadModifiedBaseCompany(company2, listOf(expectedSearchString), null)
            uploadModifiedBaseCompany(expectedSearchString, null, null)
        }

        private fun uploadModifiedBaseCompany(
            name: String,
            alternativeNames: List<String>?,
            identifier: String?,
        ): String {
            val companyInformation =
                baseCompanyInformation.copy(
                    companyName = name,
                    companyAlternativeNames = alternativeNames,
                    identifiers =
                        mapOf(
                            IdentifierType.Isin to listOf(identifier ?: UUID.randomUUID().toString()),
                        ),
                )
            return testCompanyAlterationManager.addCompany(companyInformation).companyId
        }

        @Test
        fun `verify that identifier validation returns nothing when input is unknown to the system`() {
            val testData = listOf("1", "just a string", "1234567890", "ab")
            val validationResults = testCompanyQueryManager.validateCompanyIdentifiers(testData)
            assertEquals(testData.size, validationResults.size)
            assertTrue(validationResults.all { it.companyId.isNullOrEmpty() and it.companyId.isNullOrEmpty() })
        }

        @Test
        fun `verify that identifier validation returns results for values known to the system`() {
            val expectedResults = mutableListOf<CompanyIdentifierValidationResult>()
            val testData = mutableListOf<String>()
            testCompanyList.forEach {
                val identifier =
                    it.identifiers.values
                        .first { values -> values.isNotEmpty() }
                        .first()
                testData.add(identifier)
                expectedResults.add(
                    CompanyIdentifierValidationResult(
                        identifier = identifier,
                        companyId =
                            testCompanyQueryManager
                                .searchCompaniesByNameOrIdentifierAndGetApiModel(identifier, 1)
                                .first()
                                .companyId,
                        companyName = it.companyName,
                        sector = it.sector,
                        countryCode = it.countryCode,
                    ),
                )
            }
            assertEquals(expectedResults, testCompanyQueryManager.validateCompanyIdentifiers(testData))
        }

        @Test
        fun `verify that duplicate identifiers lead to only entry in the validation results`() {
            val expectedResults = mutableListOf<CompanyIdentifierValidationResult>()
            val testData = mutableListOf<String>()
            val testCompany = testCompanyList.first()
            val identifier =
                testCompany.identifiers.values
                    .first { values -> values.isNotEmpty() }
                    .first()
            testData.addAll(listOf(identifier, identifier))
            expectedResults.add(
                CompanyIdentifierValidationResult(
                    identifier = identifier,
                    companyId =
                        testCompanyQueryManager
                            .searchCompaniesByNameOrIdentifierAndGetApiModel(identifier, 1)
                            .first()
                            .companyId,
                    companyName = testCompany.companyName,
                    sector = testCompany.sector,
                    countryCode = testCompany.countryCode,
                ),
            )
            assertEquals(expectedResults, testCompanyQueryManager.validateCompanyIdentifiers(testData))
        }
    }
