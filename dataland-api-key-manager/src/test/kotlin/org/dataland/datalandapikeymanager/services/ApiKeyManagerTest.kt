package org.dataland.datalandapikeymanager.services

import jakarta.transaction.Transactional
import org.dataland.datalandapikeymanager.DatalandApiKeyManager
import org.dataland.datalandapikeymanager.entities.ApiKeyEntity
import org.dataland.datalandapikeymanager.repositories.ApiKeyRepository
import org.dataland.datalandbackendutils.apikey.ApiKeyUtility
import org.dataland.datalandbackendutils.apikey.ParsedApiKey
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(classes = [DatalandApiKeyManager::class], properties = ["spring.profiles.active=nodb"])
@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ApiKeyManagerTest(
    @Autowired val testApiKeyManager: ApiKeyManager,
    @Autowired val testApiKeyRepository: ApiKeyRepository,
    @Autowired val testApiKeyUtility: ApiKeyUtility,
) {
    @MockitoBean
    private val mockKeycloakUserService = mock<KeycloakUserService>()
    private val keycloakUserId = "dummyUserId"

    private fun createApiKeyWithExpiry(expiryTime: Long?): String {
        val secret = testApiKeyUtility.generateApiKeySecret()
        val parsedApiKey = ParsedApiKey(keycloakUserId, secret)
        val encodedSecret = testApiKeyUtility.encodeSecret(secret)
        val testDataEntity = ApiKeyEntity(encodedSecret, keycloakUserId, expiryTime)
        testApiKeyRepository.save(testDataEntity)
        return testApiKeyUtility.convertToApiKey(parsedApiKey)
    }

    @Test
    fun `create a test which checks that an expired api key is flagged as expired`() {
        val apiKeyToCheck = createApiKeyWithExpiry(1000)
        val responseMessage = testApiKeyManager.validateApiKey(apiKeyToCheck).validationMessage
        Assertions.assertEquals(
            "The API key you provided for your Dataland account is expired.",
            responseMessage,
            "The expired api key was unexpectedly validated.",
        )
        Mockito.verify(mockKeycloakUserService, Mockito.times(1)).getUserRoleNames(keycloakUserId)
    }

    @Test
    fun `check that never expiring api keys are validated correctly`() {
        val apiKeyToCheck = createApiKeyWithExpiry(null)
        val responseMessage = testApiKeyManager.validateApiKey(apiKeyToCheck).validationMessage
        Assertions.assertEquals(
            "The API key you provided was successfully validated.",
            responseMessage,
        )
        Mockito.verify(mockKeycloakUserService, Mockito.times(1)).getUserRoleNames(keycloakUserId)
    }
}
