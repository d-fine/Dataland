package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class MetaDataControllerTest {

    private val basePathToDatalandProxy = "http://proxy:80/api"
    private val metaDataControllerApi = MetaDataControllerApi(basePathToDatalandProxy)
    private val companyDataControllerApi = CompanyDataControllerApi(basePathToDatalandProxy)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePathToDatalandProxy)

    private fun populateCompaniesAndEuTaxonomyDataSets(
        numberOfCompanies: Int,
        numberOfDataSetsPerCompany: Int
    ): List<String> {
        val testCompanyInformation = CompanyInformation(
            companyName = "Test-Company_20",
            headquarters = "Test-Headquarters_20",
            industrialSector = "Test-IndustrialSector_20",
            marketCap = BigDecimal(200),
            reportingDateOfMarketCap = LocalDate.now()
        )
        val testData = DummyDataCreator().createEuTaxonomyTestDataSet()

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
    fun `post a dummy company with dummy data set and check if its meta info can be retrieved`() {
        val testCompanyInformation = CompanyInformation(
            companyName = "Non-Existent_1",
            headquarters = "Non-Existent-Headquarters_1",
            industrialSector = "Non-Existent-IndustrialSector_2",
            marketCap = BigDecimal(200),
            reportingDateOfMarketCap = LocalDate.now()
        )
        val testData = DummyDataCreator().createEuTaxonomyTestDataSet()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")

        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
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
            populateCompaniesAndEuTaxonomyDataSets(numberOfCompanies, numberOfDataSetsToPostPerCompany)
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
