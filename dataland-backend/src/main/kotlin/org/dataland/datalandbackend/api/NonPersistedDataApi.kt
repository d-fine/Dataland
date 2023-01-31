package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Defines the restful dataland-backend API regarding data exchange
 */
@RequestMapping("/internal/nonpersisted")
interface NonPersistedDataApi {

@Operation(
    summary = "Retrieve specific data from the hashmap of the backend.",
    description = "Data identified by the provided data ID is retrieved."
)
@ApiResponses(
    value = [
        ApiResponse(responseCode = "200", description = "Successfully retrieved data set.")
    ]
)
@GetMapping(
    value = ["/{dataId}"],
    produces = ["application/json"]
)
        /**
         * A method to retrieve specific data identified by its ID
         * @param dataId identifier used to uniquely specify data in the data store
         * @return the complete data stored under the provided data ID with the associated company ID
         */
fun getCompanyAssociatedDataForInternalStorage(@PathVariable("dataId") dataId: String):
        ResponseEntity<String>
}
