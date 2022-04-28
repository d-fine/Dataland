package org.dataland.datalandbackend

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource

class TestDataProvider(@Autowired var objectMapper: ObjectMapper) {
    private val jsonFile = ClassPathResource("/CompanyInformation.json").file
    private val type: JavaType = objectMapper.typeFactory.constructParametricType(
        List::class.java, CompanyInformation::class.java
    )
    private val testCompanyInformation: List<CompanyInformation> = objectMapper.readValue(jsonFile, type)

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformation.slice(0 until requiredQuantity)
    }
}
