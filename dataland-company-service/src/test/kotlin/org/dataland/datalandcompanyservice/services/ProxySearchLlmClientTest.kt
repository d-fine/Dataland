package org.dataland.datalandcompanyservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.openai.client.OpenAIClient
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandcompanyservice.configurations.AzureOpenAiClientConfiguration
import org.dataland.datalandcompanyservice.model.DatalandCompanyCandidate
import org.dataland.datalandcompanyservice.model.ProxySearchCompany
import org.dataland.datalandcompanyservice.model.ProxySearchStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.io.ByteArrayResource
import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue

class ProxySearchLlmClientTest {
    private companion object {
        const val DEPLOYMENT = "test-deployment"
        const val API_KEY = "test-key"
        const val MAX_TOOL_ROUNDS = 2
        const val SYSTEM_PROMPT = "You are a test prompt."
        val MAX_RESEARCH_DURATION: Duration = Duration.ofSeconds(10)
        val COMPANY = ProxySearchCompany(companyId = "child-id", companyName = "Child GmbH")
    }

    private data class RecordedRequest(
        val path: String,
        val apiKey: String?,
        val body: JsonNode,
    )

    private val requests = ConcurrentLinkedQueue<RecordedRequest>()
    private val responses = ConcurrentLinkedQueue<String>()

    @Volatile
    private var responseDelay: Duration = Duration.ZERO
    private lateinit var server: HttpServer
    private lateinit var openAiClient: OpenAIClient
    private val companyService = mock<DatalandCompanyService>()
    private lateinit var llmClient: ProxySearchLlmClient

    @BeforeEach
    fun setup() {
        server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/") { exchange -> handle(exchange) }
        server.start()
        openAiClient =
            AzureOpenAiClientConfiguration().azureOpenAiClient(
                endpoint = "http://localhost:${server.address.port}/openai/v1/",
                apiKey = API_KEY,
                timeout = MAX_RESEARCH_DURATION,
                maxRetries = 0,
            )
        llmClient = createLlmClient(MAX_RESEARCH_DURATION)
    }

    private fun createLlmClient(
        maxResearchDuration: Duration,
        webSearchToolType: String = "web_search",
    ) = ProxySearchLlmClient(
        openAiClient = openAiClient,
        datalandCompanyTools = DatalandCompanyTools(companyService),
        deploymentName = DEPLOYMENT,
        reasoningEffort = "medium",
        maxToolRounds = MAX_TOOL_ROUNDS,
        maxResearchDuration = maxResearchDuration,
        webSearchToolType = webSearchToolType,
        systemPrompt = ByteArrayResource(SYSTEM_PROMPT.toByteArray()),
    )

    @AfterEach
    fun tearDown() {
        openAiClient.close()
        server.stop(0)
    }

    private fun handle(exchange: HttpExchange) {
        requests.add(
            RecordedRequest(
                path = exchange.requestURI.path,
                apiKey = exchange.requestHeaders.getFirst("api-key"),
                body = defaultObjectMapper.readTree(exchange.requestBody.readAllBytes()),
            ),
        )
        Thread.sleep(responseDelay.toMillis())
        val body = (responses.poll() ?: error("No response queued")).toByteArray()
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(200, body.size.toLong())
        exchange.responseBody.use { it.write(body) }
    }

    private fun response(
        id: String,
        output: List<Map<String, Any?>>,
        status: String = "completed",
    ): String =
        defaultObjectMapper.writeValueAsString(
            mapOf(
                "id" to id,
                "object" to "response",
                "created_at" to 0,
                "model" to DEPLOYMENT,
                "status" to status,
                "output" to output,
                "parallel_tool_calls" to true,
                "tool_choice" to "auto",
                "tools" to emptyList<Any>(),
                "error" to null,
                "incomplete_details" to if (status == "incomplete") mapOf("reason" to "max_output_tokens") else null,
                "instructions" to null,
                "metadata" to null,
                "temperature" to null,
                "top_p" to null,
            ),
        )

    private fun functionCall(
        callId: String,
        name: String,
        arguments: String,
    ) = mapOf(
        "type" to "function_call",
        "id" to "fc_$callId",
        "call_id" to callId,
        "name" to name,
        "arguments" to arguments,
        "status" to "completed",
    )

    private fun message(text: String) =
        mapOf(
            "type" to "message",
            "id" to "msg_1",
            "role" to "assistant",
            "status" to "completed",
            "content" to listOf(mapOf("type" to "output_text", "text" to text, "annotations" to emptyList<Any>())),
        )

    private val foundAnswer =
        defaultObjectMapper.writeValueAsString(
            mapOf(
                "status" to "FOUND",
                "parentName" to "Parent AG",
                "parentCompanyId" to "parent-id",
                "confidence" to 0.9,
                "reasoning" to "Annual report",
                "sources" to listOf(mapOf("title" to "Report", "url" to "https://example.com", "type" to null)),
                "candidates" to emptyList<String>(),
            ),
        )

    @Test
    fun `function calls are executed and the structured answer is parsed`() {
        whenever(companyService.searchCompaniesByName("Parent", DatalandCompanyTools.MAX_SEARCH_RESULTS))
            .thenReturn(listOf(DatalandCompanyCandidate("parent-id", "Parent AG")))
        responses.add(
            response(
                "resp_1",
                listOf(
                    mapOf("type" to "reasoning", "id" to "rs_1", "summary" to emptyList<Any>()),
                    functionCall("call_1", DatalandCompanyTools.SEARCH_COMPANIES_BY_NAME, """{"name":"Parent"}"""),
                ),
            ),
        )
        responses.add(response("resp_2", listOf(message(foundAnswer))))

        val result = llmClient.research(COMPANY)

        assertEquals(ProxySearchStatus.FOUND, result.status)
        assertEquals("parent-id", result.parentCompanyId)
        assertEquals(0.9, result.confidence)
        assertEquals("https://example.com", result.sources.single().url)

        val (first, second) = requests.toList()
        listOf(first, second).forEach {
            assertEquals("/openai/v1/responses", it.path)
            assertEquals(API_KEY, it.apiKey)
            assertEquals(DEPLOYMENT, it.body["model"].textValue())
            assertEquals(SYSTEM_PROMPT, it.body["instructions"].textValue())
            assertEquals("medium", it.body["reasoning"]["effort"].textValue())
            assertEquals(
                listOf("web_search", "function", "function"),
                it.body["tools"].map { tool -> tool["type"].textValue() },
            )
            assertEquals("json_schema", it.body["text"]["format"]["type"].textValue())
            assertTrue(it.body["text"]["format"]["strict"].booleanValue())
        }
        assertEquals(ProxySearchUserMessage.build(COMPANY), first.body["input"].textValue())
        assertEquals("resp_1", second.body["previous_response_id"].textValue())
        val functionCallOutput = second.body["input"].single()
        assertEquals("function_call_output", functionCallOutput["type"].textValue())
        assertEquals("call_1", functionCallOutput["call_id"].textValue())
        assertTrue(functionCallOutput["output"].textValue().contains("parent-id"))
    }

    @Test
    fun `the tool loop is aborted after the maximum number of rounds`() {
        repeat(MAX_TOOL_ROUNDS + 1) {
            responses.add(
                response(
                    "resp_$it",
                    listOf(functionCall("call_$it", DatalandCompanyTools.SEARCH_COMPANIES_BY_NAME, """{"name":"X"}""")),
                ),
            )
        }
        assertThrows<ProxySearchLlmException> { llmClient.research(COMPANY) }
        assertEquals(MAX_TOOL_ROUNDS + 1, requests.size)
    }

    @Test
    fun `the web search tool type is configurable and can be disabled`() {
        mapOf(
            "web_search_preview" to listOf("web_search_preview", "function", "function"),
            "none" to listOf("function", "function"),
        ).forEach { (type, expectedTools) ->
            requests.clear()
            responses.add(response("resp_$type", listOf(message(foundAnswer))))
            createLlmClient(MAX_RESEARCH_DURATION, type).research(COMPANY)
            assertEquals(expectedTools, requests.single().body["tools"].map { it["type"].textValue() })
        }
    }

    @Test
    fun `incomplete, empty and malformed answers are rejected`() {
        listOf(
            response("resp_incomplete", listOf(message(foundAnswer)), status = "incomplete"),
            response("resp_empty", emptyList()),
            response("resp_malformed", listOf(message("this is not json"))),
        ).forEach {
            responses.add(it)
            assertThrows<ProxySearchLlmException> { llmClient.research(COMPANY) }
        }
    }

    @Test
    fun `the research is aborted when the maximum duration is exceeded`() {
        responseDelay = Duration.ofSeconds(2)
        responses.add(
            response(
                "resp_slow",
                listOf(functionCall("call_1", DatalandCompanyTools.SEARCH_COMPANIES_BY_NAME, """{"name":"X"}""")),
            ),
        )
        val shortLlmClient = createLlmClient(Duration.ofMillis(500))
        val start = System.nanoTime()
        assertThrows<RuntimeException> { shortLlmClient.research(COMPANY) }
        assertTrue(Duration.ofNanos(System.nanoTime() - start) < Duration.ofSeconds(2))
    }
}
