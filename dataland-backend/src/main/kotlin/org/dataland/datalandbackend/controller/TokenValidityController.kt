package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.TokenValidityApi
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for token validity
 */
@RestController
class TokenValidityController : TokenValidityApi {
    override fun validateToken() {
        // this method is supposed to be empty
        // it should do as less as possible but return a 200 code if and only if called with a valid token
    }
}
