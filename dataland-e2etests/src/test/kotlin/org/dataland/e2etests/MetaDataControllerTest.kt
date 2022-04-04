package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.AllDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.PostCompanyRequestBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MetaDataControllerTest {

    private val basePathToBackendViaProxy = "http://proxy:80/api"
    private val metaDataControllerApi = AllDataControllerApi(basePathToBackendViaProxy)
    private val companyDataControllerApi = CompanyDataControllerApi(basePathToBackendViaProxy)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePathToBackendViaProxy)

    private fun populateDatalandWithCompaniesAndEuTaxnomyDataSets(
        numberOfCompanies: Int,
        numberOfDataSetsPerCompany: Int
    ): List<String> {
        val testCompanyName = "Fantasy-Company_100"
        val testData = DummyDataCreator().createEuTaxonomyTestDataSet()

        val listOfPostedTestCompanyIds = mutableListOf<String>()
        repeat(numberOfCompanies) {
            val testCompanyId = companyDataControllerApi.postCompany(PostCompanyRequestBody(testCompanyName)).companyId
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
        val testCompanyName = "Non-Existent-Company_1"
        val testData = DummyDataCreator().createEuTaxonomyTestDataSet()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")

        val testCompanyId = companyDataControllerApi.postCompany(PostCompanyRequestBody(testCompanyName)).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        ).dataId

        val listOfDataMetaInfo =
            metaDataControllerApi.getDataMetaInfo(testDataId)

        assertEquals(
            listOf(DataMetaInformation(testDataId, testDataType, testCompanyId)),
            listOfDataMetaInfo,
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
            populateDatalandWithCompaniesAndEuTaxnomyDataSets(numberOfCompanies, numberOfDataSetsToPostPerCompany)
        val listOfDataMetaInfoComplete = metaDataControllerApi.getListOfDataMetaInfo("", "")
        val listOfDataMetaInfoPerCompanyId =
            metaDataControllerApi.getListOfDataMetaInfo(listOfTestCompanyIds.first(), "")
        val listOfDataMetaInfoPerDataType = metaDataControllerApi.getListOfDataMetaInfo("", "EuTaxonomyData")
        val listOfDataMetaInfoPerCompanyIdAndDataType =
            metaDataControllerApi.getListOfDataMetaInfo(listOfTestCompanyIds.first(), "EuTaxonomyData")

        assertEquals(initialSizeOfDataMetaInfoList + totalNumberOfDataSets, listOfDataMetaInfoComplete.size, "a.")
        assertEquals(numberOfDataSetsToPostPerCompany, listOfDataMetaInfoPerCompanyId.size, "a")
        assertEquals(initialSizeOfDataMetaInfoList + totalNumberOfDataSets, listOfDataMetaInfoPerDataType.size, "a")
        assertEquals(numberOfDataSetsToPostPerCompany, listOfDataMetaInfoPerCompanyIdAndDataType.size, "b")
    }
}
