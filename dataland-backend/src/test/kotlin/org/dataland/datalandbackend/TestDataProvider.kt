package org.dataland.datalandbackend

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.EuTaxonomyData
import org.springframework.beans.factory.annotation.Autowired
import java.io.File

class TestDataProvider(@Autowired var objectMapper: ObjectMapper) {
    private val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyData.json")
    private val testCompanyWithData =
        objectMapper.readValue(jsonFile, object : TypeReference<List<CompanyWithData>>() {})

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyWithData.slice(0 until requiredQuantity).map { it.companyInformation }
    }
}

data class CompanyWithData(
    val companyInformation: CompanyInformation,
    val euTaxonomyData: EuTaxonomyData
)
