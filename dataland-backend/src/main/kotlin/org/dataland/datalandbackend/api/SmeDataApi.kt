package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

/**
 * Defines the restful dataland-backend API regarding private sme company data.
 */
@RequestMapping("/data/sme")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface SmeDataApi {
    /**
     * A method to store private data via Dataland into a data store
     */
    // TODO activate at the end to allow only owners
            /*@PreAuthorize("(hasRole('ROLE_USER') " +
                    "and @DataOwnersManager.isCurrentUserDataOwner(#companyAssociatedData.companyId))",)*/
    @Operation(
        summary = "Upload new private data set.",
        description = "The uploaded private data is added to the private data store, the generated data id is " +
            "returned.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data to the private data store."),
        ],
    )
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postSmeJsonAndDocuments(
        @RequestPart(value = "companyAssociatedSmeData") companyAssociatedSmeData: CompanyAssociatedData<SmeData>,
        @RequestPart(value = "documents") documents: Array<MultipartFile>? = null,
    ): ResponseEntity<DataMetaInformation>
}
