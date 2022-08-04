package org.dataland.csvconverter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.utils.CompanyInformationWithEuTaxonomyDataForNonFinancialsModel
import java.io.File

class TestDataProvider(objectMapper: ObjectMapper) {
    private val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyDataForNonFinancials.json")
    private val testCompanyInformationWithEuTaxonomyData: List<CompanyInformationWithEuTaxonomyDataForNonFinancialsModel> =
        objectMapper.readValue(jsonFile, object : TypeReference<List<CompanyInformationWithEuTaxonomyDataForNonFinancialsModel>>() {})

    fun getAllCompanyInformationWithEuTaxonomyData(): List<CompanyInformationWithEuTaxonomyDataForNonFinancialsModel> {
        return testCompanyInformationWithEuTaxonomyData
    }
}
