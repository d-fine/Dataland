package org.dataland.datalandbackend

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource

class TestDataProvider(@Autowired var objectMapper: ObjectMapper) {
    private val jsonFile = ClassPathResource("/CompanyInformation.json").file
    private val testCompanyInformation: List<CompanyInformation> =
        objectMapper.readValue(jsonFile, object : TypeReference<List<CompanyInformation>>() {})

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformation.slice(0 until requiredQuantity)
    }
}
