package org.dataland.datalanddataexporter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import java.io.File
import java.text.SimpleDateFormat

/**
 *  A provider for test data used in different tests.
 * */
object TestDataProvider {
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()

    private val sfdrJsonFile = File("./src/test/resources/csv/inputs/mockSfdrData.json")
    private val companyInformationWithSfdrPreparedFixturesFile =
        File("./build/resources/test/CompanyInformationWithSfdrPreparedFixtures.json")

    private fun getMockSfdrObjectMapper(): ObjectMapper {
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return objectMapper
    }

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
        val data =
            getSfdrDataList().find {
                it.companyInformation.companyName == "Sfdr-dataset-with-no-null-fields"
            }

        return data?.t ?: throw NoSuchElementException("Sfdr dataset with no null fields not found.")
    }

    /**
     * This function converts a JSON to a list
     */
    private fun getSfdrDataList(): List<CompanyInformationWithT<SfdrData>> =
        objectMapper.readValue<List<CompanyInformationWithT<SfdrData>>>(
            companyInformationWithSfdrPreparedFixturesFile.inputStream(),
        )
}
