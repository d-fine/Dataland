package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForNonFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.LksgDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataLksgData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.FRONTEND_DISPLAYED_FRAMEWORKS
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.dataland.e2etests.accessmanagement.UnauthorizedCompanyDataControllerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CompanyDataControllerTest {

    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val tokenHandler = TokenHandler()
    private val unauthorizedCompanyDataControllerApi = UnauthorizedCompanyDataControllerApi()

    private val dataControllerApiForNonFinancials =
        EuTaxonomyDataForNonFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProviderForEuTaxonomyDataForNonFinancials =
        TestDataProvider(EuTaxonomyDataForNonFinancials::class.java)

    private val dataControllerApiForLksgData =
        LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProviderForLksgData =
        TestDataProvider(LksgData::class.java)

    private val dataControllerApiForSfdrData =
        SfdrDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProviderForSfdrData =
        TestDataProvider(SfdrData::class.java)

    private fun postFirstCompanyWithoutIdentifiers(): StoredCompany {
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        return companyDataControllerApi.postCompany(testCompanyInformation)
    }

    @Test
    fun `post a dummy company and check if post was successful`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyInformation)
        assertEquals(
            testCompanyInformation, postCompanyResponse.companyInformation,
            "The company information in the post-response does not match " +
                "the actual information of the company to be posted."
        )
        assertTrue(
            postCompanyResponse.companyId.isNotEmpty(),
            "No valid company Id was assigned to the posted company."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its company Id`() {
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val receivedCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        assertEquals(
            StoredCompany(receivedCompanyId, testCompanyInformation, emptyList()),
            companyDataControllerApi.getCompanyById(receivedCompanyId),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val storedCompany = postFirstCompanyWithoutIdentifiers()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val getCompaniesByNameResponse = companyDataControllerApi.getCompanies(
            searchString = storedCompany.companyInformation.companyName,
            onlyCompanyNames = true,
        )
        val expectedCompany = StoredCompany(storedCompany.companyId, storedCompany.companyInformation, emptyList())
        assertTrue(
            getCompaniesByNameResponse.contains(expectedCompany),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    @Suppress("kotlin:S138")
    fun `post two dummy companies and check if the distinct endpoint returns all values`() {
        val numCompanies = 2
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(numCompanies)
        val testData = testDataProviderForEuTaxonomyDataForNonFinancials.getTData(numCompanies)
        testCompanyInformation.forEachIndexed { index, element ->
            val receivedCompanyId = companyDataControllerApi.postCompany(element).companyId
            dataControllerApiForNonFinancials.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                CompanyAssociatedDataEuTaxonomyDataForNonFinancials(receivedCompanyId, testData[index])
            )
        }
        val distinctValues = companyDataControllerApi.getAvailableCompanySearchFilters()
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
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation = listOf(
            testDataProviderForLksgData.generateCustomCompanyInformation("CompanyForLksg", "SectorShouldBeHidden1928"),
            testDataProviderForSfdrData.generateCustomCompanyInformation("CompanyForSfdr", "SectorShouldBeHidden2891")
        )
        val testLksgData = testDataProviderForLksgData.getTData(1)[0]
        val receivedCompanyIdForLksgCompany = companyDataControllerApi.postCompany(testCompanyInformation[0]).companyId
        dataControllerApiForLksgData.postCompanyAssociatedLksgData(
            CompanyAssociatedDataLksgData(receivedCompanyIdForLksgCompany, testLksgData)
        )
        val testSfdrData = testDataProviderForSfdrData.getTData(1)[0]
        val receivedCompanyIdForSfdrCompany = companyDataControllerApi.postCompany(testCompanyInformation[1]).companyId
        dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
            CompanyAssociatedDataSfdrData(receivedCompanyIdForSfdrCompany, testSfdrData)
        )
        val distinctValues = companyDataControllerApi.getAvailableCompanySearchFilters()
        assertEquals(
            DataTypeEnum.values().size - FRONTEND_DISPLAYED_FRAMEWORKS.size, testCompanyInformation.size,
            "Not all excluded frameworks are covered by this test. Please extend it."
        )
        assertTrue(
            distinctValues.sectors!!.intersect(testCompanyInformation.map { it.sector }.toSet()).isEmpty(),
            "At least one sector of the frontend-excluded data sets appears in the distinct sector value list."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its country code and sector`() {
        val storedCompany = postFirstCompanyWithoutIdentifiers()
        val getCompaniesByCountryCodeAndSectorResponse = companyDataControllerApi.getCompanies(
            sectors = setOf(storedCompany.companyInformation.sector),
            countryCodes = setOf(storedCompany.companyInformation.countryCode)
        )
        assertTrue(
            getCompaniesByCountryCodeAndSectorResponse.contains(storedCompany),
            "The posted company could not be found in the query results when querying for its country code and sector."
        )
    }

    @Test
    fun `post a dummy company and check that it is not returned if filtered by a different sector`() {
        val storedCompany = postFirstCompanyWithoutIdentifiers()
        val getCompaniesByCountryCodeAndSectorResponse = companyDataControllerApi.getCompanies(
            sectors = setOf("${storedCompany.companyInformation.sector}a"),
            countryCodes = setOf(storedCompany.companyInformation.countryCode)
        )
        assertTrue(
            !getCompaniesByCountryCodeAndSectorResponse.contains(storedCompany),
            "The posted company is in the query results," +
                " even though the country code filter was set to a different country code."

        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {
        val listOfTestCompanyInformation =
            testDataProviderForEuTaxonomyDataForNonFinancials.getCompanyInformationWithoutIdentifiers(3)
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val allCompaniesListSizeBefore = companyDataControllerApi.getCompanies().size
        for (companyInformation in listOfTestCompanyInformation) {
            companyDataControllerApi.postCompany(companyInformation)
        }
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val allCompaniesListSizeAfter = companyDataControllerApi.getCompanies().size
        assertEquals(
            listOfTestCompanyInformation.size, allCompaniesListSizeAfter - allCompaniesListSizeBefore,
            "The size of the all-companies-list did not increase by ${listOfTestCompanyInformation.size}."
        )
    }

    @Test
    fun `post a dummy company and check if it can be searched for by identifier`() {
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformation(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        assertTrue(
            companyDataControllerApi.getCompanies(
                searchString = testCompanyInformation.identifiers.first().identifierValue,
                onlyCompanyNames = false
            ).any { it.companyId == testCompanyId },
            "The posted company could not be found in the query results when querying for its first identifiers value."
        )
    }

    @Test
    fun `post a dummy company as teaser company and test if it is retrievable by company ID as unauthorized user`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
            .copy(isTeaserCompany = true)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val getCompanyByIdResponse = unauthorizedCompanyDataControllerApi.getCompanyById(testCompanyId)
        val expectedStoredTeaserCompany = StoredCompany(
            companyId = testCompanyId,
            companyInformation = testCompanyInformation,
            dataRegisteredByDataland = emptyList()
        )
        assertEquals(
            expectedStoredTeaserCompany, getCompanyByIdResponse,
            "The posted company does not equal the teaser company."
        )
    }

    @Test
    fun `post a dummy company and test if it cannot be retrieved by its company ID as unauthorized user`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
            .copy(isTeaserCompany = false)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val exception = assertThrows<IllegalArgumentException> {
            unauthorizedCompanyDataControllerApi.getCompanyById(testCompanyId)
        }
        assertTrue(
            exception.message!!.contains("Unauthorized access failed"),
            "The exception message does not say that an unauthorized access was the cause."
        )
    }

    @Test
    fun `post a dummy company as a user type which does not have the rights to do so and receive an error code 403`() {
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val exception =
            assertThrows<ClientException> { companyDataControllerApi.postCompany(testCompanyInformation).companyId }
        assertEquals(
            "Client error : 403 ", exception.message,
            "The exception message does not say that a 403 client error was the cause."
        )
    }
}
