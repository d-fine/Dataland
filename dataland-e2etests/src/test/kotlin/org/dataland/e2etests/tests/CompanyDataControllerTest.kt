package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.UserType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class CompanyDataControllerTest {

    private val apiAccessor = ApiAccessor()

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
        apiAccessor.tokenHandler.obtainTokenForUserType(UserType.Reader)
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
        val getCompaniesOnlyByNameResponse = apiAccessor.getCompaniesOnlyByName(
            uploadInfo.actualStoredCompany.companyInformation.companyName,
        )
        val expectedCompany = StoredCompany(
            uploadInfo.actualStoredCompany.companyId,
            uploadInfo.actualStoredCompany.companyInformation,
            emptyList(),
        )
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
            distinctValues.sectors!!.containsAll(listOfTestCompanyInformation.map { it.sector }),
            "The list of all occurring sectors does not contain the sectors of the posted companies.",
        )
        assertTrue(
            distinctValues.countryCodes!!.containsAll(listOfTestCompanyInformation.map { it.countryCode }),
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
            distinctValues.sectors!!.intersect(
                mapOfAllBackendOnlyDataTypesToListOfOneCompanyInformation.map { it.value[0].sector }.toSet(),
            ).isEmpty(),
            "At least one sector of the frontend-excluded data sets appears in the distinct sector value list.",
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its country code and sector`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val getCompaniesByCountryCodeAndSectorResponse = apiAccessor.companyDataControllerApi.getCompanies(
            sectors = setOf(uploadInfo.actualStoredCompany.companyInformation.sector),
            countryCodes = setOf(uploadInfo.actualStoredCompany.companyInformation.countryCode),
        )
        assertTrue(
            getCompaniesByCountryCodeAndSectorResponse.contains(uploadInfo.actualStoredCompany),
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
        val allCompaniesListSizeAfter = apiAccessor.getNumberOfStoredCompanies()
        assertEquals(
            listOfUploadInfo.size,
            allCompaniesListSizeAfter - allCompaniesListSizeBefore,
            "The size of the all-companies-list did not increase by ${listOfUploadInfo.size}.",
        )
    }

    @Test
    fun `post a dummy company and check if it can be searched for by identifier`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        apiAccessor.tokenHandler.obtainTokenForUserType(UserType.Reader)
        assertTrue(
            apiAccessor.companyDataControllerApi.getCompanies(
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
        apiAccessor.tokenHandler.obtainTokenForUserType(UserType.Reader)
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
    fun `post a dummy company twice and receive the expected error code`() {
        val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        val randomIsin = CompanyIdentifier(
            identifierValue = UUID.randomUUID().toString(),
            identifierType = CompanyIdentifier.IdentifierType.isin,
        )
        val randomizedCompanyInformation = testCompanyInformation.copy(identifiers = listOf(randomIsin))
        apiAccessor.tokenHandler.obtainTokenForUserType(UserType.Uploader)
        apiAccessor.companyDataControllerApi.postCompany(randomizedCompanyInformation)
        val exception =
            assertThrows<Exception> {
                apiAccessor.companyDataControllerApi.postCompany(randomizedCompanyInformation)
            }
        assertEquals(
            "Client error : 400 ",
            exception.message,
            "The exception message does not say that a 400 client error was the cause.",
        )
    }
}
