package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * Defines the restful dataland-backend API regarding private sme company data.
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
    /*     @PostMapping(
            produces = [MediaType.APPLICATION_JSON_VALUE],
            consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        )
       fun postSmeJsonAndDocuments(
            @RequestPart
            companyAssociatedSmeDataAsString: CompanyAssociatedData<SmeData>,
            @RequestPart documents: Array<MultipartFile>? = null,
        ):
            ResponseEntity<DataMetaInformation>
   */
   /* @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE],consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postSmeJsonAndDocuments(
        @RequestPart(value = "companyAssociatedSmeDataAsString") @Parameter(
            schema = Schema(
                type = "string",
                format = "binary"
            )
        ) companyAssociatedSmeDataAsString: CompanyAssociatedData<SmeData>,
        @RequestPart(value = "file") documents: Array<MultipartFile>? = null,
    ): ResponseEntity<DataMetaInformation>
*/
    @PostMapping()
    fun postSmeJsonAndDocuments(
        @RequestBody() data: CompanyAssociatedData<SmeData>,
        @RequestParam() documents: Array<MultipartFile>?
    ): ResponseEntity<DataMetaInformation>

}

// TODO nginx beschwert sich noch wenn die pdfs zu groß sind!  dort musst du auch die maxxfile size einstellen
