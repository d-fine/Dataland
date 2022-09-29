package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.dataland.e2etests.accessmanagement.UnauthorizedCompanyDataControllerApi
import org.dataland.e2etests.utils.copyNormalised
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CompanyDataControllerTest {

    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val tokenHandler = TokenHandler()
    private val unauthorizedCompanyDataControllerApi = UnauthorizedCompanyDataControllerApi()

    private val testDataProviderForEuTaxonomyDataForNonFinancials =
        TestDataProvider(EuTaxonomyDataForNonFinancials::class.java)

    @Test
    fun `post a dummy company and check if post was successful`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
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
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val receivedCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        assertEquals(
            StoredCompany(receivedCompanyId, testCompanyInformation, emptyList()).copyNormalised(),
            companyDataControllerApi.getCompanyById(receivedCompanyId).copyNormalised(),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyInformation)
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val getCompaniesByNameResponse = companyDataControllerApi.getCompanies(
            searchString = testCompanyInformation.companyName,
            onlyCompanyNames = true,
        ).map { it.copyNormalised() }
        val expectedCompany = StoredCompany(postCompanyResponse.companyId, testCompanyInformation, emptyList())
            .copyNormalised()
        assertTrue(
            getCompaniesByNameResponse.contains(expectedCompany),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {
        val listOfTestCompanyInformation =
            testDataProviderForEuTaxonomyDataForNonFinancials.getCompanyInformationWithoutIdentifiers(3)
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val allCompaniesListSizeBefore = companyDataControllerApi.getCompanies().size
        for (companyInformation in listOfTestCompanyInformation) {
            companyDataControllerApi.postCompany(companyInformation)
        }
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
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
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        assertTrue(
            companyDataControllerApi.getCompanies(
                searchString = testCompanyInformation.identifiers.first().identifierValue,
                onlyCompanyNames = false
            ).any { it.companyId == testCompanyId }
        )
    }

    @Test
    fun `post a dummy company as teaser company and test if it is retrievable by company ID as unauthorized user`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
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
            expectedStoredTeaserCompany.copyNormalised(), getCompanyByIdResponse.copyNormalised(),
            "The posted company does not equal the teaser company."
        )
    }

    @Test
    fun `post a dummy company and test if it cannot be retrieved by its company ID as unauthorized user`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
            .copy(isTeaserCompany = false)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val exception = assertThrows<IllegalArgumentException> {
            unauthorizedCompanyDataControllerApi.getCompanyById(testCompanyId)
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post a dummy company as a user type which does not have the rights to do so and receive an error code 403`() {
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val exception =
            assertThrows<ClientException> { companyDataControllerApi.postCompany(testCompanyInformation).companyId }
        assertEquals("Client error : 403 ", exception.message)
    }
}
