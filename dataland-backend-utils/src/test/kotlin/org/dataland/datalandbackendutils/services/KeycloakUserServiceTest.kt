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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

class KeycloakUserServiceTest {
    private val objectMapper = ObjectMapper()
    private val mockOkHttpClient = mock<OkHttpClient>()
    private val mockCall = mock<Call>()
    private lateinit var keycloakUserService: KeycloakUserService

    private val keycloakBaseUrl = "http://fakeurl.com"

    @BeforeEach
    fun setUp() {
        reset(mockOkHttpClient, mockCall)

        keycloakUserService = KeycloakUserService(objectMapper, mockOkHttpClient, keycloakBaseUrl)
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

    private fun stubHttpCall(
        expectedUrl: String,
        code: Int,
        message: String,
        jsonString: String,
    ) {
        val response =
            Response
                .Builder()
                .request(Request.Builder().url(expectedUrl).build())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(message)
                .body(jsonString.toResponseBody(applicationJsonString.toMediaTypeOrNull()))
                .build()
        doReturn(response).whenever(mockCall).execute()
        doReturn(mockCall).whenever(mockOkHttpClient).newCall(argThat { this.url.toString() == expectedUrl })
    }

    @Test
    fun `getUser should return valid KeycloakUserInfo on successful parse`() {
        val expectedUrl = "$keycloakBaseUrl/admin/realms/datalandsecurity/users/${firstUser.userId}"

        stubHttpCall(
            expectedUrl = expectedUrl,
            code = 200,
            message = "OK",
            jsonString = firstUserJson,
        )

        val result = keycloakUserService.getUser(firstUser.userId)

        assertEquals(firstUser, result)
    }

    @Test
    fun `getUser should return empty KeycloakUserInfo on unsuccessful parse`() {
        val nonExistentUserId = "nonExistentUserId"
        val expectedUrl = "$keycloakBaseUrl/admin/realms/datalandsecurity/users/$nonExistentUserId"
        val emptyKeycloakUserInfo =
            KeycloakUserInfo(email = null, userId = nonExistentUserId, firstName = null, lastName = null)

        stubHttpCall(
            expectedUrl = expectedUrl,
            code = 404,
            message = "User not found",
            jsonString = "non-existent-user",
        )

        val result = keycloakUserService.getUser(nonExistentUserId)

        assertEquals(emptyKeycloakUserInfo, result)
    }

    @Test
    fun `isKeycloakUser should return valid true on successful parse`() {
        val expectedUrl = "$keycloakBaseUrl/admin/realms/datalandsecurity/users/${firstUser.userId}"

        stubHttpCall(
            expectedUrl = expectedUrl,
            code = 200,
            message = "OK",
            jsonString = firstUserJson,
        )

        assertTrue(keycloakUserService.isKeycloakUserId(firstUser.userId))
    }

    @Test
    fun `searchUsers should return a list of KeycloakUserInfo on successful parse`() {
        val emailSearch = "test"
        val expectedUrl = "$keycloakBaseUrl/admin/realms/datalandsecurity/users?email=$emailSearch"
        val json = "[$firstUserJson, $secondUserJson]"

        stubHttpCall(
            expectedUrl = expectedUrl,
            code = 200,
            message = "OK",
            jsonString = json,
        )

        val result = keycloakUserService.searchUsers(emailSearch)

        assertTrue(result.contains(firstUser))
        assertTrue(result.contains(secondUser))
    }

    @Test
    fun `getUsersByRole should return a list of KeycloakUserInfo on successful parse`() {
        val role = "ROLE_PREMIUM_USER"
        val expectedUrl = "$keycloakBaseUrl/admin/realms/datalandsecurity/roles/$role/users/"
        val json = "[$firstUserJson, $secondUserJson]"

        stubHttpCall(
            expectedUrl = expectedUrl,
            code = 200,
            message = "OK",
            jsonString = json,
        )

        val result = keycloakUserService.getUsersByRole(role)

        assertTrue(result.contains(firstUser))
        assertTrue(result.contains(secondUser))
    }

    @Test
    fun `findUserByEmail should return a KeycloakUserInfo object on successful parse`() {
        val emailWithExactMatch = firstUser.email
        val expectedUrl = "$keycloakBaseUrl/admin/realms/datalandsecurity/users?email=$emailWithExactMatch&exact=true"
        val json = "[$firstUserJson]"

        stubHttpCall(
            expectedUrl = expectedUrl,
            code = 200,
            message = "OK",
            jsonString = json,
        )

        val result = keycloakUserService.findUserByEmail(emailWithExactMatch!!)

        assertEquals(firstUser, result)
    }
}
