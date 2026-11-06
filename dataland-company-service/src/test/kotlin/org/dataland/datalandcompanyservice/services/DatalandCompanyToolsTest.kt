package org.dataland.datalandcompanyservice.services

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandcompanyservice.model.DatalandCompanyCandidate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class DatalandCompanyToolsTest {
    private val service = mock<DatalandCompanyService>()
    private val tools = DatalandCompanyTools(service)
    private val candidate = DatalandCompanyCandidate("id-1", "Parent AG")

    @Test
    fun `search by name delegates with the result limit`() {
        whenever(service.searchCompaniesByName("Parent", DatalandCompanyTools.MAX_SEARCH_RESULTS))
            .thenReturn(listOf(candidate))
        assertEquals(listOf(candidate), tools.searchCompaniesByName("Parent"))
        verify(service).searchCompaniesByName("Parent", DatalandCompanyTools.MAX_SEARCH_RESULTS)
    }

    @Test
    fun `identifier lookup returns the company or null`() {
        whenever(service.findCompanyByIdentifier("Lei", "123")).thenReturn(candidate)
        assertEquals(candidate, tools.findCompanyByIdentifier("Lei", "123"))
        assertNull(tools.findCompanyByIdentifier("Lei", "unknown"))
    }

    @Test
    fun `the tool definitions are strict and require all parameters`() {
        val definitions = tools.definitions.associateBy { it.name() }
        assertEquals(
            setOf(DatalandCompanyTools.SEARCH_COMPANIES_BY_NAME, DatalandCompanyTools.FIND_COMPANY_BY_IDENTIFIER),
            definitions.keys,
        )
        definitions.values.forEach { tool ->
            assertTrue(tool.strict().get())
            val parameters = defaultObjectMapper.valueToTree<JsonNode>(tool.parameters().get())
            assertEquals(false, parameters["additionalProperties"].booleanValue())
            assertEquals(
                parameters["properties"].fieldNames().asSequence().toSet(),
                parameters["required"].map { it.textValue() }.toSet(),
            )
        }
    }

    @Test
    fun `function calls are executed and their results returned as json`() {
        whenever(service.searchCompaniesByName("Parent", DatalandCompanyTools.MAX_SEARCH_RESULTS))
            .thenReturn(listOf(candidate))
        whenever(service.findCompanyByIdentifier("Lei", "123")).thenReturn(candidate)

        val searchResult = defaultObjectMapper.readTree(tools.call("searchCompaniesByName", """{"name":"Parent"}"""))
        assertEquals("id-1", searchResult["companies"][0]["companyId"].textValue())

        val lookupResult =
            defaultObjectMapper.readTree(
                tools.call("findCompanyByIdentifier", """{"identifierType":"Lei","identifier":"123"}"""),
            )
        assertEquals("Parent AG", lookupResult["company"]["companyName"].textValue())

        val unknownResult =
            defaultObjectMapper.readTree(
                tools.call("findCompanyByIdentifier", """{"identifierType":"Lei","identifier":"unknown"}"""),
            )
        assertTrue(unknownResult["company"].isNull)
    }

    @Test
    fun `invalid function calls are reported back as errors`() {
        listOf(
            "unknownFunction" to """{"name":"Parent"}""",
            "searchCompaniesByName" to """{"wrong":"Parent"}""",
            "searchCompaniesByName" to "not json",
        ).forEach { (name, arguments) ->
            assertTrue(defaultObjectMapper.readTree(tools.call(name, arguments)).has("error"))
        }
        verifyNoInteractions(service)
    }
}
