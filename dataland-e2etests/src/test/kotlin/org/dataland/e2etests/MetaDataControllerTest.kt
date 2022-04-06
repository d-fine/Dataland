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

    private fun createCompaniesAndEuTaxonomyData(numberOfCompanies: Int, numberOfDataSetsPerCompany: Int)
            : List<String> {
        val listOfPostedTestCompanyIds = mutableListOf<String>()
        var counterToMarkCompanies = 1000
        var counterToMarkData = 50000000
        repeat(numberOfCompanies) {
            val testCompanyId = companyDataControllerApi.postCompany(
                dummyDataCreator.createCompanyTestInformation(counterToMarkCompanies.toString())
            ).companyId
            repeat(numberOfDataSetsPerCompany) {
                euTaxonomyDataControllerApi.postCompanyAssociatedData(
                    CompanyAssociatedDataEuTaxonomyData(
                        testCompanyId,
                        dummyDataCreator.createEuTaxonomyTestData(counterToMarkData)
                    )
                )
                counterToMarkData++
            }
            counterToMarkCompanies++
            listOfPostedTestCompanyIds.add(testCompanyId)
        }
        return listOfPostedTestCompanyIds
    }

    @Test
    fun `post a dummy company dummy taxonomy data for it and check if meta info about that data can be retrieved`() {
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
            "The meta info of the posted eu taxonomy data does not match the retrieved meta info."
        )
    }

    @Test
    fun `post several dummy companies and n dummy data sets for them and check filtering options`() {
        val numberOfCompanies = 5
        val numberOfDataSetsToPostPerCompany = 3
        val totalNumberOfDataSets = numberOfCompanies * numberOfDataSetsToPostPerCompany
        val initialSizeOfDataMetaInfoList = metaDataControllerApi.getListOfDataMetaInfo("", "").size
        val listOfTestCompanyIds =
            createCompaniesAndEuTaxonomyData(numberOfCompanies, numberOfDataSetsToPostPerCompany)
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
