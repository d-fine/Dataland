package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding legos.
 */
@RequestMapping("/legos")
//@SecurityRequirement(name = "default-bearer-auth")
//@SecurityRequirement(name = "default-oauth")
interface LegoApi {

    /**
     * A method to create a new lego dataset in dataland
     * @return information about the stored data
     */
    @Operation(
        summary = "Add a new data set.",
        description = "A new data set is added using the provided information.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data."),
        ],
    )
    @PostMapping(
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    //Todo activate authentication again
    //@PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun postLego(
        @RequestParam framework: String,
        @RequestParam reportingPeriod: String,
        @RequestParam companyId: String,
        @Valid @RequestBody
        data: String,
    ):
            ResponseEntity<String>

    /**
     * A method to get a lego dataset from dataland.
     * @return json dataset as string
     */
    @Operation(
        summary = "Get a lego dataset.",
        description = "A string representation of a lego dataset.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully returned dataset."),
        ],
    )
    @GetMapping(
        value = ["/{framework}/{reportingPeriod}/{companyId}"],
        produces = ["application/json"],
    )
    fun getLego(
        @PathVariable("framework") framework: String,
        @PathVariable("reportingPeriod") reportingPeriod: String,
        @PathVariable("companyId") companyId: String,
    ): ResponseEntity<String>
}
