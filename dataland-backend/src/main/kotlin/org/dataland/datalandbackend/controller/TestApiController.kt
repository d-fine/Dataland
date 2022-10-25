package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.TestApi
import org.dataland.datalandbackend.exceptions.InternalServerErrorApiException
import org.dataland.skyminderClient.model.ContactInformation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * API controller used for testing
 */
@RestController
class TestApiController : TestApi {
    override fun getDummy500Response(): ResponseEntity<List<ContactInformation>> {
        throw InternalServerErrorApiException("This is a requested dummy 500 response")
    }
}
