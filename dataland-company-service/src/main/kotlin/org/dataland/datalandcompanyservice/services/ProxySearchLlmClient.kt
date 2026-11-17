package org.dataland.datalandcompanyservice.services

import com.fasterxml.jackson.core.JsonProcessingException
import com.openai.client.OpenAIClient
import com.openai.core.JsonValue
import com.openai.core.RequestOptions
import com.openai.models.Reasoning
import com.openai.models.ReasoningEffort
import com.openai.models.responses.Response
import com.openai.models.responses.ResponseCreateParams
import com.openai.models.responses.ResponseFormatTextJsonSchemaConfig
import com.openai.models.responses.ResponseFunctionToolCall
import com.openai.models.responses.ResponseInputItem
import com.openai.models.responses.ResponseStatus
import com.openai.models.responses.ResponseTextConfig
import com.openai.models.responses.WebSearchPreviewTool
import com.openai.models.responses.WebSearchTool
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandcompanyservice.model.ProxySearchCompany
import org.dataland.datalandcompanyservice.model.ProxySearchLlmResult
import org.dataland.datalandcompanyservice.model.ProxySearchStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Duration
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
 * The LLM receives the data Dataland stores about the company (see [ProxySearchUserMessage]) and may use the web
 * search tool of the Responses API (executed by Azure) and the Dataland company tools. Function calls are executed
 * locally and their outputs are sent back (chained via previous_response_id) until the LLM returns its final answer,
 * which is enforced to match the JSON schema of [ProxySearchLlmResult] (structured output).
 */
@Component
@ConditionalOnProperty(name = ["dataland.company-service.provider"], havingValue = "azure")
class ProxySearchLlmClient(
    private val openAiClient: OpenAIClient,
    private val datalandCompanyTools: DatalandCompanyTools,
    @Value("\${dataland.company-service.azure.deployment-name}") private val deploymentName: String,
    @Value("\${dataland.company-service.azure.reasoning-effort}") private val reasoningEffort: String,
    @Value("\${dataland.company-service.azure.max-tool-rounds}") private val maxToolRounds: Int,
    @Value("\${dataland.company-service.azure.max-research-duration}") private val maxResearchDuration: Duration,
    @Value("\${dataland.company-service.azure.web-search-tool-type}") private val webSearchToolType: String,
    @Value("classpath:prompts/proxy-search.st") systemPrompt: Resource,
) {
    companion object {
        const val RESULT_SCHEMA_NAME = "proxy_search_result"
        const val WEB_SEARCH_TOOL_TYPE_NONE = "none"
        const val WEB_SEARCH_PREVIEW_TOOL_TYPE = "web_search_preview"
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val instructions = systemPrompt.getContentAsString(StandardCharsets.UTF_8)

    /**
     * Asks the LLM for the proxy company of the given company.
     * @param company the company for which a proxy company is searched, including its Dataland data
     * @return the structured result of the research
     * @throws ProxySearchLlmException if the LLM did not produce a usable result within the limits
     */
    fun research(company: ProxySearchCompany): ProxySearchLlmResult {
        val deadline = System.nanoTime() + maxResearchDuration.toNanos()
        var response = create(baseParams().input(ProxySearchUserMessage.build(company)).build(), deadline)
        repeat(maxToolRounds) {
            val functionCalls = response.output().mapNotNull { it.functionCall().getOrNull() }
            if (functionCalls.isEmpty()) return parseResult(response)
            response = create(followUpParams(response.id(), functionCalls), deadline)
        }
        throw ProxySearchLlmException("The LLM did not finish within $maxToolRounds tool-calling rounds.")
    }

    /**
     * Sends a request to the Responses API. The request may only use the time that is left of the overall research
     * duration, so that the proxy finder answers before its callers time out.
     */
    private fun create(
        params: ResponseCreateParams,
        deadline: Long,
    ): Response {
        val remaining = Duration.ofNanos(deadline - System.nanoTime())
        if (remaining.isNegative || remaining.isZero) {
            throw ProxySearchLlmException("The proxy research exceeded the maximum duration of $maxResearchDuration.")
        }
        return openAiClient.responses().create(params, RequestOptions.builder().timeout(remaining).build())
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
        addWebSearchTool(builder)
        datalandCompanyTools.definitions.forEach { builder.addTool(it) }
        return builder
    }

    /**
     * Adds the hosted web search tool. Its type is configurable, because the supported type depends on the
     * Azure OpenAI deployment ("web_search" or "web_search_preview"); "none" disables web search.
     */
    private fun addWebSearchTool(builder: ResponseCreateParams.Builder) {
        when (val type = webSearchToolType.trim()) {
            "", WEB_SEARCH_TOOL_TYPE_NONE -> Unit
            WEB_SEARCH_PREVIEW_TOOL_TYPE ->
                builder.addTool(WebSearchPreviewTool.builder().type(WebSearchPreviewTool.Type.WEB_SEARCH_PREVIEW).build())
            else -> builder.addTool(WebSearchTool.builder().type(WebSearchTool.Type.of(type)).build())
        }
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
