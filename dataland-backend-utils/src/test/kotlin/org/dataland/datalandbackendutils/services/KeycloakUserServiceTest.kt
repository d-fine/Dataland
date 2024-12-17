package org.dataland.datalandbackendutils.services

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class KeycloakUserServiceTest {
    private lateinit var objectMapper: ObjectMapper
    private lateinit var authenticatedOkHttpClient: OkHttpClient
    private lateinit var service: KeycloakUserService

    private val keycloakBaseUrl = "http://fakeurl.com"

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        objectMapper = ObjectMapper()
        authenticatedOkHttpClient = mock<OkHttpClient>()
        service = KeycloakUserService(objectMapper, authenticatedOkHttpClient, keycloakBaseUrl)
    }

    private val firstUser = KeycloakUserInfo("test@example.com", "1", "John", "Doe")
    private val firstUserJson =
        """
        {
            "id": "${firstUser.userId}",
            "email": "${firstUser.email}",
            "firstName": "${firstUser.firstName}",
            "lastName": "${firstUser.lastName}"
        }
        """.trimIndent()

    private val secondUser = KeycloakUserInfo("example@test.com", "2", "Jane", "Doe")

    private val secondUserJson =
        """
        {
            "id": "${secondUser.userId}",
            "email": "${secondUser.email}",
            "firstName": "${secondUser.firstName}",
            "lastName": "${secondUser.lastName}"
        }
        """.trimIndent()

    private val applicationJsonString = "application/json"

    @Test
    fun `getUser should return valid KeycloakUserInfo on successful parse`() {
        val expectedUrl = "$keycloakBaseUrl/admin/realms/datalandsecurity/users/${firstUser.userId}"

        val response =
            Response
                .Builder()
                .request(Request.Builder().url(expectedUrl).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(firstUserJson.toResponseBody(applicationJsonString.toMediaTypeOrNull()))
                .build()

        val call = mock<Call>()
        whenever(call.execute()).thenReturn(response)
        whenever(authenticatedOkHttpClient.newCall(argThat { this.url.toString() == expectedUrl })).thenReturn(call)

        val result = service.getUser(firstUser.userId)
        assertEquals(firstUser, result)
    }

    @Test
    fun `searchUsers should return a list of KeycloakUserInfo on successful parse`() {
        val emailSearch = "test"
        val expectedUrl = "$keycloakBaseUrl/admin/realms/datalandsecurity/users?email=$emailSearch"

        val json = "[$firstUserJson, $secondUserJson]"

        val response =
            Response
                .Builder()
                .request(Request.Builder().url(expectedUrl).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(json.toResponseBody(applicationJsonString.toMediaTypeOrNull()))
                .build()

        val call = mock<Call>()
        whenever(call.execute()).thenReturn(response)
        whenever(authenticatedOkHttpClient.newCall(argThat { this.url.toString() == expectedUrl })).thenReturn(call)

        val result = service.searchUsers(emailSearch)

        assertTrue(result.contains(firstUser))
        assertTrue(result.contains(secondUser))
    }

    @Test
    fun `getUsersByRole should return a list of KeycloakUserInfo on succesful parse`() {
        val role = "ROLE_PREMIUM_USER"
        val expectedUrl = "$keycloakBaseUrl/admin/realms/datalandsecurity/users/roles/$role/users/"

        val json = "[$firstUserJson, $secondUserJson]"

        val response =
            Response
                .Builder()
                .request(Request.Builder().url(expectedUrl).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(json.toResponseBody(applicationJsonString.toMediaTypeOrNull()))
                .build()

        val call = mock<Call>()
        whenever(call.execute()).thenReturn(response)
        whenever(authenticatedOkHttpClient.newCall(argThat { this.url.toString() == expectedUrl })).thenReturn(call)

        val result = service.getUsersByRole(role)

        assertTrue(result.contains(firstUser))
        assertTrue(result.contains(secondUser))
    }
}
