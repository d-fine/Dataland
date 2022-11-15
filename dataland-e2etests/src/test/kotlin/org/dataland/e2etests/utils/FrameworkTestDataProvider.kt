package org.dataland.e2etests.utils

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
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.SmeData
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

class FrameworkTestDataProvider <T> (private val clazz: Class<T>) {

    private val jsonFilesForTesting = mapOf(
        EuTaxonomyDataForNonFinancials::class.java to
            File("./build/resources/CompanyInformationWithEuTaxonomyDataForNonFinancials.json"),
        EuTaxonomyDataForFinancials::class.java to
            File("./build/resources/CompanyInformationWithEuTaxonomyDataForFinancials.json"),
        LksgData::class.java to
            File("./build/resources/CompanyInformationWithLksgData.json"),
        SfdrData::class.java to
            File("./build/resources/CompanyInformationWithSfdrData.json"),
        SmeData::class.java to
            File("./build/resources/CompanyInformationWithSmeData.json")
    )

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
        .add(BigDecimalAdapter).add(LocalDateAdapter).build()

    private fun getJsonFileForTesting(): File {
        return jsonFilesForTesting[clazz]!!
    }

    private fun convertJsonToList(jsonFile: File): List<CompanyInformationWithT<T>> {
        val jsonFileAsString = jsonFile.inputStream().bufferedReader().readText()
        val parameterizedTypeOfCompanyInformationWithT = Types
            .newParameterizedType(CompanyInformationWithT::class.java, clazz)
        val parameterizedTypeOfConverterOutput = Types
            .newParameterizedType(List::class.java, parameterizedTypeOfCompanyInformationWithT)
        val jsonAdapter: JsonAdapter<List<CompanyInformationWithT<T>>> =
            moshi.adapter(parameterizedTypeOfConverterOutput)
        return jsonAdapter.fromJson(jsonFileAsString)!!
    }

    private val testCompanyInformationWithTData = convertJsonToList(getJsonFileForTesting())

    fun getCompanyInformationWithoutIdentifiers(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformationWithTData.slice(0 until requiredQuantity)
            .map { it.companyInformation.copy(identifiers = emptyList()) }
    }

    fun getTData(numberOfDataSets: Int): List<T> {
        return testCompanyInformationWithTData.slice(0 until numberOfDataSets)
            .map { it.t }
    }

    fun getCompaniesWithTDataAndNoIdentifiers(requiredNumberOfCompanies: Int, dataSetsPerCompany: Int):
        Map<CompanyInformation, List<T>> {
        val companies = getCompanyInformationWithoutIdentifiers(requiredNumberOfCompanies)
        return companies.associateWith { getTData(dataSetsPerCompany) }
    }
}
data class CompanyInformationWithT<T>(
    @Json var companyInformation: CompanyInformation,
    @Json var t: T
)
