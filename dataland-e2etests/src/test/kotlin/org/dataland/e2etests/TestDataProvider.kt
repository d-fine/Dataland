package org.dataland.e2etests

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource

class TestDataProvider(@Autowired var objectMapper: ObjectMapper) {
    private var dataCounter = 0
    private val companyJson = ClassPathResource("/companies.json").file
    private val dataJson = ClassPathResource("/eutaxonomies.json").file
    private val companyType: JavaType = objectMapper.typeFactory.constructParametricType(
        List::class.java, CompanyInformation::class.java
    )
    private val dataType: JavaType = objectMapper.typeFactory.constructParametricType(
        List::class.java, EuTaxonomyData::class.java
    )
    private val testCompanyInformation: List<CompanyInformation> = objectMapper.readValue(companyJson, companyType)
    private val testData: List<EuTaxonomyData> = objectMapper.readValue(dataJson, dataType)

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformation.slice(0 until requiredQuantity)
    }

    fun getEuTaxonomyData(numberOfDataSets: Int): List<EuTaxonomyData> {
        if (dataCounter + numberOfDataSets > testData.size) {
            dataCounter = 0
        }
        val data = testData.slice(dataCounter until dataCounter + numberOfDataSets)
        dataCounter += numberOfDataSets
        return data
    }

    fun getCompaniesWithData(requiredCompanyNumber: Int, dataSetsPerCompany: Int):
            Map<CompanyInformation, List<EuTaxonomyData>> {
        if (dataSetsPerCompany > testData.size) {
            throw(
                IllegalArgumentException(
                    "Test data not big enough to provide $dataSetsPerCompany test data sets " +
                        "(it only has ${testData.size} elements)."
                )
                )
        }
        val companies = getCompanyInformation(requiredCompanyNumber)

        return companies.associateWith { getEuTaxonomyData(dataSetsPerCompany) }
    }
}
