package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class CompanyDataControllerTest {

    private val basePathToDatalandProxy = "http://proxy:80/api"
    private val metaDataControllerApi = MetaDataControllerApi(basePathToDatalandProxy)
    private val companyDataControllerApi = CompanyDataControllerApi(basePathToDatalandProxy)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePathToDatalandProxy)
    private val testCompanyList1 = listOf(
        CompanyInformation(
            companyName = "Test-Company_1",
            headquarters = "Test-Headquarters_1",
            sector = "Test-Sector_1",
            marketCap = BigDecimal(100),
            reportingDateOfMarketCap = LocalDate.now()
        ),
        CompanyInformation(
            companyName = "Test-Company_2",
            headquarters = "Test-Headquarters_2",
            sector = "Test-Sector_2",
            marketCap = BigDecimal(200),
            reportingDateOfMarketCap = LocalDate.now()
        ),
        CompanyInformation(
            companyName = "Test-Company_3",
            headquarters = "Test-Headquarters_3",
            sector = "Test-Sector_3",
            marketCap = BigDecimal(500),
            reportingDateOfMarketCap = LocalDate.now()
        )
    )
    private val testCompanyList2 = listOf(
        CompanyInformation(
            companyName = "Test-Company_list1",
            headquarters = "Test-Headquarters_list1",
            sector = "Test-Sector_list1",
            marketCap = BigDecimal(300),
            reportingDateOfMarketCap = LocalDate.now()
        ),
        CompanyInformation(
            companyName = "Test-Company_list2",
            headquarters = "Test-Headquarters_list2",
            sector = "Test-Sector_list2",
            marketCap = BigDecimal(400),
            reportingDateOfMarketCap = LocalDate.now()
        )
    )

    @Test
    fun `post a dummy company and check if post was successful`() {
        val testCompanyInformation = testCompanyList1[0]

        val postCompanyResponse =
            companyDataControllerApi.postCompany(testCompanyInformation)

        assertEquals(
            testCompanyInformation.companyName, postCompanyResponse.companyInformation.companyName,
            "The company name in the post-response does not match the actual name of the company to be posted."
        )
        assertTrue(
            postCompanyResponse.companyId.toInt() > 0,
            "No valid company Id was assigned to the posted company."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val testCompanyInformation = testCompanyList1[1]
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyInformation)
        val getCompaniesByNameResponse = companyDataControllerApi.getCompaniesByName(testCompanyInformation.companyName)
        assertTrue(
            getCompaniesByNameResponse.contains(
                StoredCompany(
                    companyId = postCompanyResponse.companyId,
                    companyInformation = testCompanyInformation,
                    dataRegisteredByDataland = emptyList()
                )
            ),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {

        val allCompaniesListSizeBefore = companyDataControllerApi.getCompaniesByName("").size
        for (company in testCompanyList2) {
            companyDataControllerApi.postCompany(company)
        }
        val allCompaniesListSizeAfter = companyDataControllerApi.getCompaniesByName("").size
        assertEquals(
            testCompanyList2.size, allCompaniesListSizeAfter - allCompaniesListSizeBefore,
            "The size of the all-companies-list did not increase by ${testCompanyList2.size}."
        )
    }

    @Test
    fun `post a dummy company and a dummy data set for it and check if the company contains that data set ID`() {
        val testCompanyInformation = testCompanyList1[2]
        val testData = DummyDataCreator().createEuTaxonomyTestDataSet()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")

        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        ).dataId
        val listOfDataMetaInfoForTestCompany = metaDataControllerApi.getListOfDataMetaInfo(
            companyId = testCompanyId,
            dataType = testDataType
        )
        assertTrue(
            listOfDataMetaInfoForTestCompany.contains(
                DataMetaInformation(testDataId, testDataType, testCompanyId)
            ),
            "The all-data-sets-list of the posted company does not contain the posted data set."
        )
    }
}
