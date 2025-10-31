package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.services.InheritedRolesManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InheritedRolesControllerTest {
    private val mockInheritedRolesManager = mock<InheritedRolesManager>()
    private lateinit var inheritedRolesController: InheritedRolesController

    private val validUserId = UUID.randomUUID().toString()
    private val invalidUserId = "not-a-uuid"

    @BeforeEach
    fun setup() {
        reset(mockInheritedRolesManager)

        inheritedRolesController = InheritedRolesController(mockInheritedRolesManager)
    }

    @Test
    fun `ensure that an invalid user ID leads to the appropriate exception`() {
        assertThrows<ResourceNotFoundApiException> {
            inheritedRolesController.getInheritedRoles(invalidUserId)
        }
    }

    @Test
    fun `ensure that a valid user ID is processed without exceptions`() {
        assertDoesNotThrow { inheritedRolesController.getInheritedRoles(validUserId) }
    }
}
