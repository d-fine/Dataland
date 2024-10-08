package org.dataland.e2etests.utils.testDataProvivders

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyFinancialsData
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.PathwaysToParisData
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.VsmeData
import java.io.File
import java.util.UUID

class FrameworkTestDataProvider<T>(
    private val clazz: Class<T>,
) {
    private val jsonFilesForTesting =
        mapOf(
            EutaxonomyNonFinancialsData::class.java to
                File("./build/resources/test/CompanyInformationWithEutaxonomyNonFinancialsData.json"),
            EutaxonomyFinancialsData::class.java to
                File("./build/resources/test/CompanyInformationWithEutaxonomyFinancialsData.json"),
            LksgData::class.java to
                File("./build/resources/test/CompanyInformationWithLksgData.json"),
            SfdrData::class.java to
                File("./build/resources/test/CompanyInformationWithSfdrData.json"),
            VsmeData::class.java to
                File("./build/resources/test/CompanyInformationWithVsmeData.json"),
            PathwaysToParisData::class.java to
                File("./build/resources/test/CompanyInformationWithP2pData.json"),
        )

    private val moshi: Moshi =
        Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .add(BigDecimalAdapter)
            .add(LocalDateAdapter)
            .build()

    private fun getJsonFileForTesting(): File = jsonFilesForTesting[clazz]!!

    private fun convertJsonToList(jsonFile: File): List<CompanyInformationWithT<T>> {
        val jsonFileAsString = jsonFile.inputStream().bufferedReader().readText()
        val parameterizedTypeOfCompanyInformationWithT =
            Types
                .newParameterizedType(CompanyInformationWithT::class.java, clazz)
        val parameterizedTypeOfConverterOutput =
            Types
                .newParameterizedType(List::class.java, parameterizedTypeOfCompanyInformationWithT)
        val jsonAdapter: JsonAdapter<List<CompanyInformationWithT<T>>> =
            moshi.adapter(parameterizedTypeOfConverterOutput)
        return jsonAdapter.fromJson(jsonFileAsString)!!
    }

    private val testCompanyInformationWithTData = convertJsonToList(getJsonFileForTesting())

    fun getCompanyInformationWithoutIdentifiers(requiredQuantity: Int): List<CompanyInformation> =
        testCompanyInformationWithTData
            .slice(0 until requiredQuantity)
            .map {
                it.companyInformation.copy(
                    identifiers = IdentifierType.entries.map { id -> id.value }.associateWith { emptyList() },
                    companyContactDetails = emptyList(),
                )
            }

    private fun companyListForTestingLksgSpecificValidation(): List<CompanyInformationWithT<T>> =
        convertJsonToList(File("./build/resources/test/CompanyInformationWithLksgPreparedFixtures.json"))

    private fun companyListForTestingSfdrSpecificValidation(): List<CompanyInformationWithT<T>> =
        convertJsonToList(File("./build/resources/test/CompanyInformationWithSfdrPreparedFixtures.json"))

    private fun companyListForTestingEuTaxonomyNonFinancialsSpecificValidation(): List<CompanyInformationWithT<T>> =
        convertJsonToList(
            File("./build/resources/test/CompanyInformationWithEutaxonomyNonFinancialsPreparedFixtures.json"),
        )

    private fun companyListForTestingEuTaxonomyFinancialsSpecificValidation(): List<CompanyInformationWithT<T>> =
        convertJsonToList(
            File("./build/resources/test/CompanyInformationWithEutaxonomyFinancialsPreparedFixtures.json"),
        )

    fun getSpecificCompanyByNameFromLksgPreparedFixtures(companyName: String): CompanyInformationWithT<T>? =
        companyListForTestingLksgSpecificValidation().find {
            it.companyInformation.companyName == companyName
        }

    fun getSpecificCompanyByNameFromSfdrPreparedFixtures(companyName: String): CompanyInformationWithT<T>? =
        companyListForTestingSfdrSpecificValidation().find {
            it.companyInformation.companyName == companyName
        }

    fun getSpecificCompanyByNameFromEuTaxonomyNonFinancialsPreparedFixtures(companyName: String): CompanyInformationWithT<T>? =
        companyListForTestingEuTaxonomyNonFinancialsSpecificValidation().find {
            it.companyInformation.companyName == companyName
        }

    fun getSpecificCompanyByNameFromEuTaxonomyFinancialsPreparedFixtures(companyName: String): CompanyInformationWithT<T>? =
        companyListForTestingEuTaxonomyFinancialsSpecificValidation().find {
            it.companyInformation.companyName == companyName
        }

    fun getCompanyInformationWithRandomIdentifiers(requiredQuantity: Int): List<CompanyInformation> =
        testCompanyInformationWithTData
            .slice(0 until requiredQuantity)
            .map {
                it.companyInformation.copy(
                    identifiers =
                        mapOf(
                            IdentifierType.Isin.value to listOf(UUID.randomUUID().toString()),
                        ),
                )
            }

    fun getTData(numberOfDataSets: Int): List<T> =
        testCompanyInformationWithTData
            .slice(0 until numberOfDataSets)
            .map { it.t }
}
