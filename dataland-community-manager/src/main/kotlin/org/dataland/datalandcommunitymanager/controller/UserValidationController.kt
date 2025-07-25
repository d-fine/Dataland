package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.utils.validateIsEmailAddress
import org.dataland.datalandcommunitymanager.api.UserValidationApi
import org.dataland.datalandcommunitymanager.model.BasicUserInformation
import org.dataland.datalandcommunitymanager.model.EmailAddress
import org.dataland.datalandcommunitymanager.services.UserValidationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller class for validating user-related information.
 */
@RestController
class UserValidationController(
    @Autowired private val userValidationService: UserValidationService,
) : UserValidationApi {
    override fun postEmailAddressValidation(emailAddress: EmailAddress): ResponseEntity<BasicUserInformation> {
        val email = emailAddress.email
        email.validateIsEmailAddress()
        return ResponseEntity.ok(userValidationService.validateEmailAddress(email))
    }
}
