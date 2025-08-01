package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserValidationServiceTest {
    private val mockKeycloakUserService = mock<KeycloakUserService>()
    private lateinit var userValidationService: UserValidationService

    private val knownEmailAddress = "known@example.com"
    private val unknownEmailAddress = "unknown@example.com"

    private val userId = UUID.randomUUID().toString()
    private val firstName = "Jane"
    private val lastName = "Doe"

    private val keycloakUserInfo =
        KeycloakUserInfo(
            email = knownEmailAddress,
            userId = userId,
            firstName = firstName,
            lastName = lastName,
        )

    @BeforeEach
    fun setUp() {
        reset(mockKeycloakUserService)

        doReturn(null).whenever(mockKeycloakUserService).findUserByEmail(unknownEmailAddress)
        doReturn(keycloakUserInfo).whenever(mockKeycloakUserService).findUserByEmail(knownEmailAddress)

        userValidationService = UserValidationService(mockKeycloakUserService)
    }

    @Test
    fun `check that an unknown email address results in a ResourceNotFoundApiException`() {
        assertThrows<ResourceNotFoundApiException> {
            userValidationService.validateEmailAddress(unknownEmailAddress)
        }
    }

    @Test
    fun `check that a known email address is processed as expected`() {
        val returnedKeycloakUserInfo =
            assertDoesNotThrow {
                userValidationService.validateEmailAddress(knownEmailAddress)
            }

        assertEquals(
            keycloakUserInfo,
            returnedKeycloakUserInfo,
        )
    }
}
