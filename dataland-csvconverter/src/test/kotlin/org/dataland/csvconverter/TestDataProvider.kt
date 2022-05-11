package org.dataland.csvconverter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.EuTaxonomyData
import java.io.File

class TestDataProvider(private val objectMapper: ObjectMapper) {
    private val companyJsonFile = File("./build/resources/CompanyInformation.json")
    private val testCompanyInformation: List<CompanyInformation> =
        objectMapper.readValue(companyJsonFile, object : TypeReference<List<CompanyInformation>>() {})

    private val dataJsonFile = File("./build/resources/EuTaxonomyData.json")
    private val testCompanyAssociatedEuTaxonomyData =
        objectMapper.readValue(dataJsonFile, object : TypeReference<List<EuTaxonomyData>>() {})

    fun getAllCompanies(): List<CompanyInformation> {
        return testCompanyInformation
    }

    fun getAllData(): List<EuTaxonomyData> {
        return testCompanyAssociatedEuTaxonomyData
    }
}
