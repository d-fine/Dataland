package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_PROXY
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.dataland.e2etests.accessmanagement.UnauthorizedCompanyDataControllerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CompanyDataControllerTest {

    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val testDataProvider = TestDataProvider()
    private val tokenHandler = TokenHandler()
    private val unauthorizedCompanyDataControllerApi = UnauthorizedCompanyDataControllerApi()

    private fun postOneCompanyAndData(): Map<String, String> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        val testData = testDataProvider.getEuTaxonomyData(1).first()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        ).dataId
        return mapOf("companyId" to testCompanyId, "dataId" to testDataId, "dataType" to testDataType)
    }

    @Test
    fun `post a dummy company and check if post was successful`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
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
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val receivedCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        assertEquals(
            StoredCompany(receivedCompanyId, testCompanyInformation, emptyList()),
            companyDataControllerApi.getCompanyById(receivedCompanyId),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyInformation)
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val getCompaniesByNameResponse = companyDataControllerApi.getCompanies(
            testCompanyInformation.companyName, null, true
        )
        assertTrue(
            getCompaniesByNameResponse.contains(
                StoredCompany(
                    postCompanyResponse.companyId,
                    testCompanyInformation,
                    dataRegisteredByDataland = emptyList()
                )
            ),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {
        val listOfTestCompanyInformation = testDataProvider.getCompanyInformation(3)
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val allCompaniesListSizeBefore = companyDataControllerApi.getCompanies("", null, true).size
        for (companyInformation in listOfTestCompanyInformation) {
            companyDataControllerApi.postCompany(companyInformation)
        }
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val allCompaniesListSizeAfter = companyDataControllerApi.getCompanies("", null, true).size
        assertEquals(
            listOfTestCompanyInformation.size, allCompaniesListSizeAfter - allCompaniesListSizeBefore,
            "The size of the all-companies-list did not increase by ${listOfTestCompanyInformation.size}."
        )
    }

    @Test
    fun `post a dummy company and a dummy data set for it and check if the company contains that data set ID`() {
        val testDataInformation = postOneCompanyAndData()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val listOfDataMetaInfoForTestCompany = metaDataControllerApi.getListOfDataMetaInfo(
            testDataInformation["companyId"],
            testDataInformation["dataType"]
        )
        assertTrue(
            listOfDataMetaInfoForTestCompany.contains(
                DataMetaInformation(
                    testDataInformation["dataId"]!!,
                    testDataInformation["dataType"]!!,
                    testDataInformation["companyId"]!!
                )
            ),
            "The all-data-sets-list of the posted company does not contain the posted data set."
        )
    }

    @Test
    fun `post a dummy company and check if it can be searched for by identifier`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        assertTrue(
            companyDataControllerApi.getCompanies(
                searchString = testCompanyInformation.identifiers.first().identifierValue,
                selectedIndex = null,
                onlyCompanyNames = false
            ).any { it.companyId == testCompanyId }
        )
    }

    @Test
    fun `post the teaser dummy company and test if it can be retrieved by its company ID as unauthorized user`() {
        val teaserCompanyInformation = testDataProvider.getTeaserDummyCompany()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val teaserCompanyId = companyDataControllerApi.postCompany(teaserCompanyInformation).companyId
        val getCompanyByIdResponse = unauthorizedCompanyDataControllerApi.getCompanyById(teaserCompanyId)
        val expectedStoredTeaserCompany = StoredCompany(
            companyId = teaserCompanyId,
            companyInformation = teaserCompanyInformation,
            dataRegisteredByDataland = emptyList()
        )
        assertEquals(
            expectedStoredTeaserCompany, getCompanyByIdResponse,
            "The posted company does not equal the teaser company."
        )
    }

    @Test
    fun `post a regular dummy company and test if it cannot be retrieved by its company ID as unauthorized user`() {
        val nonTeaserCompanyInformation = testDataProvider.getNonTeaserDummyCompany()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val nonTeaserCompanyId = companyDataControllerApi.postCompany(nonTeaserCompanyInformation).companyId
        val exception = assertThrows<IllegalArgumentException> {
            unauthorizedCompanyDataControllerApi.getCompanyById(nonTeaserCompanyId)
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post a dummy company as a user type which does not have the rights to do so and receive an error code 403`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val exception =
            assertThrows<ClientException> { companyDataControllerApi.postCompany(testCompanyInformation).companyId }
        assertEquals("Client error : 403 ", exception.message)
    }
}
