package org.dataland.datalandproxyfinder.services

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.openai.core.JsonValue
import com.openai.models.responses.FunctionTool
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandproxyfinder.model.DatalandCompanyCandidate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Tool functions that the LLM can call to resolve companies to Dataland company IDs. Provides the function tool
 * definitions for the Responses API and executes the function calls requested by the LLM.
 */
@Component
class DatalandCompanyTools(
    private val datalandCompanyService: DatalandCompanyService,
) {
    companion object {
        const val MAX_SEARCH_RESULTS = 10
        const val SEARCH_COMPANIES_BY_NAME = "searchCompaniesByName"
        const val FIND_COMPANY_BY_IDENTIFIER = "findCompanyByIdentifier"
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * The function tool definitions (strict JSON schemas) that are passed to the LLM.
     */
    val definitions: List<FunctionTool> =
        listOf(
            functionTool(
                name = SEARCH_COMPANIES_BY_NAME,
                description = "Searches companies on Dataland by name and returns matching Dataland company IDs and names.",
                properties =
                    mapOf(
                        "name" to
                            mapOf(
                                "type" to "string",
                                "description" to "The company name or a part of it (at least 3 characters).",
                            ),
                    ),
            ),
            functionTool(
                name = FIND_COMPANY_BY_IDENTIFIER,
                description = "Looks up a company on Dataland by an identifier and returns it, or null if it is unknown.",
                properties =
                    mapOf(
                        "identifierType" to
                            mapOf(
                                "type" to "string",
                                "enum" to IdentifierType.entries.map { it.name },
                                "description" to "The identifier type, e.g. Lei.",
                            ),
                        "identifier" to mapOf("type" to "string", "description" to "The identifier value."),
                    ),
            ),
        )

    /**
     * Searches Dataland companies by (part of) their name.
     */
    fun searchCompaniesByName(name: String): List<DatalandCompanyCandidate> =
        datalandCompanyService.searchCompaniesByName(name, MAX_SEARCH_RESULTS)

    /**
     * Looks up a Dataland company by an identifier.
     */
    fun findCompanyByIdentifier(
        identifierType: String,
        identifier: String,
    ): DatalandCompanyCandidate? = datalandCompanyService.findCompanyByIdentifier(identifierType, identifier)

    /**
     * Executes a function call requested by the LLM.
     * Invalid calls (unknown function, malformed arguments) are reported back to the LLM as an error object,
     * so that it can correct itself. Failures of the Dataland backend are propagated.
     * @param name the name of the called function
     * @param arguments the arguments of the call as a JSON object string
     * @return the result of the call as a JSON string
     */
    fun call(
        name: String,
        arguments: String,
    ): String {
        logger.info("LLM called tool $name with arguments $arguments.")
        val result =
            try {
                val argumentNode = defaultObjectMapper.readTree(arguments)
                when (name) {
                    SEARCH_COMPANIES_BY_NAME ->
                        mapOf("companies" to searchCompaniesByName(argumentNode.requiredText("name")))
                    FIND_COMPANY_BY_IDENTIFIER ->
                        mapOf(
                            "company" to
                                findCompanyByIdentifier(
                                    argumentNode.requiredText("identifierType"),
                                    argumentNode.requiredText("identifier"),
                                ),
                        )
                    else -> mapOf("error" to "Unknown function $name.")
                }
            } catch (exception: JsonProcessingException) {
                mapOf("error" to "The arguments are not valid JSON: ${exception.originalMessage}")
            } catch (exception: IllegalArgumentException) {
                mapOf("error" to exception.message)
            }
        return defaultObjectMapper.writeValueAsString(result)
    }

    private fun JsonNode.requiredText(field: String): String =
        get(field)?.takeIf { it.isTextual }?.textValue()
            ?: throw IllegalArgumentException("The string argument '$field' is missing.")

    private fun functionTool(
        name: String,
        description: String,
        properties: Map<String, Any>,
    ): FunctionTool =
        FunctionTool
            .builder()
            .name(name)
            .description(description)
            .strict(true)
            .parameters(
                FunctionTool.Parameters
                    .builder()
                    .putAdditionalProperty("type", JsonValue.from("object"))
                    .putAdditionalProperty("properties", JsonValue.from(properties))
                    .putAdditionalProperty("required", JsonValue.from(properties.keys.toList()))
                    .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                    .build(),
            ).build()
}
