package org.dataland.e2etests.utils.testDataProvivders

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import org.dataland.datalandqaservice.openApiClient.model.EutaxonomyNonFinancialsData as EuTaxonomyNonFinancialsQaReport
import org.dataland.datalandqaservice.openApiClient.model.SfdrData as SfdrQaReport

class QaReportTestDataProvider<T>(private val clazz: Class<T>, private val framework: String) {

    private val sfdrJsonFilesForTesting = mapOf(
        SfdrQaReport::class.java to File("./build/resources/test/SfdrQaReportPreparedFixtures.json"),
        /*
        The following line of code is a dummy entry to avoid a compilation error.
        The compilation error occurs if this map has only one entry.
        Feel free to remove the dummy entry as soon as you have added an actual "real" second entry to this map.
         */
        String::class.java to File("./"),
    )

    private val euTaxonomyNonFinancialsJsonFilesForTesting = mapOf(
        EuTaxonomyNonFinancialsQaReport::class.java to
            File("./build/resources/test/EuTaxonomyNonFinancialsQaReportPreparedFixtures.json"),
        /*
        The following line of code is a dummy entry to avoid a compilation error.
        The compilation error occurs if this map has only one entry.
        Feel free to remove the dummy entry as soon as you have added an actual "real" second entry to this map.
         */
        String::class.java to File("./"),
    )

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
        .add(BigDecimalAdapter).add(LocalDateAdapter).build()

    private fun getJsonFileForTesting(): File {
        val file: File? = when (framework) {
            "sfdr" -> sfdrJsonFilesForTesting[clazz]
            "eutaxonomy-non-financials" -> euTaxonomyNonFinancialsJsonFilesForTesting[clazz]
            else -> null
        }

        if (file == null) {
            throw if (framework == "sfdr" || framework == "eutaxonomy-non-financials") {
                IllegalArgumentException("No JSON file for testing found for class $clazz")
            } else {
                NotImplementedError("QaReportTestDataProvider is not implemented for framework $framework")
            }
        }

        return file
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

    fun getTData(numberOfDataSets: Int): List<T> {
        return testCompanyInformationWithTData.slice(0 until numberOfDataSets)
            .map { it.t }
    }
}
