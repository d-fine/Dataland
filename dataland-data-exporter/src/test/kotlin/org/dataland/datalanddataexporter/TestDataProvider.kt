package org.dataland.datalanddataexporter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import java.io.File
import java.text.SimpleDateFormat

/**
 *  A provider for test data used in different tests.
 * */
object TestDataProvider {
    private val sfdrJsonFile = File("./src/test/resources/csv/inputs/mockSfdrData.json")
    private val companyInformationWithSfdrPreparedFixturesFile =
        File("./build/resources/test/CompanyInformationWithSfdrPreparedFixtures.json")

    private fun getMockSfdrObjectMapper(): ObjectMapper {
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return objectMapper
    }

    private val moshi: Moshi =
        Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .add(BigDecimalAdapter)
            .add(LocalDateAdapter)
            .build()

    /**
     * This function loads test data for the SFDR framework and reads it into the SfdrData class
     */
    fun getMockSfdrData(): SfdrData = getMockSfdrObjectMapper().readValue(sfdrJsonFile, SfdrData::class.java)

    /**
     * This function loads tests data for the SFDR framework and reads it into a JSONNode
     */
    fun getMockSfdrJsonNode(): JsonNode = getMockSfdrObjectMapper().readTree(sfdrJsonFile)

    /**
     * This function gets the sfdr dataset with no null fields
     */
    fun getMockSfdrDataWithNoNullFields(): SfdrData {
        val test =
            getSfdrDataList().find {
                it.companyInformation.companyName == "Sfdr-dataset-with-no-null-fields"
            }

        return test?.t ?: throw NoSuchElementException("Sfdr dataset with no null fields not found.")
    }

    /**
     * This function converts a JSON to a list
     */
    private fun getSfdrDataList(): List<CompanyInformationWithT<SfdrData>> {
        val jsonFileAsString = companyInformationWithSfdrPreparedFixturesFile.inputStream().bufferedReader().readText()
        val parameterizedTypeOfCompanyInformationWithT =
            Types
                .newParameterizedType(CompanyInformationWithT::class.java, SfdrData::class.java)
        val parameterizedTypeOfConverterOutput =
            Types
                .newParameterizedType(List::class.java, parameterizedTypeOfCompanyInformationWithT)
        val jsonAdapter: JsonAdapter<List<CompanyInformationWithT<SfdrData>>> =
            moshi.adapter(parameterizedTypeOfConverterOutput)
        return jsonAdapter.fromJson(jsonFileAsString)!!
    }
}
