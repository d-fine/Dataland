package org.dataland.csvconverter

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.EuTaxonomyData
import org.springframework.core.io.ClassPathResource

class TestDataProvider(private val objectMapper: ObjectMapper) {
    private val companyJsonFile = ClassPathResource("/CompanyInformation.json").file
    private val companyType: JavaType = objectMapper.typeFactory.constructParametricType(
        List::class.java, CompanyInformation::class.java
    )
    private val testCompanyInformation: List<CompanyInformation> = objectMapper.readValue(companyJsonFile, companyType)

    private val dataJsonFile = ClassPathResource("/CompanyAssociatedEuTaxonomyData.json").file
    private val dataType: JavaType = objectMapper.typeFactory.constructParametricType(
        List::class.java, CompanyAssociatedData("1", EuTaxonomyData())::class.java
    )
    private val testCompanyAssociatedEuTaxonomyData: List<CompanyAssociatedData<EuTaxonomyData>> =
        objectMapper.readValue(dataJsonFile, dataType)

    fun getAllCompanies(): List<CompanyInformation> {
        return testCompanyInformation
    }

    fun getAllData(): List<CompanyAssociatedData<EuTaxonomyData>> {
        return testCompanyAssociatedEuTaxonomyData
    }
}
