package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MetaDataControllerTest {

    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val dummyDataCreator = DummyDataCreator()

    private fun createCompaniesAndEuTaxonomyDataSets(
        numberOfCompanies: Int,
        numberOfDataSetsPerCompany: Int
    ): List<String> {
        val testCompanyInformation = dummyDataCreator.createCompanyTestInformation("new_1")
        val testData = dummyDataCreator.createEuTaxonomyTestData(790240100)

        val listOfPostedTestCompanyIds = mutableListOf<String>()
        repeat(numberOfCompanies) {
            val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
            repeat(numberOfDataSetsPerCompany) {
                euTaxonomyDataControllerApi.postCompanyAssociatedData(
                    CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
                )
            }
            listOfPostedTestCompanyIds.add(testCompanyId)
        }
        return listOfPostedTestCompanyIds
    }

    @Test
    fun `post a dummy company and a dummy data set for it and check if meta info about that data can be retrieved`() {
        val testCompanyInformation = dummyDataCreator.createCompanyTestInformation("new_2")
        val testData = dummyDataCreator.createEuTaxonomyTestData(990714200)
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")

        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        ).dataId
        val dataMetaInformation = metaDataControllerApi.getDataMetaInfo(testDataId)
        assertEquals(
            DataMetaInformation(testDataId, testDataType, testCompanyId),
            dataMetaInformation,
            "The posted and the received eu taxonomy data sets and their company IDs are not equal."
        )
    }

    @Test
    fun `post several dummy companies and n dummy data sets for them and check filtering options`() {
        val numberOfCompanies = 5
        val numberOfDataSetsToPostPerCompany = 3
        val totalNumberOfDataSets = numberOfCompanies * numberOfDataSetsToPostPerCompany
        val initialSizeOfDataMetaInfoList = metaDataControllerApi.getListOfDataMetaInfo("", "").size
        val listOfTestCompanyIds =
            createCompaniesAndEuTaxonomyDataSets(numberOfCompanies, numberOfDataSetsToPostPerCompany)
        val listOfDataMetaInfoComplete = metaDataControllerApi.getListOfDataMetaInfo("", "")
        val listOfDataMetaInfoPerCompanyId =
            metaDataControllerApi.getListOfDataMetaInfo(listOfTestCompanyIds.first(), "")
        val listOfDataMetaInfoPerDataType = metaDataControllerApi.getListOfDataMetaInfo("", "EuTaxonomyData")
        val listOfDataMetaInfoPerCompanyIdAndDataType =
            metaDataControllerApi.getListOfDataMetaInfo(listOfTestCompanyIds.first(), "EuTaxonomyData")

        assertEquals(initialSizeOfDataMetaInfoList + totalNumberOfDataSets, listOfDataMetaInfoComplete.size)
        assertEquals(numberOfDataSetsToPostPerCompany, listOfDataMetaInfoPerCompanyId.size)
        assertEquals(initialSizeOfDataMetaInfoList + totalNumberOfDataSets, listOfDataMetaInfoPerDataType.size)
        assertEquals(numberOfDataSetsToPostPerCompany, listOfDataMetaInfoPerCompanyIdAndDataType.size)
    }
}
