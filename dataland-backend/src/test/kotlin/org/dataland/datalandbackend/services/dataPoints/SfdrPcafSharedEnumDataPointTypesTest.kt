package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandbackend.frameworks.pcaf.model.general.company.PcafGeneralCompanyCompanyExchangeStatusOptions
import org.dataland.datalandbackend.frameworks.pcaf.model.general.company.PcafGeneralCompanyMainPcafSectorOptions
import org.dataland.datalandbackend.frameworks.sfdr.model.general.company.SfdrGeneralCompanyCompanyExchangeStatusOptions
import org.dataland.datalandbackend.frameworks.sfdr.model.general.company.SfdrGeneralCompanyMainPcafSectorOptions
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

/**
 * Verifies that SFDR and PCAF share the data point types of the main PCAF sector and the company exchange status, as
 * PCAF datasets cannot be uploaded and receive these values from SFDR uploads only.
 */
class SfdrPcafSharedEnumDataPointTypesTest {
    private val specificationsPath = "../dataland-specification-service/src/main/resources/specifications"
    private val sharedFields =
        mapOf(
            "mainPcafSector" to "extendedEnumPcafMainSector",
            "companyExchangeStatus" to "extendedEnumCompanyExchangeStatus",
        )
    private val removedSfdrTypes = listOf("extendedEnumSfdrMainPcafSector", "extendedEnumSfdrCompanyExchangeStatus")

    private fun readSpecification(relativePath: String): JsonNode = defaultObjectMapper.readTree(File("$specificationsPath/$relativePath"))

    private fun companySchema(framework: String): JsonNode = readSpecification("frameworks/$framework.json")["schema"]["general"]["company"]

    @ParameterizedTest
    @ValueSource(strings = ["mainPcafSector", "companyExchangeStatus"])
    fun `check that SFDR and PCAF map the field to the same PCAF data point type`(fieldName: String) {
        val expectedType = sharedFields.getValue(fieldName)
        assertEquals(expectedType, companySchema("sfdr")[fieldName].asText())
        assertEquals(expectedType, companySchema("pcaf")[fieldName].asText())
    }

    @ParameterizedTest
    @ValueSource(strings = ["extendedEnumPcafMainSector", "extendedEnumCompanyExchangeStatus"])
    fun `check that the shared data point type is owned by SFDR and PCAF`(dataPointType: String) {
        val specification = readSpecification("dataPointTypes/$dataPointType.json")
        val owners = specification["frameworkOwnership"].map { it.asText() }.toSet()
        assertEquals(setOf("sfdr", "pcaf"), owners)
        assertEquals(dataPointType, specification["dataPointBaseTypeId"].asText())
    }

    @ParameterizedTest
    @ValueSource(strings = ["extendedEnumPcafMainSector", "extendedEnumCompanyExchangeStatus"])
    fun `check that the validator of the shared data point base type exists`(dataPointType: String) {
        val validatorClassName = readSpecification("dataPointBaseTypes/$dataPointType.json")["validatedBy"].asText()
        Class.forName(validatorClassName)
    }

    @Test
    fun `check that the SFDR specific enum data point types are removed`() {
        removedSfdrTypes.forEach { removedType ->
            assertFalse(File("$specificationsPath/dataPointTypes/$removedType.json").exists())
            assertFalse(File("$specificationsPath/dataPointBaseTypes/$removedType.json").exists())
        }
        listOf("sfdr", "pcaf").forEach { framework ->
            val frameworkSpecification = File("$specificationsPath/frameworks/$framework.json").readText()
            removedSfdrTypes.forEach { assertFalse(frameworkSpecification.contains(it)) }
        }
    }

    @Test
    fun `check that the SFDR and PCAF options of the shared fields are identical`() {
        assertEquals(
            PcafGeneralCompanyMainPcafSectorOptions.entries.map { it.name },
            SfdrGeneralCompanyMainPcafSectorOptions.entries.map { it.name },
        )
        assertEquals(
            PcafGeneralCompanyCompanyExchangeStatusOptions.entries.map { it.name },
            SfdrGeneralCompanyCompanyExchangeStatusOptions.entries.map { it.name },
        )
        assertTrue(SfdrGeneralCompanyMainPcafSectorOptions.entries.isNotEmpty())
    }
}
