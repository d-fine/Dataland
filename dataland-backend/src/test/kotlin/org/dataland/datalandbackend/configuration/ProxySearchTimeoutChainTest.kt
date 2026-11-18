package org.dataland.datalandbackend.configuration

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * The AI-based proxy search involves three timeouts that depend on each other. They must satisfy
 * max-research-duration of the proxy finder < read timeout of the backend < proxy_read_timeout of the inbound proxy,
 * so that the 503 of the proxy finder arrives instead of a cut connection.
 */
class ProxySearchTimeoutChainTest {
    private fun readFile(path: String): String = File(path).readText()

    private fun findSeconds(
        content: String,
        regex: Regex,
        description: String,
    ): Long =
        requireNotNull(
            regex
                .find(content)
                ?.groupValues
                ?.get(1)
                ?.toLong(),
        ) { "Could not find $description" }

    @Test
    fun `the timeouts of the proxy search are ordered from inner to outer`() {
        val researchDurationSeconds =
            findSeconds(
                readFile("../dataland-company-service/src/main/resources/provider-azure.properties"),
                Regex("""^dataland\.company-service\.azure\.max-research-duration=(\d+)s""", RegexOption.MULTILINE),
                "max-research-duration of the proxy finder",
            )
        val backendReadTimeoutSeconds =
            findSeconds(
                readFile("src/main/resources/application.properties"),
                Regex("""^dataland\.company-service\.read-timeout-seconds=(\d+)""", RegexOption.MULTILINE),
                "read timeout for the proxy finder in the backend",
            )
        val inboundProxyTimeoutSeconds =
            findSeconds(
                readFile("../dataland-inbound-proxy/templates/common.template"),
                Regex("""location /api/company-proxies/ \{[\s\S]*?proxy_read_timeout (\d+)s;"""),
                "proxy_read_timeout of /api/company-proxies/ in the inbound proxy",
            )
        assertTrue(researchDurationSeconds < backendReadTimeoutSeconds, "research duration must be < backend timeout")
        assertTrue(backendReadTimeoutSeconds < inboundProxyTimeoutSeconds, "backend timeout must be < inbound proxy timeout")
    }
}
