package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.utils.OpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.model.ExportFileType
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
            description = OpenApiDescriptionsAndExamples.BYPASS_QA_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.BYPASS_QA_EXAMPLE,
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
        @Parameter(
            name = "dataId",
            description = OpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
            required = true,
        )
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
            description = OpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
            required = true,
        )
        @RequestParam reportingPeriod: String,
        @Parameter(
            name = "companyId",
            description = OpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
            required = true,
        )
        @RequestParam companyId: String,
    ): ResponseEntity<CompanyAssociatedData<T>>

    /**
     * A method to export the CompanyAssociatedData by its [reportingPeriods], [companyIds] as a [exportFileType] file.
     * @param reportingPeriods specifies the reporting periods
     * @param companyIds specifies the companies
     * @param exportFileType specifies the file type to export to
     * @param keepValueFieldsOnly specifies whether to exclude metadata from the export
     * @return JSON of companyAssociatedData in the form of InputStreamResource
     */
    @Operation(
        summary = "Export data for the reportingPeriods and companyIds provided.",
        description =
            "Export data for the each combination of reportingPeriod and companyId provided into a file of the " +
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
    @GetMapping(
        value = ["/export"],
        produces = ["application/octet-stream"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun exportCompanyAssociatedDataByDimensions(
        @Parameter(
            name = "reportingPeriods",
            description = OpenApiDescriptionsAndExamples.REPORTING_PERIODS_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.REPORTING_PERIODS_EXAMPLE,
            required = true,
        )
        @RequestParam("reportingPeriods") reportingPeriods: List<String>,
        @Parameter(
            name = "companyIds",
            description = OpenApiDescriptionsAndExamples.COMPANY_IDS_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.COMPANY_IDS_EXAMPLE,
            required = true,
        )
        @RequestParam("companyIds") companyIds: List<String>,
        @Parameter(
            name = "fileFormat",
            description = OpenApiDescriptionsAndExamples.FILE_FORMAT_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.FILE_FORMAT_EXAMPLE,
            required = true,
        )
        @RequestParam("fileFormat") exportFileType: ExportFileType,
        @Parameter(
            name = "keepValueFieldsOnly",
            description = OpenApiDescriptionsAndExamples.KEEP_VALUE_FIELDS_ONLY_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.KEEP_VALUE_FIELDS_ONLY_EXAMPLE,
            required = false,
        )
        @RequestParam(
            value = "keepValueFieldsOnly",
            defaultValue = "true",
        ) keepValueFieldsOnly: Boolean = true,
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
        @Parameter(
            name = "companyId",
            description = OpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("companyId") companyId: String,
        @Parameter(
            name = "showOnlyActive",
            description = OpenApiDescriptionsAndExamples.SHOW_ONLY_ACTIVE_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.SHOW_ONLY_ACTIVE_EXAMPLE,
            required = false,
        )
        @RequestParam(defaultValue = "true") showOnlyActive: Boolean,
        @Parameter(
            name = "reportingPeriod",
            description = OpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
            example = OpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
            required = false,
        )
        @RequestParam reportingPeriod: String? = null,
    ): ResponseEntity<List<DataAndMetaInformation<T>>>
}
