package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.skyminderClient.model.ContactInformation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding the communication with Skyminder.
 */

@RequestMapping("/skyminder")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
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
        produces = ["application/json"]
    )
    @PreAuthorize("hasRole(@RoleContainer.DATA_READER)")
    /**
     * A method to search for company data using the Skyminder API.
     * @param countryCode three-letter ISO country code (e.g. DEU for Germany)
     * @param companyName string to be used for searching in Skyminder
     * @return the list of ContactInformation generated from all responses returned by Skyminder
     */
    fun getDataSkyminderRequest(
        @RequestParam countryCode: String,
        @RequestParam companyName: String
    ): ResponseEntity<List<ContactInformation>>
}
