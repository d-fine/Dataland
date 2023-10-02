package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.interfaces.api.TokenValidityApiInterface
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for token validity
 */
@RestController
class TokenValidityController : TokenValidityApiInterface {
    override fun validateToken() {
        // this method is supposed to be empty
        // it should do as less as possible but return a 200 code if and only if called with a valid token
    }
}
