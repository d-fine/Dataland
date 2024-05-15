package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
     * A method to store private sme data via Dataland into a data store
     */
    @PreAuthorize(
        "(hasRole('ROLE_USER') " +
            "and @DataOwnersManager.isCurrentUserDataOwner(#companyAssociatedSmeData.companyId))",
    )
    @Operation(
        summary = "Upload a new private sme data set.",
        description = "The uploaded private sme data is added to the private data store, the generated data id is " +
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
        @RequestPart(value = "documents") documents: Array<MultipartFile>?,
    ): ResponseEntity<DataMetaInformation>

    /**
     * A method to retrieve specific data identified by its ID
     * @param dataId identifier used to uniquely specify data in the data store
     * @return the complete data stored under the provided data ID with the associated company ID
     */

    @PreAuthorize(
        "(hasRole('ROLE_USER') " +
            "and @DataOwnersManager.isCurrentUserDataOwner(#companyAssociatedSmeData.companyId))",
    )
    @Operation(
        summary = "Retrieve specific data from the private data store.",
        description = "Data identified by the provided data ID is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data set."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}"],
        produces = ["application/json"],
    )
    fun getCompanyAssociatedSmeData(@PathVariable("dataId") dataId: String):
        ResponseEntity<CompanyAssociatedData<SmeData>>

    /**
     * Retrieve a document by its ID
     * @param hash the hash of the document
     * @param dataId the dataId to which the document is connected
     */

    @PreAuthorize(
        "(hasRole('ROLE_USER') " +
            "and @DataOwnersManager.isCurrentUserDataOwner(#companyAssociatedSmeData.companyId))",
    )
    @Operation(
        summary = "Receive a document.",
        description = "Receive a document by its ID from internal storage.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully received document.",
                headers = [Header(name = HttpHeaders.CONTENT_DISPOSITION, schema = Schema(type = "string"))],
            ),
        ],
    )
    @GetMapping(
        produces = [
            "application/json",
            "application/pdf",
        ],
    )
    fun getPrivateDocument(
        @RequestParam("dataId") dataId: String,
        @RequestParam("hash") hash: String,
    ): ResponseEntity<InputStreamResource>
}
