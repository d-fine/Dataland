package org.dataland.csvconverter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.utils.CompanyInformationWithEuTaxonomyDataModel
import java.io.File

class TestDataProvider(objectMapper: ObjectMapper) {
    private val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyData.json")
    private val testCompanyInformationWithEuTaxonomyData: List<CompanyInformationWithEuTaxonomyDataModel> =
        objectMapper.readValue(jsonFile, object : TypeReference<List<CompanyInformationWithEuTaxonomyDataModel>>() {})

    fun getAllCompanyInformationWithEuTaxonomyData(): List<CompanyInformationWithEuTaxonomyDataModel> {
        return testCompanyInformationWithEuTaxonomyData
    }
}
