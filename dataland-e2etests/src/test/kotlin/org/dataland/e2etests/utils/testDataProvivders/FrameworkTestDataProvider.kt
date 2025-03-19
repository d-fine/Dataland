package org.dataland.e2etests.utils.testDataProvivders

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.dataland.datalandbackend.openApiClient.model.AdditionalCompanyInformationData
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
    private val dataFile: File,
) {
    data class FrameworkTestDataConfiguration(
        val fakeFixtureFile: File,
        val preparedFixtureFile: File,
    )

    companion object {
        private val jsonFilesForTesting =
            mapOf(
                EutaxonomyNonFinancialsData::class.java to
                    FrameworkTestDataConfiguration(
                        File("./build/resources/test/CompanyInformationWithEutaxonomyNonFinancialsData.json"),
                        File("./build/resources/test/CompanyInformationWithEutaxonomyNonFinancialsPreparedFixtures.json"),
                    ),
                EutaxonomyFinancialsData::class.java to
                    FrameworkTestDataConfiguration(
                        File("./build/resources/test/CompanyInformationWithEutaxonomyFinancialsData.json"),
                        File("./build/resources/test/CompanyInformationWithEutaxonomyFinancialsPreparedFixtures.json"),
                    ),
                LksgData::class.java to
                    FrameworkTestDataConfiguration(
                        File("./build/resources/test/CompanyInformationWithLksgData.json"),
                        File("./build/resources/test/CompanyInformationWithLksgPreparedFixtures.json"),
                    ),
                SfdrData::class.java to
                    FrameworkTestDataConfiguration(
                        File("./build/resources/test/CompanyInformationWithSfdrData.json"),
                        File("./build/resources/test/CompanyInformationWithSfdrPreparedFixtures.json"),
                    ),
                VsmeData::class.java to
                    FrameworkTestDataConfiguration(
                        File("./build/resources/test/CompanyInformationWithVsmeData.json"),
                        File("./build/resources/test/CompanyInformationWithVsmePreparedFixtures.json"),
                    ),
                PathwaysToParisData::class.java to
                    FrameworkTestDataConfiguration(
                        File("./build/resources/test/CompanyInformationWithP2pData.json"),
                        File("./build/resources/test/CompanyInformationWithP2pPreparedFixtures.json"),
                    ),
                AdditionalCompanyInformationData::class.java to
                    FrameworkTestDataConfiguration(
                        File("./build/resources/test/CompanyInformationWithAdditionalCompanyInformationData.json"),
                        File("./build/resources/test/CompanyInformationWithAdditionalCompanyInformationPreparedFixtures.json"),
                    ),
            )

        fun <T> forFrameworkFixtures(clazz: Class<T>): FrameworkTestDataProvider<T> =
            FrameworkTestDataProvider(clazz, (jsonFilesForTesting[clazz] ?: throw NoSuchElementException()).fakeFixtureFile)

        fun <T> forFrameworkPreparedFixtures(clazz: Class<T>): FrameworkTestDataProvider<T> =
            FrameworkTestDataProvider(clazz, (jsonFilesForTesting[clazz] ?: throw NoSuchElementException()).preparedFixtureFile)
    }

    private val moshi: Moshi =
        Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .add(BigDecimalAdapter)
            .add(LocalDateAdapter)
            .build()

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

    private val testCompanyInformationWithTData = convertJsonToList(dataFile)

    fun getCompanyInformationWithoutIdentifiers(requiredQuantity: Int): List<CompanyInformation> =
        testCompanyInformationWithTData
            .slice(0 until requiredQuantity)
            .map {
                it.companyInformation.copy(
                    identifiers = IdentifierType.entries.map { id -> id.value }.associateWith { emptyList() },
                    companyContactDetails = emptyList(),
                )
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

    fun getByCompanyName(companyName: String): CompanyInformationWithT<T> =
        testCompanyInformationWithTData
            .first { it.companyInformation.companyName == companyName }

    fun getTData(numberOfDataSets: Int): List<T> =
        testCompanyInformationWithTData
            .slice(0 until numberOfDataSets)
            .map { it.t }
}
