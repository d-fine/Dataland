package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForNonFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.dataland.e2etests.accessmanagement.UnauthorizedMetaDataControllerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

class MetaDataControllerTest {
/*
    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val euTaxonomyDataForNonFinancialsControllerApi =
        EuTaxonomyDataForNonFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProvider = TestDataProvider()
    private val tokenHandler = TokenHandler()
    private val unauthorizedMetaDataControllerApi = UnauthorizedMetaDataControllerApi()

    private fun postCompaniesAndEuTaxonomyData(testData: Map<CompanyInformation, List<EuTaxonomyDataForNonFinancials>>):
        List<String> {
        val listOfPostedTestCompanyIds = mutableListOf<String>()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        for ((company, data) in testData) {
            val testCompanyId = companyDataControllerApi.postCompany(company).companyId
            data.forEach {
                euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedData(
                    CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, it)
                )
            }
            listOfPostedTestCompanyIds.add(testCompanyId)
        }
        return listOfPostedTestCompanyIds
    }

    @Test
    fun `post dummy company and taxonomy data for it and check if meta info about that data can be retrieved`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(EuTaxonomyDataForNonFinancials::class.java,1).first()
        val testData = testDataProvider.getEuTaxonomyDataForNonFinancials(1).first()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testData)
        ).dataId
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val dataMetaInformation = metaDataControllerApi.getDataMetaInfo(testDataId)
        assertEquals(
            DataMetaInformation(testDataId, testDataType, testCompanyId),
            dataMetaInformation,
            "The meta info of the posted eu taxonomy data does not match the retrieved meta info."
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with empty filters`() {
        val numberOfCompanies = 5
        val numberOfDataSetsToPostPerCompany = 3
        val totalNumberOfDataSets = numberOfCompanies * numberOfDataSetsToPostPerCompany
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val initialSizeOfDataMetaInfoComplete = metaDataControllerApi.getListOfDataMetaInfo("", "").size
        val testData = testDataProvider
            .getCompaniesWithEuTaxonomyDataForNonFinancials(EuTaxonomyDataForNonFinancials::class.java,numberOfCompanies, numberOfDataSetsToPostPerCompany)
        postCompaniesAndEuTaxonomyData(testData)
        val listOfDataMetaInfoComplete = metaDataControllerApi.getListOfDataMetaInfo("", "")
        val expectedSizeOfDataMetaInfoComplete = initialSizeOfDataMetaInfoComplete + totalNumberOfDataSets
        assertEquals(
            expectedSizeOfDataMetaInfoComplete, listOfDataMetaInfoComplete.size,
            "The list with all data meta info is expected to increase by $totalNumberOfDataSets to " +
                "$expectedSizeOfDataMetaInfoComplete, but has the size ${listOfDataMetaInfoComplete.size}."
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on company ID`() {
        val numberOfCompanies = 3
        val numberOfDataSetsToPostPerCompany = 4
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val testData = testDataProvider
            .getCompaniesWithEuTaxonomyDataForNonFinancials(EuTaxonomyDataForNonFinancials::class.java,numberOfCompanies, numberOfDataSetsToPostPerCompany)
        val listOfTestCompanyIds = postCompaniesAndEuTaxonomyData(testData)
        val listOfDataMetaInfoPerCompanyId =
            metaDataControllerApi.getListOfDataMetaInfo(listOfTestCompanyIds.first(), "")
        assertEquals(
            numberOfDataSetsToPostPerCompany, listOfDataMetaInfoPerCompanyId.size,
            "The first posted company is expected to have meta info about $numberOfDataSetsToPostPerCompany " +
                "data sets, but has meta info about ${listOfDataMetaInfoPerCompanyId.size} data sets."
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on data type`() {
        val numberOfCompanies = 4
        val numberOfDataSetsToPostPerCompany = 5
        val totalNumberOfDataSets = numberOfCompanies * numberOfDataSetsToPostPerCompany
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val initialSizeOfDataMetaInfoList = metaDataControllerApi.getListOfDataMetaInfo("", "").size
        val testData = testDataProvider
            .getCompaniesWithEuTaxonomyDataForNonFinancials(EuTaxonomyDataForNonFinancials::class.java, numberOfCompanies, numberOfDataSetsToPostPerCompany)
        postCompaniesAndEuTaxonomyData(testData)
        val listOfDataMetaInfoPerDataType = metaDataControllerApi.getListOfDataMetaInfo(
            "", "EuTaxonomyDataForNonFinancials"
        )
        val expectedSizeOfDataMetaInfoList = initialSizeOfDataMetaInfoList + totalNumberOfDataSets
        assertEquals(
            expectedSizeOfDataMetaInfoList, listOfDataMetaInfoPerDataType.size,
            "The list with all data meta info is expected to increase by $totalNumberOfDataSets to " +
                "$expectedSizeOfDataMetaInfoList, but has the size ${listOfDataMetaInfoPerDataType.size}."
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filters on company ID and data type`() {
        val numberOfCompanies = 2
        val numberOfDataSetsToPostPerCompany = 6
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val testData = testDataProvider
            .getCompaniesWithEuTaxonomyDataForNonFinancials(EuTaxonomyDataForNonFinancials::class.java,numberOfCompanies, numberOfDataSetsToPostPerCompany)
        val listOfTestCompanyIds = postCompaniesAndEuTaxonomyData(testData)
        val listOfDataMetaInfoPerCompanyIdAndDataType =
            metaDataControllerApi.getListOfDataMetaInfo(listOfTestCompanyIds.first(), "EuTaxonomyDataForNonFinancials")
        assertEquals(
            numberOfDataSetsToPostPerCompany, listOfDataMetaInfoPerCompanyIdAndDataType.size,
            "The first posted company is expected to have meta info about $numberOfDataSetsToPostPerCompany " +
                "data sets, but has meta info about ${listOfDataMetaInfoPerCompanyIdAndDataType.size} data sets."
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check if green asset ratio is in expected range`() {
        val numberOfCompanies = 2
        val numberOfDataSetsToPostPerCompany = 6
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        val testData = testDataProvider
            .getCompaniesWithEuTaxonomyDataForNonFinancials(EuTaxonomyDataForNonFinancials::class.java,numberOfCompanies, numberOfDataSetsToPostPerCompany)
        postCompaniesAndEuTaxonomyData(testData)
        val greenAssetRatio = metaDataControllerApi.getGreenAssetRatioForNonFinancials(null)
        assertTrue(greenAssetRatio.all { it.value.toDouble() in 0.0..1.0 })
    }

    @Test
    fun `post a dummy teaser company and data for it and confirm unauthorized meta info access succeeds`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(EuTaxonomyDataForNonFinancials::class.java,1).first()
        val testData = testDataProvider.getEuTaxonomyDataForNonFinancials(1).first()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testData)
        ).dataId
        companyDataControllerApi.setTeaserCompanies(listOf(testCompanyId))
        val dataMetaInformation = unauthorizedMetaDataControllerApi.getDataMetaInfo(testDataId)
        assertEquals(
            DataMetaInformation(testDataId, testDataType, testCompanyId),
            dataMetaInformation,
            "The meta info of the posted eu taxonomy data does not match the retrieved meta info."
        )
    }

    @Test
    fun `post a dummy company and taxonomy data for it and confirm unauthorized meta info access is denied`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(EuTaxonomyDataForNonFinancials::class.java,1).first()
        val testData = testDataProvider.getEuTaxonomyDataForNonFinancials(1).first()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testData)
        ).dataId
        val exception = assertThrows<IllegalArgumentException> {
            unauthorizedMetaDataControllerApi.getDataMetaInfo(testDataId)
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post a dummy company as teaser company and data for it and confirm unauthorized meta info search succeeds`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(EuTaxonomyDataForNonFinancials::class.java,1).first()
        val testData = testDataProvider.getEuTaxonomyDataForNonFinancials(1).first()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testData)
        ).dataId
        companyDataControllerApi.setTeaserCompanies(listOf(testCompanyId))

        assertTrue(
            unauthorizedMetaDataControllerApi.getListOfDataMetaInfo(testCompanyId, testDataType).contains(
                DataMetaInformation(dataId = testDataId, dataType = testDataType, companyId = testCompanyId)
            ),
            "The meta info of the posted eu taxonomy data that was associated with the teaser company does not" +
                "match the retrieved meta info."
        )
    }

    @Test
    fun `post a dummy company and taxonomy data for it and confirm unauthorized meta info search is denied`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(EuTaxonomyDataForNonFinancials::class.java,1).first()
        val testData = testDataProvider.getEuTaxonomyDataForNonFinancials(1).first()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testData)
        ).dataId
        val exception = assertThrows<IllegalArgumentException> {
            unauthorizedMetaDataControllerApi.getListOfDataMetaInfo(testCompanyId, testDataType)
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

 */
}
