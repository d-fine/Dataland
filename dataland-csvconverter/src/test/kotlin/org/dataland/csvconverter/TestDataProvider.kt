package org.dataland.csvconverter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.EuTaxonomyData
import org.springframework.core.io.ClassPathResource

class TestDataProvider(private val objectMapper: ObjectMapper) {
    private val companyJsonFile = ClassPathResource("/CompanyInformation.json").file
    private val testCompanyInformation: List<CompanyInformation> =
        objectMapper.readValue(companyJsonFile, object : TypeReference<List<CompanyInformation>>() {})

    private val dataJsonFile = ClassPathResource("/CompanyAssociatedEuTaxonomyData.json").file
    private val testCompanyAssociatedEuTaxonomyData: List<CompanyAssociatedData<EuTaxonomyData>> =
        objectMapper.readValue(dataJsonFile, object : TypeReference<List<CompanyAssociatedData<EuTaxonomyData>>>() {})

    fun getAllCompanies(): List<CompanyInformation> {
        return testCompanyInformation
    }

    fun getAllData(): List<CompanyAssociatedData<EuTaxonomyData>> {
        return testCompanyAssociatedEuTaxonomyData
    }
}
