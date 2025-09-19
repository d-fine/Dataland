package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.utils.validateIsEmailAddress
import org.dataland.datalandcommunitymanager.api.EmailAddressApi
import org.dataland.datalandcommunitymanager.model.EmailAddress
import org.dataland.datalandcommunitymanager.services.EmailAddressService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller class for handling requests concerning user email addresses.
 */
@RestController
class EmailAddressController
    @Autowired
    constructor(
        private val emailAddressService: EmailAddressService,
    ) : EmailAddressApi {
        override fun postEmailAddressValidation(emailAddress: EmailAddress): ResponseEntity<KeycloakUserInfo> {
            val email = emailAddress.email
            email.validateIsEmailAddress()
            return ResponseEntity.ok(emailAddressService.validateEmailAddress(email))
        }

        override fun getUsersByCompanyAssociatedSubdomains(companyId: UUID): ResponseEntity<List<KeycloakUserInfo>> =
            ResponseEntity.ok(emailAddressService.getUsersByCompanyAssociatedSubdomains(companyId))
    }
