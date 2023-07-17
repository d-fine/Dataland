package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class CompanyDataControllerGetCompaniesEndpointTest {

    private val apiAccessor = ApiAccessor()
    private val company1 = "Company 1"
    private val company2 = "Company 2"
    private val company3 = "Company 3"
    private val company5 = "Company 5"
    private val company8 = "Company 8"
    private val company9 = "Company 9"

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val expectedDataset = uploadTestEuTaxonomyFinancialsDataSet(uploadInfo.actualStoredCompany.companyId)
            .copy(uploaderUserId = null)
        val getCompaniesOnlyByNameResponse = apiAccessor.getCompaniesOnlyByName(
            uploadInfo.actualStoredCompany.companyInformation.companyName,
        )
        val expectedCompany = StoredCompany(
            uploadInfo.actualStoredCompany.companyId,
            uploadInfo.actualStoredCompany.companyInformation,
            listOf(expectedDataset),
        )
        assertTrue(
            getCompaniesOnlyByNameResponse.contains(expectedCompany),
            "Dataland does not contain the posted company.",
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its country code and sector`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val expectedDataMetaInfo = uploadTestEuTaxonomyFinancialsDataSet(uploadInfo.actualStoredCompany.companyId)
        val expectedStoredCompany = uploadInfo.actualStoredCompany
            .copy(dataRegisteredByDataland = listOf(expectedDataMetaInfo))
        val getCompaniesByCountryCodeAndSectorResponse = apiAccessor.companyDataControllerApi.getCompanies(
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
            uploadTestEuTaxonomyFinancialsDataSet(it.actualStoredCompany.companyId)
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
        val firstIdentifier = uploadInfo.inputCompanyInformation.identifiers.values.first { it.isNotEmpty() }.first()
        assertTrue(
            apiAccessor.companyDataControllerApi.getCompanies(
                searchString = firstIdentifier,
                onlyCompanyNames = false,
            ).isEmpty(),
            "The posted company was found in the query results.",
        )
        apiAccessor.companyDataControllerApi.existsIdentifier(
            IdentifierType.permId,
            firstIdentifier,
        )
        uploadTestEuTaxonomyFinancialsDataSet(uploadInfo.actualStoredCompany.companyId)
        assertTrue(
            apiAccessor.companyDataControllerApi.getCompanies(
                searchString = firstIdentifier,
                onlyCompanyNames = false,
            ).any { it.companyId == uploadInfo.actualStoredCompany.companyId },
            "The posted company could not be found in the query results when querying for its first identifiers value.",
        )
    }

    private fun testThatSearchForCompanyIdentifierWorks(identifierType: String, identifierValue: String) {
        val searchResponse = apiAccessor.companyDataControllerApi.getCompanies(
            searchString = identifierValue,
            onlyCompanyNames = false,

        )
            .toMutableList()
        assertTrue(
            searchResponse.all
                {
                        results ->
                    results.companyInformation.identifiers[identifierType]
                        ?.any { it == identifierValue } ?: false
                },
            "The search by identifier returns at least one company that does not contain the looked" +
                "for value $identifierType.",
        )
    }

    @Test
    fun `search for all identifier values and check if all results contain the looked for value`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val testString = UUID.randomUUID().toString()
        val testCompanyList = listOf(
            CompanyInformation(
                company1, "",
                identifiers = mapOf(
                    IdentifierType.isin.value to listOf("Isin$testString"),
                    IdentifierType.lei.value to listOf("Lei$testString"),
                ),
                "",
                listOf(company1 + testString),
            ),
        )
        val companyResponse = apiAccessor.companyDataControllerApi.postCompany(testCompanyList.first())
        uploadTestEuTaxonomyFinancialsDataSet(companyResponse.companyId)
        testThatSearchForCompanyIdentifierWorks(IdentifierType.isin.value, "Isin$testString")
        testThatSearchForCompanyIdentifierWorks(IdentifierType.lei.value, "Lei$testString")
    }

    @Test
    fun `upload a company with a dataset and check if it can be found via an empty name search`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val companyInformation = CompanyInformation(
            "retrieve empty search string", "",
            mapOf(
                IdentifierType.lei.value to listOf(UUID.randomUUID().toString()),
            ),
            "",
            listOf(),
        )
        val uploadedCompany = apiAccessor.companyDataControllerApi.postCompany(companyInformation)
        val uploadedData = uploadTestEuTaxonomyFinancialsDataSet(uploadedCompany.companyId).copy(uploaderUserId = null)
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
        val companyIdentifier = mapOf(
            IdentifierType.lei.value to listOf(testIdentifier),
        )
        val companyInformation = CompanyInformation(
            testName, "", companyIdentifier, "", listOf(),
        )
        val uploadedCompany = apiAccessor.companyDataControllerApi.postCompany(companyInformation)
        val uploadedData = uploadTestEuTaxonomyFinancialsDataSet(uploadedCompany.companyId).copy(uploaderUserId = null)
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
        for (company in companyList) {
            val uploadedCompany = apiAccessor.companyDataControllerApi.postCompany(company)
            uploadTestEuTaxonomyFinancialsDataSet(uploadedCompany.companyId)
        }
        val sortedCompanyNames = apiAccessor.companyDataControllerApi.getCompanies(
            searchString = testString,
        ).map { it.companyInformation.companyName }
        assertEquals(
            listOf(testString, company2, "${testString}2", company5, "3$testString", company9),
            sortedCompanyNames.filter { it != company8 && it != company3 },
        )
        assertEquals(
            listOf(company2, "${testString}2", company5, company3, company8, company9),
            sortedCompanyNames.filter { it != "3$testString" && it != testString },
        )

        val otherCompanyNames = apiAccessor.companyDataControllerApi.getCompanies(
            searchString = "other_name",
        ).map { it.companyInformation.companyName }
        assertTrue(otherCompanyNames.contains(company8))
        assertFalse(otherCompanyNames.contains("Company 7"))
    }

    private fun createCompaniesForTestingOrdering(inputString: String): List<CompanyInformation> {
        return listOf(
            CompanyInformation(
                company9, "",
                mapOf(
                    IdentifierType.isin.value to listOf("3$inputString"),
                ),
                "", listOf(),
            ),
            CompanyInformation(company8, "", mapOf(), "", listOf("3$inputString", "other_name")),
            CompanyInformation("3$inputString", "", mapOf(), "", listOf()),
            CompanyInformation(company5, "", mapOf(), "", listOf("${inputString}2")),
            CompanyInformation("${inputString}2", "", mapOf(), "", listOf()),
            CompanyInformation(
                company3, "",
                mapOf(
                    IdentifierType.isin.value to listOf(inputString),
                ),
                "", listOf(),
            ),
            CompanyInformation(company2, "", mapOf(), "", listOf(inputString)),
            CompanyInformation(inputString, "", mapOf(), "", listOf()),
        )
    }
    private fun uploadTestEuTaxonomyFinancialsDataSet(companyId: String): DataMetaInformation {
        return apiAccessor.uploadSingleFrameworkDataSet(
            companyId = companyId,
            frameworkData = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)[0],
            reportingPeriod = "2023",
            frameworkDataUploadFunction = apiAccessor::euTaxonomyFinancialsUploaderFunction,
        ).copy(qaStatus = QaStatus.accepted, currentlyActive = true)
    }
}
