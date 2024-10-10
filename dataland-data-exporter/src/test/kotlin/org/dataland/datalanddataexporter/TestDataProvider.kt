package org.dataland.datalanddataexporter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import java.io.File
import java.text.SimpleDateFormat

/**
 *  A provider for test data used in different tests.
 * */
object TestDataProvider {
    private val sfdrJsonFilePath = File("./src/test/resources/csv/inputs/mockSfdrData.json")

    private fun getMockSfdrObjectMapper(): ObjectMapper {
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return objectMapper
    }

    /**
     * This function loads test data for the SFDR framework and reads it into the SfdrData class
     */
    fun getMockSfdrData(): SfdrData = getMockSfdrObjectMapper().readValue(sfdrJsonFilePath, SfdrData::class.java)

    /**
     * This function loads tests data for the SFDR framework and reads it into a JSONNode
     */
    fun getMockSfdrJsonNode(): JsonNode = getMockSfdrObjectMapper().readTree(sfdrJsonFilePath)
}
