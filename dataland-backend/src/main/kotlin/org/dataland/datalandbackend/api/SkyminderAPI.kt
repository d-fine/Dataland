package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.skyminderClient.model.ContactInformation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API regarding the communication with Skyminder.
 */

@RequestMapping("/")
interface SkyminderAPI {
    @Operation(
        summary = "Retrieve company data from Skyminder server.",
        description = "Gets company data from Skyminder using a country ISO code and company name."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved company data from Skyminder.")
        ]
    )
    @GetMapping(
        value = ["/data/skyminder/{code}/{name}"],
        produces = ["application/json"]
    )
    /**
     * A method to search for company data using the Skyminder API.
     * @param countryCode three-letter ISO country code (e.g. DEU for Germany)
     * @param name string to be used for searching in Skyminder
     * @return the list of ContactInformation generated from all responses returned by Skyminder
     */
    fun getDataSkyminderRequest(
        @PathVariable("code") countryCode: String,
        @PathVariable("name") name: String
    ): ResponseEntity<List<ContactInformation>>
}
