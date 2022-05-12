package org.dataland.e2etests

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import java.io.File
import java.lang.reflect.ParameterizedType
import java.math.BigDecimal
import java.time.LocalDate

object BigDecimalAdapter {
    @FromJson
    fun fromJson(string: String) = BigDecimal(string)

    @ToJson
    fun toJson(value: BigDecimal) = value.toString()
}

object LocalDateAdapter {
    @FromJson
    fun fromJson(string: String) = LocalDate.parse(string)

    @ToJson
    fun toJson(value: LocalDate) = value.toString()
}

class TestDataProvider {
    private var dataCounter = 0

    private val companyWithDataJson = File("./build/resources/CompanyInformationWithEuTaxonomyData.json")
    private val companyJsonString = companyWithDataJson.inputStream().bufferedReader().readText()

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
        .add(BigDecimalAdapter).add(LocalDateAdapter).build()
    private val companyWithDataType: ParameterizedType = Types
        .newParameterizedType(List::class.java, CompanyInformation::class.java, EuTaxonomyData::class.java)
    private val companyJsonAdapter: JsonAdapter<List<Pair<CompanyInformation, EuTaxonomyData>>> = moshi.adapter(companyWithDataType)

    private val testCompanyInformation: List<CompanyInformation> = companyJsonAdapter
        .fromJson(companyJsonString) ?: emptyList()
    private val testData: List<EuTaxonomyData> = dataJsonAdapter
        .fromJson(dataJsonString) ?: emptyList()

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformation.slice(0 until requiredQuantity)
    }

    fun getEuTaxonomyData(numberOfDataSets: Int): List<EuTaxonomyData> {
        if (dataCounter + numberOfDataSets > testData.size) {
            dataCounter = 0
        }
        val data = testData.slice(dataCounter until dataCounter + numberOfDataSets)
        dataCounter += numberOfDataSets

        return data
    }

    fun getCompaniesWithData(requiredNumberOfCompanies: Int, dataSetsPerCompany: Int):
        Map<CompanyInformation, List<EuTaxonomyData>> {
        if (dataSetsPerCompany > testData.size) {
            throw(
                IllegalArgumentException(
                    "Test data not big enough to provide $dataSetsPerCompany test data sets " +
                        "(it only has ${testData.size} elements)."
                )
                )
        }
        val companies = getCompanyInformation(requiredNumberOfCompanies)

        return companies.associateWith { getEuTaxonomyData(dataSetsPerCompany) }
    }
}
