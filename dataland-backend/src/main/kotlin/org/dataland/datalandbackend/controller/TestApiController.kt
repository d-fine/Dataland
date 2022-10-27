package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.TestApi
import org.dataland.datalandbackend.exceptions.InternalServerErrorApiException
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * API controller used for testing
 */
@RestController
@Profile("development", "ci")
class TestApiController : TestApi {
    override fun getDummy500Response(): ResponseEntity<Unit> {
        throw InternalServerErrorApiException("This is a requested dummy 500 response")
    }
}
