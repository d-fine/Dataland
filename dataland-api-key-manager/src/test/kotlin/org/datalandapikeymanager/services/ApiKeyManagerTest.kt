package org.datalandapikeymanager.services

import org.dataland.datalandapikeymanager.DatalandApiKeyManager
import org.dataland.datalandapikeymanager.entities.ApiKeyEntity
import org.dataland.datalandapikeymanager.repositories.ApiKeyRepository
import org.dataland.datalandapikeymanager.services.ApiKeyManager
import org.dataland.datalandbackendutils.apikey.ApiKeyUtility
import org.dataland.datalandbackendutils.apikey.ParsedApiKey
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import javax.transaction.Transactional

@ComponentScan(basePackages = ["org.dataland"])
@SpringBootTest(classes = [DatalandApiKeyManager::class])
@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ApiKeyManagerTest(
    @Autowired val testApiKeyManager: ApiKeyManager,
    @Autowired val testApiKeyRepository: ApiKeyRepository,
    @Autowired val testApiKeyUtility: ApiKeyUtility,
) {
    @Test
    fun `create a test which checks that an expired api key is flagged as expired`() {
        val keycloakUserId="18b67ecc-1176-4506-8414-1e81661017ca"
        val secret = testApiKeyUtility.generateApiKeySecret()
        val parsedApiKey = ParsedApiKey(keycloakUserId, secret)
        val encodedSecret = testApiKeyUtility.encodeSecret(secret)
        val testDataEntity = ApiKeyEntity(encodedSecret, keycloakUserId, listOf("READER"), 1000)
        testApiKeyRepository.save(testDataEntity)
        val apiKeyToCheck = testApiKeyUtility.convertToApiKey(parsedApiKey)
        val responseMessage = testApiKeyManager.validateApiKey(apiKeyToCheck).validationMessage
        Assertions.assertEquals(
            "The API key you provided for your Dataland account is expired.",
            responseMessage,
            "The expired api key was unexpectedly validated."
        )
    }
}
