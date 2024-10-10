package org.dataland.datalandbackendutils.services

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [ObjectMapper::class, OkHttpClient::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeycloakTokenManagerTest {
    @Mock lateinit var httpClient: OkHttpClient

    @Autowired lateinit var objectMapper: ObjectMapper

    lateinit var keycloakTokenManager: KeycloakTokenManager

    @BeforeAll
    fun setup() {
        keycloakTokenManager = KeycloakTokenManager(objectMapper, httpClient, "http://keycloak", "client", "secret")
    }

    @Test
    fun `check that the requisition of a new token works`() {
        val expectedToken = "expectedToken"
        val mockCall = mock(Call::class.java)
        `when`(mockCall.execute()).thenReturn(
            Response
                .Builder()
                .request(Request.Builder().url("http://into.void").build())
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .code(200)
                .body(
                    "{ \"access_token\":\"${expectedToken}\", \"expires_in\": 0 }".toResponseBody(),
                ).build(),
        )
        `when`(httpClient.newCall(any() ?: Request.Builder().url("http://into.void").build())).thenReturn(mockCall)
        val token = keycloakTokenManager.getAccessToken()
        Assertions.assertEquals(expectedToken, token)
    }
}
