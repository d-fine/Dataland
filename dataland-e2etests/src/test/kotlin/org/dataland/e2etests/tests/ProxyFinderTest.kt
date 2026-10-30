package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyProxyControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.PROXY_FINDER_MOCK_PROXY_COMPANY_LEI
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.util.UUID

/**
 * Tests the full path from the backend to the proxy finder and back.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProxyFinderTest {
    private val apiAccessor = ApiAccessor()
    private val companyProxyControllerApi = CompanyProxyControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private lateinit var mockProxyCompanyId: String

    @BeforeAll
    fun ensureMockProxyCompanyExists() {
        mockProxyCompanyId =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                try {
                    apiAccessor.companyDataControllerApi
                        .getCompanyIdByIdentifier(IdentifierType.Lei, PROXY_FINDER_MOCK_PROXY_COMPANY_LEI)
                        .companyId
                } catch (exception: ClientException) {
                    if (exception.statusCode != HttpStatus.NOT_FOUND.value()) throw exception
                    apiAccessor.uploadOneCompanyWithIdentifiers(lei = PROXY_FINDER_MOCK_PROXY_COMPANY_LEI)!!.actualStoredCompany.companyId
                }
            }
    }

    private fun uploadCompany(): String =
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        }

    private fun assertProxySuggestionFailsWith(
        expectedStatus: HttpStatus,
        companyId: String,
        technicalUser: TechnicalUser = TechnicalUser.Admin,
    ): ClientException {
        val exception =
            assertThrows<ClientException> {
                GlobalAuth.withTechnicalUser(technicalUser) { companyProxyControllerApi.findProxySuggestion(companyId) }
            }
        assertEquals(expectedStatus.value(), exception.statusCode)
        return exception
    }

    @Test
    fun `an admin receives the hardcoded proxy suggestion of the proxy finder through the backend`() {
        val companyId = uploadCompany()

        val suggestion =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                companyProxyControllerApi.findProxySuggestion(companyId)
            }

        assertEquals(companyId, suggestion.companyId)
        assertEquals(mockProxyCompanyId, suggestion.proxyCompanyId)
        assertNotEquals(companyId, suggestion.proxyCompanyId)
        assertTrue(suggestion.confidence in 0.0..1.0)
        assertTrue(suggestion.reasoning.isNotBlank())
        assertTrue(suggestion.sources.isNotEmpty())
        assertDoesNotThrow {
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                apiAccessor.companyDataControllerApi.isCompanyIdValid(suggestion.proxyCompanyId)
            }
        }
    }

    @Test
    fun `non admin users are forbidden to request proxy suggestions`() {
        val companyId = uploadCompany()
        listOf(TechnicalUser.Reader, TechnicalUser.Uploader, TechnicalUser.Reviewer).forEach {
            assertProxySuggestionFailsWith(HttpStatus.FORBIDDEN, companyId, it)
        }
    }

    @Test
    fun `requesting a proxy suggestion for an unknown company results in a 404`() {
        assertProxySuggestionFailsWith(HttpStatus.NOT_FOUND, UUID.randomUUID().toString())
    }

    @Test
    fun `a suggestion of the company itself is rejected by the proxy finder validation with a 422`() {
        val exception = assertProxySuggestionFailsWith(HttpStatus.UNPROCESSABLE_CONTENT, mockProxyCompanyId)
        val responseBody = (exception.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("invalid-proxy-suggestion"))
    }
}
