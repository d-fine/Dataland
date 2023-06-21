package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class CompanyDataControllerTest {

    private val apiAccessor = ApiAccessor()
    private val company1 = "Company 1"
    private val company2 = "Company 2"
    private val company3 = "Company 3"
    private val company5 = "Company 5"
    private val company6 = "Company 6"
    private val company8 = "Company 8"
    private val company9 = "Company 9"

    @Test
    fun `post a dummy company and check if post was successful`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        assertEquals(
            uploadInfo.inputCompanyInformation,
            uploadInfo.actualStoredCompany.companyInformation,
            "The company information in the post-response does not match " +
                "the actual information of the company to be posted.",
        )
        assertTrue(
            uploadInfo.actualStoredCompany.companyId.isNotEmpty(),
            "No valid company Id was assigned to the posted company.",
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its company Id`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val expectedStoredCompany = StoredCompany(
            uploadInfo.actualStoredCompany.companyId,
            uploadInfo.inputCompanyInformation,
            emptyList(),
        )
        assertEquals(
            expectedStoredCompany,
            apiAccessor.companyDataControllerApi.getCompanyById(uploadInfo.actualStoredCompany.companyId),
            "Dataland does not contain the posted company.",
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val expectedDataset = apiAccessor.uploadSingleFrameworkDataSet(
            companyId = uploadInfo.actualStoredCompany.companyId,
            frameworkData = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)[0],
            reportingPeriod = "2023",
            frameworkDataUploadFunction = apiAccessor::euTaxonomyFinancialsUploaderFunction,
        ).copy(qaStatus = QaStatus.accepted, currentlyActive = true, uploaderUserId = null)
        val getCompaniesOnlyByNameResponse = apiAccessor.getCompaniesOnlyByName(
            uploadInfo.actualStoredCompany.companyInformation.companyName,
        )
        val expectedCompany = StoredCompany(
            uploadInfo.actualStoredCompany.companyId,
            uploadInfo.actualStoredCompany.companyInformation,
            listOf(expectedDataset),
        )
        println(expectedCompany)
        println(getCompaniesOnlyByNameResponse)
        assertTrue(
            getCompaniesOnlyByNameResponse.contains(expectedCompany),
            "Dataland does not contain the posted company.",
        )
    }

    @Test
    fun `post two dummy companies with framework data and check if the distinct endpoint returns all values`() {
        val listOfTestCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(2)
        apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials to listOfTestCompanyInformation),
            1,
        )
        val distinctValues = apiAccessor.companyDataControllerApi.getAvailableCompanySearchFilters()
        assertTrue(
            distinctValues.sectors.containsAll(listOfTestCompanyInformation.map { it.sector }),
            "The list of all occurring sectors does not contain the sectors of the posted companies.",
        )
        assertTrue(
            distinctValues.countryCodes.containsAll(listOfTestCompanyInformation.map { it.countryCode }),
            "The list of all occurring country codes does not contain the country codes of the posted companies.",
        )
    }

    @Test
    fun `post dummy companies with frontendExcluded framework data and check if the distinct endpoint ignores`() {
        val mapOfAllBackendOnlyDataTypesToListOfOneCompanyInformation = apiAccessor.generalTestDataProvider
            .generateOneCompanyInformationPerBackendOnlyFramework()
        apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOfAllBackendOnlyDataTypesToListOfOneCompanyInformation,
            1,
        )
        val distinctValues = apiAccessor.companyDataControllerApi.getAvailableCompanySearchFilters()
        assertTrue(
            distinctValues.sectors.intersect(
                mapOfAllBackendOnlyDataTypesToListOfOneCompanyInformation.map { it.value[0].sector }.toSet(),
            ).isEmpty(),
            "At least one sector of the frontend-excluded data sets appears in the distinct sector value list.",
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its country code and sector`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val expectedDataMetaInfo = apiAccessor.uploadSingleFrameworkDataSet(
            companyId = uploadInfo.actualStoredCompany.companyId,
            frameworkData = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)[0],
            reportingPeriod = "2023",
            frameworkDataUploadFunction = apiAccessor::euTaxonomyFinancialsUploaderFunction,
        )
        val expectedStoredCompany = uploadInfo.actualStoredCompany
            .copy(dataRegisteredByDataland = listOf(expectedDataMetaInfo))
        val getCompaniesByCountryCodeAndSectorResponse = apiAccessor.companyDataControllerApi.getCompanies(
            apiAccessor.frameworkData,
            sectors = if (uploadInfo.actualStoredCompany.companyInformation.sector != null) {
                setOf(uploadInfo.actualStoredCompany.companyInformation.sector!!) } else { null },
            countryCodes = setOf(uploadInfo.actualStoredCompany.companyInformation.countryCode),
        )
        assertTrue(
            getCompaniesByCountryCodeAndSectorResponse.contains(expectedStoredCompany),
            "The posted company could not be found in the query results when querying for its country code and sector.",
        )
    }

    @Test
    fun `post a dummy company and check that it is not returned if filtered by a different sector`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val getCompaniesByCountryCodeAndSectorResponse = apiAccessor.companyDataControllerApi.getCompanies(
            apiAccessor.frameworkData,
            sectors = setOf("${uploadInfo.actualStoredCompany.companyInformation.sector}a"),
            countryCodes = setOf(uploadInfo.actualStoredCompany.companyInformation.countryCode),
        )
        assertFalse(
            getCompaniesByCountryCodeAndSectorResponse.contains(uploadInfo.actualStoredCompany),
            "The posted company is in the query results," +
                " even though the country code filter was set to a different country code.",

        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {
        val allCompaniesListSizeBefore = apiAccessor.getNumberOfStoredCompanies()
        val listOfUploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(3)
        listOfUploadInfo.forEach {
            apiAccessor.uploadSingleFrameworkDataSet(
                companyId = it.actualStoredCompany.companyId,
                frameworkData = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)[0],
                reportingPeriod = "2023",
                frameworkDataUploadFunction = apiAccessor::euTaxonomyFinancialsUploaderFunction,
            )
        }
        val allCompaniesListSizeAfter = apiAccessor.getNumberOfStoredCompanies()
        assertEquals(
            listOfUploadInfo.size,
            allCompaniesListSizeAfter - allCompaniesListSizeBefore,
            "The size of the all-companies-list did not increase by ${listOfUploadInfo.size}.",
        )
    }

    @Test
    fun `post a dummy company and check if it can be searched for by identifier at the right time`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertTrue(
            apiAccessor.companyDataControllerApi.getCompanies(
                apiAccessor.frameworkData,
                searchString = uploadInfo.inputCompanyInformation.identifiers.first().identifierValue,
                onlyCompanyNames = false,
            ).isEmpty(),
            "The posted company was found in the query results.",
        )
        apiAccessor.companyDataControllerApi.existsIdentifier(
            CompanyDataControllerApi.IdentifierType_existsIdentifier.permId,
            uploadInfo.inputCompanyInformation.identifiers.first().identifierValue,
        )
        apiAccessor.uploadSingleFrameworkDataSet(
            companyId = uploadInfo.actualStoredCompany.companyId,
            frameworkData = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)[0],
            reportingPeriod = "2023",
            frameworkDataUploadFunction = apiAccessor::euTaxonomyFinancialsUploaderFunction,
        )
        assertTrue(
            apiAccessor.companyDataControllerApi.getCompanies(
                apiAccessor.frameworkData,
                searchString = uploadInfo.inputCompanyInformation.identifiers.first().identifierValue,
                onlyCompanyNames = false,
            ).any { it.companyId == uploadInfo.actualStoredCompany.companyId },
            "The posted company could not be found in the query results when querying for its first identifiers value.",
        )
    }

    @Test
    fun `post a dummy company as teaser company and test if it is retrievable by company ID as unauthorized user`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(true)
        val getCompanyByIdResponse = apiAccessor.unauthorizedCompanyDataControllerApi.getCompanyById(
            uploadInfo.actualStoredCompany.companyId,
        )
        val expectedStoredTeaserCompany = StoredCompany(
            companyId = uploadInfo.actualStoredCompany.companyId,
            companyInformation = uploadInfo.inputCompanyInformation,
            dataRegisteredByDataland = emptyList(),
        )
        assertEquals(
            expectedStoredTeaserCompany,
            getCompanyByIdResponse,
            "The posted company does not equal the teaser company.",
        )
    }

    @Test
    fun `post a dummy company and test if it cannot be retrieved by its company ID as unauthorized user`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val exception = assertThrows<IllegalArgumentException> {
            apiAccessor.unauthorizedCompanyDataControllerApi.getCompanyById(
                uploadInfo.actualStoredCompany.companyId,
            )
        }
        assertTrue(
            exception.message!!.contains("Unauthorized access failed"),
            "The exception message does not say that an unauthorized access was the cause.",
        )
    }

    @Test
    fun `post a dummy company as a user type which does not have the rights to do so and receive an error code 403`() {
        val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val exception =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi.postCompany(testCompanyInformation)
                    .companyId
            }
        assertEquals(
            "Client error : 403 ",
            exception.message,
            "The exception message does not say that a 403 client error was the cause.",
        )
    }

    @Test
    fun `post a dummy company twice and receive the expected error code and message`() {
        val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithRandomIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        apiAccessor.companyDataControllerApi.postCompany(testCompanyInformation)
        val response = apiAccessor.companyDataControllerApi.postCompanyWithHttpInfo(testCompanyInformation)
            as ClientError

        assertEquals(
            400,
            response.statusCode,
            "The status code is ${response.statusCode} instead of the expected 400.",
        )
        assertTrue(
            response.body.toString().contains("Could not insert company as one company identifier is already used"),
            "The response message is not as expected.",
        )
    }

    val baseCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithRandomIdentifiers(1).first()

    @Test
    fun `check if the new companies search via name and ids endpoint works as expected`() {
        val testString = "unique-test-string-${UUID.randomUUID()}"
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        uploadModifiedBaseCompany(company9, null, "3$testString")
        uploadModifiedBaseCompany(company8, listOf("3$testString", "other_name"), null)
        uploadModifiedBaseCompany("3$testString", null, null)
        uploadModifiedBaseCompany(company6, null, "${testString}2")
        uploadModifiedBaseCompany(company5, listOf("${testString}2"), null)
        uploadModifiedBaseCompany("${testString}2", null, null)
        uploadModifiedBaseCompany(company3, null, testString)
        uploadModifiedBaseCompany(company2, listOf(testString), null)
        uploadModifiedBaseCompany(testString, null, null)
        val sortedCompanyNames = apiAccessor.companyDataControllerApi.getCompaniesBySearchString(
            searchString = testString,
        ).map { it.companyName }
        assertEquals(
            listOf(testString, company2, "${testString}2", company5, company6, "3$testString", company9),
            sortedCompanyNames.filter { it != company8 && it != company3 },
        )
        assertEquals(
            listOf(company3, company2, "${testString}2", company5, company6, company8, company9),
            sortedCompanyNames.filter { it != "3$testString" && it != testString },
        )

        val otherCompanyNames = apiAccessor.companyDataControllerApi.getCompaniesBySearchString(
            searchString = "other_name",
        ).map { it.companyName }
        assertTrue(otherCompanyNames.contains(company8))
        assertFalse(otherCompanyNames.contains("Company 7"))
    }

    private fun uploadModifiedBaseCompany(name: String, alternativeNames: List<String>?, identifier: String?):
        CompanyInformation {
        val companyInformation = baseCompanyInformation.copy(
            companyName = name,
            companyAlternativeNames = alternativeNames,
            identifiers = listOf(
                CompanyIdentifier(
                    CompanyIdentifier.IdentifierType.isin,
                    identifier ?: UUID.randomUUID().toString(),
                ),
            ),
        )
        apiAccessor.companyDataControllerApi.postCompany(companyInformation)
        return (companyInformation)
    }

    private fun testThatSearchForCompanyIdentifierWorks(identifier: CompanyIdentifier) {
        val searchResponse = apiAccessor.companyDataControllerApi.getCompanies(
            dataTypes = apiAccessor.frameworkData,
            searchString = identifier.identifierValue,
            onlyCompanyNames = false,

        )
            .toMutableList()
        println(searchResponse)
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
                "for value $identifier.",
        )
    }
 //TODO this test is broken, searchRespnses are empty
    @Test
    fun `search for all identifier values and check if all results contain the looked for value`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val testString = "unique-test-string-${UUID.randomUUID()}"
        val testCompanyList = listOf(
            uploadModifiedBaseCompany(company1, listOf(testString), null),
            uploadModifiedBaseCompany(company1, listOf(testString), null),
            uploadModifiedBaseCompany(company1, listOf(testString), null),
            uploadModifiedBaseCompany(company1, listOf(testString), null),
        )
        for (company in testCompanyList) {
            for (identifier in company.identifiers) {
                testThatSearchForCompanyIdentifierWorks(identifier)
            }
        }
    }

    // TODO Recreate the old unit tests for the old getcompanies endpoint here
    @Test
    fun `retrieve companies as a list and check for each company if it can be found as expected`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val companyIdentifier = listOf(
            CompanyIdentifier(
                CompanyIdentifier.IdentifierType.lei,
                UUID.randomUUID().toString(),
            ),
        )
        val companyInformation = CompanyInformation(
            "retrieve empty search string", "", companyIdentifier, "",
            listOf(),
        )
        val uploadedCompany = apiAccessor.companyDataControllerApi.postCompany(companyInformation)
        val uploadedData = apiAccessor.uploadSingleFrameworkDataSet(
            companyId = uploadedCompany.companyId,
            frameworkData = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)[0],
            reportingPeriod = "2023",
            frameworkDataUploadFunction = apiAccessor::euTaxonomyFinancialsUploaderFunction,
        ).copy(qaStatus = QaStatus.accepted, currentlyActive = true, uploaderUserId = null)
        val expectedCompany = StoredCompany(
            uploadedCompany.companyId,
            uploadedCompany.companyInformation,
            listOf(uploadedData),
        )

        val retrievedCompanies = apiAccessor.getCompaniesOnlyByName("")
        assertTrue(
            retrievedCompanies.contains(expectedCompany),
            "Not all the companyInformation of the posted companies could be found in the stored companies.",
        )
    }

    @Test
    fun `search for identifier and name substring to verify substring matching in company search`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val testIdentifier = UUID.randomUUID().toString()
        val testName = "SubstringSearch"
        val companyIdentifier = listOf(
            CompanyIdentifier(
                CompanyIdentifier.IdentifierType.lei,
                testIdentifier,
            ),
        )
        val companyInformation = CompanyInformation(
            testName, "", companyIdentifier, "",
            listOf(),
        )
        val uploadedCompany = apiAccessor.companyDataControllerApi.postCompany(companyInformation)
        val uploadedData = apiAccessor.uploadSingleFrameworkDataSet(
            companyId = uploadedCompany.companyId,
            frameworkData = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)[0],
            reportingPeriod = "2023",
            frameworkDataUploadFunction = apiAccessor::euTaxonomyFinancialsUploaderFunction,
        ).copy(qaStatus = QaStatus.accepted, currentlyActive = true, uploaderUserId = null)
        val expectedCompany = StoredCompany(
            uploadedCompany.companyId,
            uploadedCompany.companyInformation,
            listOf(uploadedData),
        )

        val searchIdentifier = testIdentifier.drop(1).dropLast(1)
        val searchName = testName.drop(1).dropLast(1)
        val searchResponseForName = apiAccessor.getCompaniesOnlyByName(searchName)
        val searchResponseForIdentifier = apiAccessor.getCompaniesByNameAndIdentifier(searchIdentifier)
        assertTrue(
            searchResponseForName.contains(expectedCompany),
            "The search results do not contain the expected company.",
        )
        assertTrue(
            searchResponseForIdentifier.contains(expectedCompany),
            "The search results do not contain the expected company.",
        )

    }
    @Test
    fun `search for name and check the ordering of results`() {
        val testString = "unique-test-string-${UUID.randomUUID()}"
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val companyList = createCompaniesForTestingOrdering(testString)
        for (company in companyList){
            val uploadedCompany = apiAccessor.companyDataControllerApi.postCompany(company)
            apiAccessor.uploadSingleFrameworkDataSet(
                companyId = uploadedCompany.companyId,
                frameworkData = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)[0],
                reportingPeriod = "2023",
                frameworkDataUploadFunction = apiAccessor::euTaxonomyFinancialsUploaderFunction,
            ).copy(qaStatus = QaStatus.accepted, currentlyActive = true, uploaderUserId = null)
        }
        val sortedCompanyNames = apiAccessor.companyDataControllerApi.getCompaniesBySearchString(
            searchString = testString,
        ).map { it.companyName }
        assertEquals(
            listOf(testString, company2, "${testString}2", company5, company6, "3$testString", company9),
            sortedCompanyNames.filter { it != company8 && it != company3 },
        )
        assertEquals(
            listOf(company3, company2, "${testString}2", company5, company6, company8, company9),
            sortedCompanyNames.filter { it != "3$testString" && it != testString },
        )

        val otherCompanyNames = apiAccessor.companyDataControllerApi.getCompaniesBySearchString(
            searchString = "other_name",
        ).map { it.companyName }
        assertTrue(otherCompanyNames.contains(company8))
        assertFalse(otherCompanyNames.contains("Company 7"))
    }

    private fun createCompaniesForTestingOrdering(testString: String) = listOf(
        CompanyInformation(
            company9, "",
            listOf(
                CompanyIdentifier(
                    CompanyIdentifier.IdentifierType.isin,
                    "3$testString",
                ),
            ),
            "", listOf(),
        ),
        CompanyInformation(company8, "", listOf(), "", listOf("3$testString", "other_name")),
        CompanyInformation("3$testString", "", listOf(), "", listOf()),
        CompanyInformation(
            company6, "",
            listOf(
                CompanyIdentifier(
                    CompanyIdentifier.IdentifierType.isin,
                    "${testString}2",
                ),
            ),
            "", listOf(),
        ),
        CompanyInformation(company5, "", listOf(), "", listOf("${testString}2")),
        CompanyInformation("${testString}2", "", listOf(), "", listOf()),
        CompanyInformation(
            company3, "",
            listOf(
                CompanyIdentifier(
                    CompanyIdentifier.IdentifierType.isin,
                    testString,
                ),
            ),
            "", listOf(),
        ),
        CompanyInformation(company2, "", listOf(), "", listOf(testString)),
        CompanyInformation(testString, "", listOf(), "", listOf()),
    )
}
