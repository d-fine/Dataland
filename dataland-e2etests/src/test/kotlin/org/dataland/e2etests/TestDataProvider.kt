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
    private val companyWithDataJson = File("./build/resources/CompanyInformationWithEuTaxonomyData.json")
    private val companyJsonString = companyWithDataJson.inputStream().bufferedReader().readText()

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
        .add(BigDecimalAdapter).add(LocalDateAdapter).build()
    private val companyWithDataType: ParameterizedType = Types
        .newParameterizedType(List::class.java, CompanyWithData::class.java)
    private val companyJsonAdapter: JsonAdapter<List<CompanyWithData>> = moshi.adapter(companyWithDataType)

    private val testCompanyInformation: List<CompanyWithData> = companyJsonAdapter
        .fromJson(companyJsonString) ?: emptyList()

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformation.slice(0 until requiredQuantity).map { it.companyInformation }
    }

    fun getEuTaxonomyData(numberOfDataSets: Int): List<EuTaxonomyData> {
        return testCompanyInformation.slice(0 until numberOfDataSets).map { it.euTaxonomyData }
    }

    fun getCompaniesWithData(requiredNumberOfCompanies: Int, dataSetsPerCompany: Int):
        Map<CompanyInformation, List<EuTaxonomyData>> {
        if (dataSetsPerCompany > testCompanyInformation.size) {
            throw(
                IllegalArgumentException(
                    "Test data not big enough to provide $dataSetsPerCompany test data sets " +
                        "(it only has ${testCompanyInformation.size} elements)."
                )
                )
        }
        val companies = getCompanyInformation(requiredNumberOfCompanies)

        return companies.associateWith { getEuTaxonomyData(dataSetsPerCompany) }
    }
}

data class CompanyWithData(
    val companyInformation: CompanyInformation,
    val euTaxonomyData: EuTaxonomyData
)
