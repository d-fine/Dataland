package org.dataland.datalandbackend

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.springframework.beans.factory.annotation.Autowired
import java.io.File

class TestDataProvider(@Autowired var objectMapper: ObjectMapper) {
    private val jsonFile = File("./build/resources/CompanyInformation.json")
    private val testCompanyInformation =
        objectMapper.readValue(jsonFile, object : TypeReference<List<CompanyInformation>>() {})

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformation.slice(0 until requiredQuantity)
    }
}
