package org.dataland.csvconverter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.csvconverter.model.CompanyInformationWithEuTaxonomyData
import java.io.File

class TestDataProvider(objectMapper: ObjectMapper) {
    private val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyData.json")
    private val testCompanyInformationWithEuTaxonomyData: List<CompanyInformationWithEuTaxonomyData> =
        objectMapper.readValue(jsonFile, object : TypeReference<List<CompanyInformationWithEuTaxonomyData>>() {})

    fun getAllCompanyInformationWithEuTaxonomyData(): List<CompanyInformationWithEuTaxonomyData> {
        return testCompanyInformationWithEuTaxonomyData
    }
}
