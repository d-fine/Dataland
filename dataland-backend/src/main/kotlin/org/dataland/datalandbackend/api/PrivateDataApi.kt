package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

/**
 * Defines the restful dataland-backend API regarding company data.
 */
@RequestMapping("/data/sme") // TODO not final!
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface PrivateDataApi {
    /**
     * TODO
     */
    // TODO activate at the end to allow only owners
     /*@PreAuthorize("(hasRole('ROLE_USER') " +
             "and @DataOwnersManager.isCurrentUserDataOwner(#companyAssociatedData.companyId))",)*/
    @Operation(
        summary = "a", // TODO
        description = "b", // TODO
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "S."), // TODO
        ],
    )
    @PostMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun postSmeJsonAndDocuments(
        @RequestPart
        companyAssociatedSmeData: String,
        @RequestPart documents: Array<MultipartFile>,
    ):
        ResponseEntity<DataMetaInformation>
}

// TODO nginx beschwert sich noch wenn die pdfs zu groß sind!  dort musst du auch die maxxfile size einstellen
