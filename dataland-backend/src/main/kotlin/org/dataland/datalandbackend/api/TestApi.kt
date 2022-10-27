package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * An API interface purely used for testing
 */
@Hidden
@RequestMapping("/testing")
interface TestApi {
    /**
     * A dummy function that returns a 500 response to test the internal server error handling behaviour
     */
    @GetMapping("/getDummy500Response")
    fun getDummy500Response(): ResponseEntity<Unit>
}
