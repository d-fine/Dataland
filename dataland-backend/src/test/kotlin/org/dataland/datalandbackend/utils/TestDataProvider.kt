package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.springframework.beans.factory.annotation.Autowired
import java.io.File

class TestDataProvider(@Autowired var objectMapper: ObjectMapper) {
    private val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyDataForNonFinancials.json")
    private val testCompanyInformationWithEuTaxonomyData =
        objectMapper.readValue(
            jsonFile,
            object : TypeReference<List<CompanyInformationWithEuTaxonomyDataForNonFinancialsModel>>() {}
        )

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformationWithEuTaxonomyData.slice(0 until requiredQuantity).map { it.companyInformation }
    }
}
