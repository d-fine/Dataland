package org.dataland.e2etests

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import java.io.File
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

class TestDataProvider <T> (private val clazz: Class<T>) {

    private val jsonFilesForTesting = mapOf(
        EuTaxonomyDataForNonFinancials::class.java to File("./build/resources/CompanyInformationWithEuTaxonomyDataForNonFinancials.json"),
        EuTaxonomyDataForFinancials::class.java to File("./build/resources/CompanyInformationWithEuTaxonomyDataForFinancials.json")
    )

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
        .add(BigDecimalAdapter).add(LocalDateAdapter).build()


    private fun getJsonFileForTesting(): File{
        return jsonFilesForTesting[clazz]!!
    }

    private fun convertJsonToList(jsonFile: File): List<CompanyInformationWithEuTaxonomyData<T>> {
        val jsonFileAsString = jsonFile.inputStream().bufferedReader().readText()
        val parameterizedType = Types
            .newParameterizedType(List::class.java, CompanyInformationWithEuTaxonomyData::class.java)
        val jsonAdapter: JsonAdapter<List<CompanyInformationWithEuTaxonomyData<T>>> = moshi.adapter(parameterizedType)
        return jsonAdapter.fromJson(jsonFileAsString)!!
    }

    private val testCompanyInformationWithEuTaxonomyData = convertJsonToList(getJsonFileForTesting())






    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformationWithEuTaxonomyData.slice(0 until requiredQuantity)
                .map { it.companyInformation }
        }


    fun getEuTaxonomyData(numberOfDataSets: Int): List<T> {
        return testCompanyInformationWithEuTaxonomyData.slice(0 until numberOfDataSets)
            .map { it.euTaxonomyData }
    }

    fun getCompaniesWithEuTaxonomyData(requiredNumberOfCompanies: Int, dataSetsPerCompany: Int):
        Map<CompanyInformation, List<T>> {
        val companies = getCompanyInformation(requiredNumberOfCompanies)
        return companies.associateWith { getEuTaxonomyData(dataSetsPerCompany) }
    }
}

data class CompanyInformationWithEuTaxonomyData<T> (
    @Json var companyInformation: CompanyInformation,
    @Json var euTaxonomyData: T
    )




