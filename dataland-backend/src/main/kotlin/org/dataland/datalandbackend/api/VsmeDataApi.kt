package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.frameworks.vsme.model.VsmeData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
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
 * Defines the restful dataland-backend API regarding private vsme company data.
 */
@RequestMapping("/data/vsme")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface VsmeDataApi {
    /**
     * A method to store private vsme data via Dataland into a data store
     */
    @Operation(
        summary = "Upload a new private vsme data set.",
        description =
        "The uploaded private vsme data is added to the private data store, the generated data id is " +
            "returned.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data to the private data store."),
        ],
    )
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize(
        "hasRole('ROLE_USER') and " +
            "(@CompanyRoleChecker.hasCurrentUserGivenRoleForCompany(" +
            "#companyAssociatedVsmeData.companyId, " +
            "T(org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole).CompanyOwner" +
            ") or" +
            "@CompanyRoleChecker.hasCurrentUserGivenRoleForCompany(" +
            "#companyAssociatedVsmeData.companyId, " +
            "T(org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole).DataUploader" +
            ")" +
            ")",
    )
    fun postVsmeJsonAndDocuments(
        @RequestPart(value = "companyAssociatedVsmeData") companyAssociatedVsmeData: CompanyAssociatedData<VsmeData>,
        @RequestPart(value = "documents") documents: Array<MultipartFile>?,
    ): ResponseEntity<DataMetaInformation>

    /**
     * A method to retrieve specific data identified by its ID
     * @param dataId identifier used to uniquely specify data in the data store
     * @return the complete data stored under the provided data ID with the associated company ID
     */
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
    @PreAuthorize(
        "hasRole('ROLE_USER') and " +
            "(@CompanyRoleChecker.hasCurrentUserAnyRoleForCompanyOfDataId(#dataId) or " +
            "@PrivateDataAccessChecker.hasUserAccessToPrivateResources(#dataId))",
    )
    fun getCompanyAssociatedVsmeData(
        @PathVariable("dataId") dataId: String,
    ): ResponseEntity<CompanyAssociatedData<VsmeData>>

    /**
     * Retrieve a document by its ID
     * @param hash the hash of the document
     * @param dataId the dataId to which the document is connected
     */
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
        value = ["/documents"],
        produces = [
            "application/json",
            "application/pdf",
        ],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER') and " +
            "(@CompanyRoleChecker.hasCurrentUserAnyRoleForCompanyOfDataId(#dataId) or " +
            "@PrivateDataAccessChecker.hasUserAccessToPrivateResources(#dataId))",
    )
    fun getPrivateDocument(
        @RequestParam("dataId") dataId: String,
        @RequestParam("hash") hash: String,
    ): ResponseEntity<InputStreamResource>

    /**
     * A method to retrieve vsme datasets together with their meta info for one specific company identified by its
     * company ID, optionally filtered to one specific reporting period
     * @param companyId identifier of the company in Dataland
     * @param showOnlyActive if set to true, only active datasets will be returned (e.g. no outdated ones)
     * @param reportingPeriod identifies a specific reporting period (e.g. a year or quarter)
     * @return a list of all vsme datasets for the chosen company, filtered by the chosen arguments
     */
    @Operation(
        summary = "Retrieve vsme datasets with meta info.",
        description = "All vsme datasets with meta info for the given company ID are retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved vsme datasets with meta info.",
            ),
        ],
    )
    @GetMapping(
        value = ["/companies/{companyId}"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_USER') and " +
            "(@CompanyRoleChecker.hasCurrentUserAnyRoleForCompany(#companyId) or" +
            "@PrivateDataAccessChecker.hasUserAccessToAtLeastOnePrivateResourceForCompany(#companyId))",
    )
    fun getFrameworkDatasetsForCompany(
        @PathVariable("companyId") companyId: String,
        @RequestParam(defaultValue = "true") showOnlyActive: Boolean,
        @RequestParam reportingPeriod: String? = null,
    ): ResponseEntity<List<DataAndMetaInformation<VsmeData>>>
}
