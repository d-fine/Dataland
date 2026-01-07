package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportJob
import org.dataland.datalandbackend.model.export.ExportRequestData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CompanyIdParameterRequired
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataIdParameterRequired
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.IdentifierParameterRequired
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.ReportingPeriodParameterNonRequired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding data exchange
 */

@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DataApi<T> {
    /**
     * A method to store data via Dataland into a data store
     * @param companyAssociatedData consisting of the ID of the company and the data to be stored
     * @return meta info about the stored data including the ID of the created entry in the data store
     */
    @Operation(
        summary = "Upload new dataset.",
        description = "The uploaded data is added to the data store, the generated data id is returned.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data to the data store."),
        ],
    )
    @PostMapping(
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("@CompanyRoleChecker.canUserUploadDataForCompany(#companyAssociatedData.companyId, #bypassQa)")
    fun postCompanyAssociatedData(
        @Valid @RequestBody
        companyAssociatedData: CompanyAssociatedData<T>,
        @Parameter(
            name = "bypassQa",
            description = BackendOpenApiDescriptionsAndExamples.BYPASS_QA_DESCRIPTION,
            required = false,
        )
        @RequestParam(defaultValue = "false") bypassQa: Boolean,
    ): ResponseEntity<DataMetaInformation>

    /**
     * A method to retrieve specific data identified by its ID
     * @param dataId identifier used to uniquely specify data in the data store
     * @return the complete data stored under the provided data ID with the associated company ID
     */
    @Operation(
        summary = "Retrieve specific data from the data store.",
        description = "Data identified by the provided data ID is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @DataManager.isDatasetPublic(#dataId)")
    fun getCompanyAssociatedData(
        @DataIdParameterRequired
        @PathVariable("dataId") dataId: String,
    ): ResponseEntity<CompanyAssociatedData<T>>

    /**
     * A method to retrieve specific data identified by its [reportingPeriod], [companyId] and data type [T]
     * @param reportingPeriod specifies the reporting period
     * @param companyId specifies the company
     * @return the dataset stored or an error if no dataset can be found
     */
    @Operation(
        summary = "Retrieve data for the company ID and reporting period provided.",
        description = "Data identified by the company ID and reporting Period is retrieved, if available.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset."),
        ],
    )
    @GetMapping(
        value = ["/"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getCompanyAssociatedDataByDimensions(
        @Parameter(
            name = "reportingPeriod",
            description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
            required = true,
        )
        @RequestParam reportingPeriod: String,
        @CompanyIdParameterRequired
        @RequestParam companyId: String,
    ): ResponseEntity<CompanyAssociatedData<T>>

    /**
     * A method to post an export job of the CompanyAssociatedData by its reporting periods and company IDs.
     */
    @Operation(
        summary = "Start an export job for the reportingPeriods and companyIds provided.",
        description =
            "Triggers asynchronous export job for the reportingPeriods and companyIds provided." +
                "specified format (CSV, Excel-compatible CSV, JSON).",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully started export job."),
            ApiResponse(
                responseCode = "404",
                description = "Input parameter could not be found.",
                content = [Content(mediaType = "")],
            ),
        ],
    )
    @PostMapping(
        value = ["/export-jobs"],
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun postExportJobCompanyAssociatedDataByDimensions(
        @Valid @RequestBody
        exportRequestData: ExportRequestData,
        @Parameter(
            name = "keepValueFieldsOnly",
            description = BackendOpenApiDescriptionsAndExamples.KEEP_VALUE_FIELDS_ONLY_DESCRIPTION,
            required = false,
        )
        @RequestParam(
            value = "keepValueFieldsOnly",
            defaultValue = "true",
        ) keepValueFieldsOnly: Boolean = true,
        @RequestParam(
            value = "includeAliases",
            defaultValue = "true",
        ) includeAliases: Boolean = true,
    ): ResponseEntity<ExportJob>

    /**
     * A method to post an export job of the CompanyAssociatedData by its reporting periods and company IDs.
     */
    @Operation(
        summary = "Get information about export job being done.",
        description =
            "Check state of export job.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully exported datasets."),
            ApiResponse(
                responseCode = "204",
                description = "No data for download available.",
                content = [Content(mediaType = "")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Export job Id could not be found.",
                content = [Content(mediaType = "")],
            ),
        ],
    )
    @GetMapping(
        value = ["/export/state/{exportJobId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getExportJobState(
        @PathVariable(
            value = "exportJobId",
        ) exportJobId: String,
    ): ResponseEntity<ExportJobProgressState>

    /**
     * A method to export the CompanyAssociatedData by its reporting periods and company IDs.
     */
    @Operation(
        summary = "Export data exportJobId provided.",
        description =
            "Export data for each combination of reportingPeriod and companyId provided into a file of the " +
                "specified format (CSV, Excel-compatible CSV, JSON).",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully exported datasets."),
            ApiResponse(
                responseCode = "204",
                description = "No data for download available.",
                content = [Content(mediaType = "")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Company Id could not be found.",
                content = [Content(mediaType = "")],
            ),
        ],
    )
    @PostMapping(
        value = ["/export/download/{exportJobId}"],
        produces = ["application/octet-stream"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun exportCompanyAssociatedDataById(
        @PathVariable(
            value = "exportJobId",
        ) exportJobId: String,
    ): ResponseEntity<InputStreamResource>

    /**
     * A method to retrieve framework datasets together with their meta info for one specific company identified by its
     * company ID, optionally filtered to one specific reporting period
     * @param companyId identifier of the company in Dataland
     * @param showOnlyActive if set to true, only active datasets will be returned (e.g. no outdated ones)
     * @param reportingPeriod identifies a specific reporting period (e.g. a year or quarter)
     * @return a list of all datasets for the chosen company and framework, filtered by the chosen arguments
     */
    @Operation(
        summary = "Retrieve framework datasets with meta info.",
        description = "All framework datasets with meta info for the given company ID are retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved framework datasets with meta info.",
            ),
        ],
    )
    @GetMapping(
        value = ["/companies/{companyId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getFrameworkDatasetsForCompany(
        @CompanyIdParameterRequired
        @PathVariable("companyId") companyId: String,
        @Parameter(
            name = "showOnlyActive",
            description = BackendOpenApiDescriptionsAndExamples.SHOW_ONLY_ACTIVE_DESCRIPTION,
            required = false,
        )
        @RequestParam(defaultValue = "true") showOnlyActive: Boolean,
        @ReportingPeriodParameterNonRequired
        @RequestParam reportingPeriod: String? = null,
    ): ResponseEntity<List<DataAndMetaInformation<T>>>

    /**
     * A method to retrieve the latest available data for a given [identifier] and the implicitly defined data type [T]
     * @param identifier specifies the company
     * @return the dataset stored or an error if no dataset can be found
     */
    @Operation(
        summary = "Retrieve the latest data for the company provided.",
        description = "The latest data identified by the company ID is retrieved, if available.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset."),
            ApiResponse(responseCode = "400", description = "Company identifier is not unique."),
            ApiResponse(responseCode = "403", description = "You do not have the right to make this query."),
            ApiResponse(responseCode = "404", description = "Company or dataset could not be found."),
        ],
    )
    @GetMapping(
        value = ["/latest"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getLatestAvailableCompanyAssociatedData(
        @IdentifierParameterRequired
        @RequestParam identifier: String,
    ): ResponseEntity<CompanyAssociatedData<T>>
}
