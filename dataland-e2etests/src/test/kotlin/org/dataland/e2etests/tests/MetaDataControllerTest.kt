package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForNonFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForFinancials
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.FrameworkTestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.dataland.e2etests.accessmanagement.UnauthorizedMetaDataControllerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

class MetaDataControllerTest {

    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val euTaxonomyDataForNonFinancialsControllerApi =
        EuTaxonomyDataForNonFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val euTaxonomyDataForFinancialsControllerApi =
        EuTaxonomyDataForFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val tokenHandler = TokenHandler()
    private val unauthorizedMetaDataControllerApi = UnauthorizedMetaDataControllerApi()

    private val testDataProviderEuTaxonomyForNonFinancials =
        FrameworkTestDataProvider(EuTaxonomyDataForNonFinancials::class.java)
    private val testDataProviderEuTaxonomyForFinancials = FrameworkTestDataProvider(EuTaxonomyDataForFinancials::class.java)

    private val numberOfCompaniesToPostPerFramework = 4
    private val numberOfDataSetsToPostPerCompany = 5
    private val totalNumberOfDataSetsPerFramework =
        numberOfCompaniesToPostPerFramework * numberOfDataSetsToPostPerCompany

    private val testEuTaxonomyDataForNonFinancials =
        testDataProviderEuTaxonomyForNonFinancials.getTData(1).first()
    private val testCompaniesAndEuTaxonomyDataForNonFinancials = testDataProviderEuTaxonomyForNonFinancials
        .getCompaniesWithTDataAndNoIdentifiers(
            numberOfCompaniesToPostPerFramework,
            numberOfDataSetsToPostPerCompany
        )
    private val testCompaniesAndEuTaxonomyDataForFinancials = testDataProviderEuTaxonomyForFinancials
        .getCompaniesWithTDataAndNoIdentifiers(
            numberOfCompaniesToPostPerFramework,
            numberOfDataSetsToPostPerCompany
        )

    private fun postCompaniesAndEuTaxonomyDataForNonFinancials(
        testData: Map<CompanyInformation,
            List<EuTaxonomyDataForNonFinancials>>
    ):
        List<String> {
        val listOfPostedTestCompanyIds = mutableListOf<String>()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        for ((company, data) in testData) {
            val testCompanyId = companyDataControllerApi.postCompany(company).companyId
            data.forEach {
                euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                    CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, it)
                )
            }
            listOfPostedTestCompanyIds.add(testCompanyId)
        }
        return listOfPostedTestCompanyIds
    }

    private fun postCompaniesAndEuTaxonomyDataForFinancials(
        testData: Map<CompanyInformation,
            List<EuTaxonomyDataForFinancials>>
    ) {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        for ((company, data) in testData) {
            val testCompanyId = companyDataControllerApi.postCompany(company).companyId
            data.forEach {
                euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(
                    CompanyAssociatedDataEuTaxonomyDataForFinancials(testCompanyId, it)
                )
            }
        }
    }

    @Test
    fun `post dummy company and taxonomy data for it and check if meta info about that data can be retrieved`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation =
            testDataProviderEuTaxonomyForFinancials.getCompanyInformationWithoutIdentifiers(1).first()
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataForNonFinancialsControllerApi
            .postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testEuTaxonomyDataForNonFinancials)
            ).dataId
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val dataMetaInformation = metaDataControllerApi.getDataMetaInfo(testDataId)
        assertEquals(
            DataMetaInformation(testDataId, testDataType, testCompanyId),
            dataMetaInformation,
            "The meta info of the posted eu taxonomy data does not match the retrieved meta info."
        )
    }

    @Test
    fun `search for a company that does not exist and check that a 404 error is returned`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val clientException = assertThrows<ClientException> {
            companyDataControllerApi.getCompanyById("this-should-not-exist")
        }
        assertEquals(clientException.statusCode, 404)
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with empty filters`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val initialSizeOfDataMetaInfoComplete = metaDataControllerApi.getListOfDataMetaInfo().size
        postCompaniesAndEuTaxonomyDataForNonFinancials(testCompaniesAndEuTaxonomyDataForNonFinancials)
        val listOfDataMetaInfoComplete = metaDataControllerApi.getListOfDataMetaInfo()
        val expectedSizeOfDataMetaInfoComplete = initialSizeOfDataMetaInfoComplete + totalNumberOfDataSetsPerFramework
        assertEquals(
            expectedSizeOfDataMetaInfoComplete, listOfDataMetaInfoComplete.size,
            "The list with all data meta info is expected to increase by $totalNumberOfDataSetsPerFramework to " +
                "$expectedSizeOfDataMetaInfoComplete, but has the size ${listOfDataMetaInfoComplete.size}."
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on company ID`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val listOfTestCompanyIds = postCompaniesAndEuTaxonomyDataForNonFinancials(
            testCompaniesAndEuTaxonomyDataForNonFinancials
        )
        val listOfDataMetaInfoPerCompanyId =
            metaDataControllerApi.getListOfDataMetaInfo(listOfTestCompanyIds.first())
        assertEquals(
            numberOfDataSetsToPostPerCompany, listOfDataMetaInfoPerCompanyId.size,
            "The first posted company is expected to have meta info about $numberOfDataSetsToPostPerCompany " +
                "data sets, but has meta info about ${listOfDataMetaInfoPerCompanyId.size} data sets."
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on data type`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val initialSizeOfListOfDataMetaInfoForEuTaxonomyNonFinancials = metaDataControllerApi
            .getListOfDataMetaInfo(dataType = DataTypeEnum.eutaxonomyMinusFinancials).size
        postCompaniesAndEuTaxonomyDataForNonFinancials(testCompaniesAndEuTaxonomyDataForNonFinancials)
        postCompaniesAndEuTaxonomyDataForFinancials(testCompaniesAndEuTaxonomyDataForFinancials)
        val listOfDataMetaInfoForEuTaxonomyNonFinancials = metaDataControllerApi.getListOfDataMetaInfo(
            dataType = DataTypeEnum.eutaxonomyMinusFinancials
        )
        val expectedSizeOfDataMetaInfoList = initialSizeOfListOfDataMetaInfoForEuTaxonomyNonFinancials +
            totalNumberOfDataSetsPerFramework
        assertEquals(
            expectedSizeOfDataMetaInfoList, listOfDataMetaInfoForEuTaxonomyNonFinancials.size,
            "The meta info list for all EU Taxonomy Data for Financials is expected to increase by " +
                "$totalNumberOfDataSetsPerFramework to $expectedSizeOfDataMetaInfoList, but has the size " +
                "${listOfDataMetaInfoForEuTaxonomyNonFinancials.size}."
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filters on company ID and data type`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val listOfTestCompanyIds = postCompaniesAndEuTaxonomyDataForNonFinancials(
            testCompaniesAndEuTaxonomyDataForNonFinancials
        )
        val listOfDataMetaInfoPerCompanyIdAndDataType =
            metaDataControllerApi.getListOfDataMetaInfo(
                listOfTestCompanyIds.first(),
                DataTypeEnum.eutaxonomyMinusNonMinusFinancials
            )
        assertEquals(
            numberOfDataSetsToPostPerCompany, listOfDataMetaInfoPerCompanyIdAndDataType.size,
            "The first posted company is expected to have meta info about $numberOfDataSetsToPostPerCompany " +
                "data sets, but has meta info about ${listOfDataMetaInfoPerCompanyIdAndDataType.size} data sets."
        )
    }

    @Test
    fun `post a dummy teaser company and data for it and confirm unauthorized meta info access succeeds`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation =
            testDataProviderEuTaxonomyForFinancials.getCompanyInformationWithoutIdentifiers(1).first()
                .copy(isTeaserCompany = true)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataForNonFinancialsControllerApi
            .postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testEuTaxonomyDataForNonFinancials)
            ).dataId
        val dataMetaInformation = unauthorizedMetaDataControllerApi.getDataMetaInfo(testDataId)
        assertEquals(
            DataMetaInformation(testDataId, testDataType, testCompanyId),
            dataMetaInformation,
            "The meta info of the posted eu taxonomy data does not match the retrieved meta info."
        )
    }

    @Test
    fun `post a dummy company and taxonomy data for it and confirm unauthorized meta info access is denied`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation =
            testDataProviderEuTaxonomyForFinancials.getCompanyInformationWithoutIdentifiers(1).first()
                .copy(isTeaserCompany = false)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataForNonFinancialsControllerApi
            .postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testEuTaxonomyDataForNonFinancials)
            ).dataId
        val exception = assertThrows<IllegalArgumentException> {
            unauthorizedMetaDataControllerApi.getDataMetaInfo(testDataId)
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post a dummy company as teaser company and data for it and confirm unauthorized meta info search succeeds`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation =
            testDataProviderEuTaxonomyForFinancials.getCompanyInformationWithoutIdentifiers(1).first()
                .copy(isTeaserCompany = true)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataForNonFinancialsControllerApi
            .postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testEuTaxonomyDataForNonFinancials)
            ).dataId

        val expectedMetaInformation = DataMetaInformation(testDataId, testDataType, testCompanyId)
        assertTrue(
            unauthorizedMetaDataControllerApi.getListOfDataMetaInfo(testCompanyId, testDataType)
                .contains(expectedMetaInformation),
            "The meta info of the posted eu taxonomy data that was associated with the teaser company does not" +
                "match the retrieved meta info."
        )
    }

    @Test
    fun `post a dummy company and taxonomy data for it and confirm unauthorized meta info search is denied`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation =
            testDataProviderEuTaxonomyForFinancials.getCompanyInformationWithoutIdentifiers(1).first()
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testEuTaxonomyDataForNonFinancials)
        ).dataId
        val exception = assertThrows<IllegalArgumentException> {
            unauthorizedMetaDataControllerApi.getListOfDataMetaInfo(testCompanyId, testDataType)
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }
}
