package org.dataland.e2etests

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
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
    private val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyDataForNonFinancials.json")
    private val jsonFileAsString = jsonFile.inputStream().bufferedReader().readText()

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
        .add(BigDecimalAdapter).add(LocalDateAdapter).build()
    private val parameterizedType: ParameterizedType = Types
        .newParameterizedType(List::class.java, CompanyInformationWithEuTaxonomyDataForNonFinancials::class.java)
    private val jsonAdapter: JsonAdapter<List<CompanyInformationWithEuTaxonomyDataForNonFinancials>> =
        moshi.adapter(parameterizedType)

    private val testCompanyInformationWithEuTaxonomyDataForNonFinancials:
        List<CompanyInformationWithEuTaxonomyDataForNonFinancials> =
            jsonAdapter.fromJson(jsonFileAsString) ?: emptyList()

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformationWithEuTaxonomyDataForNonFinancials.slice(0 until requiredQuantity)
            .map { it.companyInformation }
    }

    fun getEuTaxonomyDataForNonFinancials(numberOfDataSets: Int): List<EuTaxonomyDataForNonFinancials> {
        return testCompanyInformationWithEuTaxonomyDataForNonFinancials.slice(0 until numberOfDataSets)
            .map { it.euTaxonomyDataForNonFinancials }
    }

    fun getCompaniesWithEuTaxonomyDataForNonFinancials(requiredNumberOfCompanies: Int, dataSetsPerCompany: Int):
        Map<CompanyInformation, List<EuTaxonomyDataForNonFinancials>> {
        val companies = getCompanyInformation(requiredNumberOfCompanies)
        return companies.associateWith { getEuTaxonomyDataForNonFinancials(dataSetsPerCompany) }
    }
}

data class CompanyInformationWithEuTaxonomyDataForNonFinancials(
    val companyInformation: CompanyInformation,
    val euTaxonomyDataForNonFinancials: EuTaxonomyDataForNonFinancials
)
