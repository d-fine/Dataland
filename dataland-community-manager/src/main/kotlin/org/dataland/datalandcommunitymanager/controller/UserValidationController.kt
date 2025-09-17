package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.utils.validateIsEmailAddress
import org.dataland.datalandcommunitymanager.api.UserValidationApi
import org.dataland.datalandcommunitymanager.model.EmailAddress
import org.dataland.datalandcommunitymanager.services.EmailAddressService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller class for validating user-related information.
 */
@RestController
class UserValidationController
    @Autowired
    constructor(
        private val emailAddressService: EmailAddressService,
    ) : UserValidationApi {
        override fun postEmailAddressValidation(emailAddress: EmailAddress): ResponseEntity<KeycloakUserInfo> {
            val email = emailAddress.email
            email.validateIsEmailAddress()
            return ResponseEntity.ok(emailAddressService.validateEmailAddress(email))
        }

        override fun getUsersByCompanyEmailSuffix(companyId: UUID): ResponseEntity<List<KeycloakUserInfo>> =
            ResponseEntity.ok(emailAddressService.getUsersByCompanyEmailSuffix(companyId))
    }
