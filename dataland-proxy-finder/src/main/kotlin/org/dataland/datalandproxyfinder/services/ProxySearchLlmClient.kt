package org.dataland.datalandproxyfinder.services

import com.fasterxml.jackson.core.JsonProcessingException
import com.openai.client.OpenAIClient
import com.openai.core.JsonValue
import com.openai.models.Reasoning
import com.openai.models.ReasoningEffort
import com.openai.models.responses.Response
import com.openai.models.responses.ResponseCreateParams
import com.openai.models.responses.ResponseFormatTextJsonSchemaConfig
import com.openai.models.responses.ResponseFunctionToolCall
import com.openai.models.responses.ResponseInputItem
import com.openai.models.responses.ResponseStatus
import com.openai.models.responses.ResponseTextConfig
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandproxyfinder.model.ProxySearchLlmResult
import org.dataland.datalandproxyfinder.model.ProxySearchStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import kotlin.jvm.optionals.getOrNull

/**
 * Thrown if the LLM did not produce a usable result.
 */
class ProxySearchLlmException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * Runs the proxy research against the Responses API of the Azure OpenAI deployment.
 *
 * The LLM may call the Dataland company tools. Function calls are executed locally and their outputs are sent back
 * (chained via previous_response_id) until the LLM returns its final answer, which is enforced to match the
 * JSON schema of [ProxySearchLlmResult] (structured output).
 */
@Component
@ConditionalOnProperty(name = ["dataland.proxy-finder.provider"], havingValue = "azure")
class ProxySearchLlmClient(
    private val openAiClient: OpenAIClient,
    private val datalandCompanyTools: DatalandCompanyTools,
    @Value("\${dataland.proxy-finder.azure.deployment-name}") private val deploymentName: String,
    @Value("\${dataland.proxy-finder.azure.reasoning-effort}") private val reasoningEffort: String,
    @Value("\${dataland.proxy-finder.azure.max-tool-rounds}") private val maxToolRounds: Int,
    @Value("classpath:prompts/proxy-search.st") systemPrompt: Resource,
) {
    companion object {
        const val RESULT_SCHEMA_NAME = "proxy_search_result"
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val instructions = systemPrompt.getContentAsString(StandardCharsets.UTF_8)

    /**
     * Asks the LLM for the proxy company of the given company.
     * @param companyName the name of the company for which a proxy company is searched
     * @return the structured result of the research
     * @throws ProxySearchLlmException if the LLM did not produce a usable result
     */
    fun research(companyName: String): ProxySearchLlmResult {
        val userMessage = "Find the proxy company for the company \"$companyName\"."
        var response = openAiClient.responses().create(baseParams().input(userMessage).build())
        repeat(maxToolRounds) {
            val functionCalls = response.output().mapNotNull { it.functionCall().getOrNull() }
            if (functionCalls.isEmpty()) return parseResult(response)
            response = openAiClient.responses().create(followUpParams(response.id(), functionCalls))
        }
        throw ProxySearchLlmException("The LLM did not finish within $maxToolRounds tool-calling rounds.")
    }

    private fun followUpParams(
        previousResponseId: String,
        functionCalls: List<ResponseFunctionToolCall>,
    ): ResponseCreateParams {
        val functionCallOutputs =
            functionCalls.map {
                ResponseInputItem.ofFunctionCallOutput(
                    ResponseInputItem.FunctionCallOutput
                        .builder()
                        .callId(it.callId())
                        .output(datalandCompanyTools.call(it.name(), it.arguments()))
                        .build(),
                )
            }
        return baseParams()
            .previousResponseId(previousResponseId)
            .inputOfResponse(functionCallOutputs)
            .build()
    }

    /**
     * Instructions, tools, output format and reasoning settings are not carried over via previous_response_id,
     * so they are sent with every request.
     */
    private fun baseParams(): ResponseCreateParams.Builder {
        val builder =
            ResponseCreateParams
                .builder()
                .model(deploymentName)
                .instructions(instructions)
                .reasoning(Reasoning.builder().effort(ReasoningEffort.of(reasoningEffort)).build())
                .text(ResponseTextConfig.builder().format(resultFormat()).build())
        datalandCompanyTools.definitions.forEach { builder.addTool(it) }
        return builder
    }

    private fun parseResult(response: Response): ProxySearchLlmResult {
        checkCompleted(response)
        val answer = extractAnswer(response)
        return try {
            defaultObjectMapper.readValue(answer, ProxySearchLlmResult::class.java).also {
                logger.info("LLM research finished with status ${it.status} (response ${response.id()}).")
            }
        } catch (exception: JsonProcessingException) {
            throw ProxySearchLlmException("The LLM answer is not a valid result object.", exception)
        }
    }

    private fun checkCompleted(response: Response) {
        val status = response.status().getOrNull()
        if (status != null && status != ResponseStatus.COMPLETED) {
            val details =
                response
                    .incompleteDetails()
                    .getOrNull()
                    ?.reason()
                    ?.getOrNull()
            throw ProxySearchLlmException("The LLM response ${response.id()} has status $status (details: $details).")
        }
    }

    private fun extractAnswer(response: Response): String {
        val contents = response.output().mapNotNull { it.message().getOrNull() }.flatMap { it.content() }
        contents.firstNotNullOfOrNull { it.refusal().getOrNull() }?.let {
            throw ProxySearchLlmException("The LLM refused to answer: ${it.refusal()}")
        }
        val answer = contents.mapNotNull { it.outputText().getOrNull()?.text() }.joinToString("")
        if (answer.isBlank()) throw ProxySearchLlmException("The LLM response ${response.id()} contains no answer.")
        return answer
    }

    private fun resultFormat(): ResponseFormatTextJsonSchemaConfig {
        val nullableString = mapOf("type" to listOf("string", "null"))
        val source =
            strictObject(
                mapOf(
                    "title" to mapOf("type" to "string"),
                    "url" to mapOf("type" to "string"),
                    "type" to
                        mapOf(
                            "type" to listOf("string", "null"),
                            "description" to "official_filing, company_website, register or third_party",
                        ),
                ),
            )
        val result =
            strictObject(
                mapOf(
                    "status" to mapOf("type" to "string", "enum" to ProxySearchStatus.entries.map { it.name }),
                    "parentName" to nullableString,
                    "parentCompanyId" to nullableString,
                    "confidence" to
                        mapOf("type" to listOf("number", "null"), "description" to "Confidence between 0 and 1."),
                    "reasoning" to nullableString,
                    "sources" to mapOf("type" to "array", "items" to source),
                    "candidates" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                ),
            )
        return ResponseFormatTextJsonSchemaConfig
            .builder()
            .name(RESULT_SCHEMA_NAME)
            .strict(true)
            .schema(
                ResponseFormatTextJsonSchemaConfig.Schema
                    .builder()
                    .putAllAdditionalProperties(result.mapValues { JsonValue.from(it.value) })
                    .build(),
            ).build()
    }

    /**
     * Strict structured outputs require all properties to be required and no additional properties.
     */
    private fun strictObject(properties: Map<String, Any>): Map<String, Any> =
        mapOf(
            "type" to "object",
            "properties" to properties,
            "required" to properties.keys.toList(),
            "additionalProperties" to false,
        )
}
