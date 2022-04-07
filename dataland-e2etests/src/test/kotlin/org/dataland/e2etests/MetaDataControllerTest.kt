package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MetaDataControllerTest {

    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val dummyDataCreator = DummyDataCreator()

    private fun generateTestData(
        numberOfCompanies: Int,
        numberOfDataPerCompany: Int,
        companyMarker: Int,
        dataMarker: Int
    ):
        Map<CompanyInformation, List<EuTaxonomyData>> {
        val testData = mutableMapOf<CompanyInformation, List<EuTaxonomyData>>()
        for (companyCounter in companyMarker until companyMarker + numberOfCompanies) {
            val data = mutableListOf<EuTaxonomyData>()
            for (dataCounter in dataMarker until dataMarker + numberOfDataPerCompany) {
                data.add(dummyDataCreator.createEuTaxonomyTestData(dataCounter))
            }
            testData[dummyDataCreator.createCompanyTestInformation(companyCounter.toString())] = data
        }
        return testData
    }

    private fun postCompaniesAndEuTaxonomyData(testData: Map<CompanyInformation, List<EuTaxonomyData>>): List<String> {
        val listOfPostedTestCompanyIds = mutableListOf<String>()
        for ((company, data) in testData) {
            val testCompanyId = companyDataControllerApi.postCompany(company).companyId
            data.forEach {
                euTaxonomyDataControllerApi.postCompanyAssociatedData(
                    CompanyAssociatedDataEuTaxonomyData(testCompanyId, it)
                )
            }
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
    fun `post companies and eu taxonomy data and check meta info search with empty filters`() {
        val numberOfCompanies = 5
        val numberOfDataSetsToPostPerCompany = 3
        val totalNumberOfDataSets = numberOfCompanies * numberOfDataSetsToPostPerCompany
        val initialSizeOfDataMetaInfoList = metaDataControllerApi.getListOfDataMetaInfo("", "").size
        val testData = generateTestData(numberOfCompanies, numberOfDataSetsToPostPerCompany, 1000, 200000000)
        postCompaniesAndEuTaxonomyData(testData)
        val listOfDataMetaInfoComplete = metaDataControllerApi.getListOfDataMetaInfo("", "")
        assertEquals(initialSizeOfDataMetaInfoList + totalNumberOfDataSets, listOfDataMetaInfoComplete.size)
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on company ID`() {
        val numberOfCompanies = 3
        val numberOfDataSetsToPostPerCompany = 4
        val testData = generateTestData(numberOfCompanies, numberOfDataSetsToPostPerCompany, 2000, 300000000)
        val listOfTestCompanyIds = postCompaniesAndEuTaxonomyData(testData)
        val listOfDataMetaInfoPerCompanyId =
            metaDataControllerApi.getListOfDataMetaInfo(listOfTestCompanyIds.first(), "")
        assertEquals(numberOfDataSetsToPostPerCompany, listOfDataMetaInfoPerCompanyId.size)
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on data type`() {
        val numberOfCompanies = 4
        val numberOfDataSetsToPostPerCompany = 5
        val totalNumberOfDataSets = numberOfCompanies * numberOfDataSetsToPostPerCompany
        val initialSizeOfDataMetaInfoList = metaDataControllerApi.getListOfDataMetaInfo("", "").size
        val testData = generateTestData(numberOfCompanies, numberOfDataSetsToPostPerCompany, 3000, 400000000)
        postCompaniesAndEuTaxonomyData(testData)
        val listOfDataMetaInfoPerDataType = metaDataControllerApi.getListOfDataMetaInfo("", "EuTaxonomyData")
        assertEquals(initialSizeOfDataMetaInfoList + totalNumberOfDataSets, listOfDataMetaInfoPerDataType.size)
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filters on company ID and data type`() {
        val numberOfCompanies = 2
        val numberOfDataSetsToPostPerCompany = 6
        val testData = generateTestData(numberOfCompanies, numberOfDataSetsToPostPerCompany, 4000, 500000000)
        val listOfTestCompanyIds = postCompaniesAndEuTaxonomyData(testData)
        val listOfDataMetaInfoPerCompanyIdAndDataType =
            metaDataControllerApi.getListOfDataMetaInfo(listOfTestCompanyIds.first(), "EuTaxonomyData")
        assertEquals(numberOfDataSetsToPostPerCompany, listOfDataMetaInfoPerCompanyIdAndDataType.size)
    }
}
