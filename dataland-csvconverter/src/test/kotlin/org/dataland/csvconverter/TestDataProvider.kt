package org.dataland.csvconverter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.moshi.Types
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.EuTaxonomyData
import java.io.File
import java.lang.reflect.ParameterizedType

class TestDataProvider(private val objectMapper: ObjectMapper) {
    private val companyJsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyData.json")
    private val type: JavaType = objectMapper.typeFactory.constructParametricType(
        Pair::class.java, CompanyInformation::class.java, EuTaxonomyData::class.java
    )
    private val listType: JavaType = objectMapper.typeFactory.constructParametricType(
        List::class.java, type)
    private val testCompanyWithData: List<Pair<CompanyInformation, EuTaxonomyData>> =
        objectMapper.readValue(companyJsonFile, listType)

    fun getAllCompanies(): List<CompanyInformation> {
        return testCompanyWithData.map { it.first }
    }

    fun getAllData(): List<EuTaxonomyData> {
        return testCompanyWithData.map { it.second }
    }
}
