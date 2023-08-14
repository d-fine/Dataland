package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.TokenValidityApi
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for token validity
 */
@RestController
class TokenValidityController : TokenValidityApi {
    override fun validateToken() {}
}