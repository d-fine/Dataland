package org.dataland.datalandbackendutils.services.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class JsonUtilsTest {
    private val inputJson = File("./src/test/resources/json/input.json")
    private val expectedJsonPaths =
        listOf(
            "companyId",
            "reportingPeriod",
            "data.general.general.fiscalYearDeviation.value",
            "data.general.general.fiscalYearDeviation.dataSource.page",
            "data.general.general.fiscalYearDeviation.dataSource.fileReference",
            "data.general.general.fiscalYearEnd",
            "data.general.general.arrayField.0",
        )
    private val expectedJsonPathsWithoutArray = expectedJsonPaths.dropLast(1)

    @Test
    fun `check that the retrieved JSON paths are as expected`() {
        val jsonNode = ObjectMapper().readTree(inputJson)
        val result = JsonUtils.getLeafNodeFieldNames(jsonNode)
        assertEquals(expectedJsonPaths, result)
    }

    @Test
    fun `check that arrays are ignored if ignoreArray is set to true`() {
        val jsonNode = ObjectMapper().readTree(inputJson)
        val result = JsonUtils.getNonArrayLeafNodeFieldNames(jsonNode)
        assertEquals(expectedJsonPathsWithoutArray, result)
    }

    @Test
    fun `check that null valued fields are extracted as empty strings`() {
        val jsonNode = ObjectMapper().readTree("{\"nullValued\": null}")
        assertEquals("", JsonUtils.getValueFromJsonNodeByPath(jsonNode, "nullValued"))
    }
}
