package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CompanyDataControllerTest {

    private val apiAccessor = ApiAccessor()

    @Test
    fun `post a dummy company and check if post was successful`() {
        val companyUpload = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        assertEquals(
            companyUpload.inputCompanyInformation, companyUpload.actualStoredCompany.companyInformation,
            "The company information in the post-response does not match " +
                "the actual information of the company to be posted."
        )
        assertTrue(
            companyUpload.actualStoredCompany.companyId.isNotEmpty(),
            "No valid company Id was assigned to the posted company."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its company Id`() {
        val companyUpload = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        apiAccessor.tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val expectedStoredCompany = StoredCompany(
            companyUpload.actualStoredCompany.companyId,
            companyUpload.inputCompanyInformation, emptyList()
        )
        assertEquals(
            expectedStoredCompany,
            apiAccessor.companyDataControllerApi.getCompanyById(companyUpload.actualStoredCompany.companyId),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val companyUpload = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val getCompaniesOnlyByNameResponse = apiAccessor.getCompaniesOnlyByName(
            companyUpload.actualStoredCompany.companyInformation.companyName
        )
        val expectedCompany = StoredCompany(
            companyUpload.actualStoredCompany.companyId,
            companyUpload.actualStoredCompany.companyInformation, emptyList()
        )
        assertTrue(
            getCompaniesOnlyByNameResponse.contains(expectedCompany),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    @Suppress("kotlin:S138")
    fun `post two dummy companies and check if the distinct endpoint returns all values`() {
        val numCompanies = 2
        val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(numCompanies)
        val testData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(numCompanies)
        apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            testCompanyInformation, testData,
            apiAccessor.euTaxonomyNonFinancialsUploaderFunction
        )
        val distinctValues = apiAccessor.companyDataControllerApi.getAvailableCompanySearchFilters()
        assertTrue(
            distinctValues.sectors!!.containsAll(testCompanyInformation.map { it.sector }),
            "The list of all occurring sectors does not contain the sectors of the posted companies."
        )
        assertTrue(
            distinctValues.countryCodes!!.containsAll(testCompanyInformation.map { it.countryCode }),
            "The list of all occurring country codes does not contain the country codes of the posted companies."
        )
    }

    @Test
    fun `post dummy companies with frontendExcluded framework data and check if the distinct endpoint ignores`() {
        val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .generateOneCompanyInformationPerBackendOnlyFramework()
        apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOf(testCompanyInformation[DataTypeEnum.lksg]!!), apiAccessor.testDataProviderForLksgData.getTData(1),
            apiAccessor.lksgUploaderFunction
        )
        apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOf(testCompanyInformation[DataTypeEnum.sfdr]!!), apiAccessor.testDataProviderForSfdrData.getTData(1),
            apiAccessor.sfdrUploaderFunction
        )
        apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOf(testCompanyInformation[DataTypeEnum.sme]!!), apiAccessor.testDataProviderForSmeData.getTData(1),
            apiAccessor.smeUploaderFunction
        )
        /* TODO we should assure somehow, that the uploads above cover all backend-only-frameworks.
        I had some ideas on how we could "enforce" that, but thos ideas would lead to this method having more than 20
        lines of code and therefore detekt failing.
        Maybe we should think about building an iterator that iterates through datatypes you give to it and does
        things with the matching testDataProvicer and controller.
        */
        val distinctValues = apiAccessor.companyDataControllerApi.getAvailableCompanySearchFilters()
        assertTrue(
            distinctValues.sectors!!.intersect(testCompanyInformation.map { it.value.sector }.toSet()).isEmpty(),
            "At least one sector of the frontend-excluded data sets appears in the distinct sector value list."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its country code and sector`() {
        val companyUpload = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val getCompaniesByCountryCodeAndSectorResponse = apiAccessor.companyDataControllerApi.getCompanies(
            sectors = setOf(companyUpload.actualStoredCompany.companyInformation.sector),
            countryCodes = setOf(companyUpload.actualStoredCompany.companyInformation.countryCode)
        )
        assertTrue(
            getCompaniesByCountryCodeAndSectorResponse.contains(companyUpload.actualStoredCompany),
            "The posted company could not be found in the query results when querying for its country code and sector."
        )
    }

    @Test
    fun `post a dummy company and check that it is not returned if filtered by a different sector`() {
        val companyUpload = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val getCompaniesByCountryCodeAndSectorResponse = apiAccessor.companyDataControllerApi.getCompanies(
            sectors = setOf("${companyUpload.actualStoredCompany.companyInformation.sector}a"),
            countryCodes = setOf(companyUpload.actualStoredCompany.companyInformation.countryCode)
        )
        assertFalse(
            getCompaniesByCountryCodeAndSectorResponse.contains(companyUpload.actualStoredCompany),
            "The posted company is in the query results," +
                " even though the country code filter was set to a different country code."

        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {
        apiAccessor.tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val allCompaniesListSizeBefore = apiAccessor.companyDataControllerApi.getCompanies().size
        val companyUploads = apiAccessor.uploadNCompaniesWithoutIdentifiers(3)
        apiAccessor.tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val allCompaniesListSizeAfter = apiAccessor.companyDataControllerApi.getCompanies().size
        assertEquals(
            companyUploads.size, allCompaniesListSizeAfter - allCompaniesListSizeBefore,
            "The size of the all-companies-list did not increase by ${companyUploads.size}."
        )
    }

    @Test
    fun `post a dummy company and check if it can be searched for by identifier`() {
        val companyUpload = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        apiAccessor.tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        assertTrue(
            apiAccessor.companyDataControllerApi.getCompanies(
                searchString = companyUpload.inputCompanyInformation.identifiers.first().identifierValue,
                onlyCompanyNames = false
            ).any { it.companyId == companyUpload.actualStoredCompany.companyId },
            "The posted company could not be found in the query results when querying for its first identifiers value."
        )
    }

    @Test
    fun `post a dummy company as teaser company and test if it is retrievable by company ID as unauthorized user`() {
        val companyUpload = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(true)
        val getCompanyByIdResponse = apiAccessor.unauthorizedCompanyDataControllerApi.getCompanyById(
            companyUpload.actualStoredCompany.companyId
        )
        val expectedStoredTeaserCompany = StoredCompany(
            companyId = companyUpload.actualStoredCompany.companyId,
            companyInformation = companyUpload.inputCompanyInformation,
            dataRegisteredByDataland = emptyList()
        )
        assertEquals(
            expectedStoredTeaserCompany, getCompanyByIdResponse,
            "The posted company does not equal the teaser company."
        )
    }

    @Test
    fun `post a dummy company and test if it cannot be retrieved by its company ID as unauthorized user`() {
        val companyUpload = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val exception = assertThrows<IllegalArgumentException> {
            apiAccessor.unauthorizedCompanyDataControllerApi.getCompanyById(
                companyUpload.actualStoredCompany.companyId
            )
        }
        assertTrue(
            exception.message!!.contains("Unauthorized access failed"),
            "The exception message does not say that an unauthorized access was the cause."
        )
    }

    @Test
    fun `post a dummy company as a user type which does not have the rights to do so and receive an error code 403`() {
        val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        apiAccessor.tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val exception =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi.postCompany(testCompanyInformation)
                    .companyId
            }
        assertEquals(
            "Client error : 403 ", exception.message,
            "The exception message does not say that a 403 client error was the cause."
        )
    }
}
