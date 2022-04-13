package org.dataland.datalandbackend

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.springframework.core.io.ClassPathResource


//import com.fasterxml.jackson.databind.

class TestDataProvider {
    var mapper = ObjectMapper()
    val jsonFile = ClassPathResource("/testData/companies.json").file
    //val jsonFile = javaClass.getResource("companies.json").file
    //val test = ClassPathResource
    val type = mapper.getTypeFactory().constructParametricType(List::class.java, CompanyInformation::class.java)
    val testCompanyInformation: List<CompanyInformation> = mapper.readValue(jsonFile, type)
    //val testCompanyInformation2 = mapper.readValue(jsonFile, CompanyInformationList::class.java)

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformation.slice(0 until requiredQuantity)
    }
}
//data class CompanyInformationList (val test: List<CompanyInformation>)