package org.dataland.csvconverter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.EuTaxonomyData
import java.io.File

class TestDataProvider(private val objectMapper: ObjectMapper) {
    private val companyJsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyData.json")
    private val testCompanyInformation: List<CompanyWithData> =
        objectMapper.readValue(companyJsonFile, object : TypeReference<List<CompanyWithData>>() {})

    fun getAllCompanies(): List<CompanyInformation> {
        return testCompanyInformation.map { it.companyInformation }
    }

    fun getAllData(): List<EuTaxonomyData> {
        return testCompanyInformation.map { it.euTaxonomyData }
    }
}

data class CompanyWithData(
    val companyInformation: CompanyInformation,
    val euTaxonomyData: EuTaxonomyData
)
