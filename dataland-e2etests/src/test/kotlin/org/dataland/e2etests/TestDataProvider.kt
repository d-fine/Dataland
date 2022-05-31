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
    private val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyData.json")
    private val jsonFileAsString = jsonFile.inputStream().bufferedReader().readText()

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
        .add(BigDecimalAdapter).add(LocalDateAdapter).build()
    private val parameterizedType: ParameterizedType = Types
        .newParameterizedType(List::class.java, CompanyInformationWithEuTaxonomyDataModel::class.java)
    private val jsonAdapter: JsonAdapter<List<CompanyInformationWithEuTaxonomyDataModel>> =
        moshi.adapter(parameterizedType)

    private val testCompanyInformationWithEuTaxonomyData: List<CompanyInformationWithEuTaxonomyDataModel> = jsonAdapter
        .fromJson(jsonFileAsString) ?: emptyList()

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformationWithEuTaxonomyData.slice(0 until requiredQuantity).map { it.companyInformation }
    }

    fun getEuTaxonomyData(numberOfDataSets: Int): List<EuTaxonomyData> {
        return testCompanyInformationWithEuTaxonomyData.slice(0 until numberOfDataSets).map { it.euTaxonomyData }
    }

    fun getCompaniesWithData(requiredNumberOfCompanies: Int, dataSetsPerCompany: Int):
        Map<CompanyInformation, List<EuTaxonomyData>> {
        val companies = getCompanyInformation(requiredNumberOfCompanies)
        return companies.associateWith { getEuTaxonomyData(dataSetsPerCompany) }
    }

    fun getFakeTeaserCompany(): CompanyInformation {
        return testCompanyInformationWithEuTaxonomyData[TEASER_COMPANY_INDEX_IN_FIXTURES].companyInformation
    }
}

data class CompanyInformationWithEuTaxonomyDataModel(
    val companyInformation: CompanyInformation,
    val euTaxonomyData: EuTaxonomyData
)
