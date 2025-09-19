package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.utils.InvalidEmailFormatApiException
import org.dataland.datalandcommunitymanager.model.EmailAddress
import org.dataland.datalandcommunitymanager.services.EmailAddressService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmailAddressControllerTest {
    private val mockEmailAddressService = mock<EmailAddressService>()

    private lateinit var emailAddressController: EmailAddressController

    private val validEmail = "test@example.com"
    private val validEmailAddress = EmailAddress(validEmail)
    private val invalidEmail = "this-is-not-an-email"
    private val invalidEmailAddress = EmailAddress(invalidEmail)

    @BeforeEach
    fun setup() {
        reset(mockEmailAddressService)
        emailAddressController = EmailAddressController(mockEmailAddressService)
    }

    @Test
    fun `check that an invalid email address is rejected`() {
        assertThrows<InvalidEmailFormatApiException> {
            emailAddressController.postEmailAddressValidation(invalidEmailAddress)
        }
    }

    @Test
    fun `check that a valid email address is forwarded to the service layer`() {
        assertDoesNotThrow {
            emailAddressController.postEmailAddressValidation(validEmailAddress)
        }

        verify(mockEmailAddressService, times(1)).validateEmailAddress(validEmail)
    }
}
