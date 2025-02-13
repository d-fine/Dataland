package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
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
    @PreAuthorize("@CompanyRoleChecker.canUserUploadDataForCompany(#companyAssociatedData.companyId)")
    fun postCompanyAssociatedData(
        @Valid @RequestBody
        companyAssociatedData: CompanyAssociatedData<T>,
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
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getCompanyAssociatedDataByDimensions(
        @RequestParam reportingPeriod: String,
        @RequestParam companyId: String,
    ): ResponseEntity<CompanyAssociatedData<T>>

    /**
     * A method to export the CompanyAssociatedData for a dataId to CSV
     * @param dataId identifier used to uniquely identify a dataset
     * @return CSV of companyAssociatedData in form of InputStreamResource
     */
    @Operation(
        summary = "Export data identified by dataId to plain CSV.",
        description = "Export data identified by dataId to plain CSV.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully exported dataset as plain CSV file."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}/csv"],
        produces = ["text/csv"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @DataManager.isDatasetPublic(#dataId)")
    fun exportCompanyAssociatedDataToCsv(
        @PathVariable("dataId") dataId: String,
    ): ResponseEntity<InputStreamResource>

    /**
     * A method to export the CompanyAssociatedData for a dataId to Excel
     * @param dataId identifier used to uniquely identify a dataset
     * @return Excel of companyAssociatedData in form of InputStreamResource
     */
    @Operation(
        summary = "Export data identified by dataId to Excel-compatible CSV.",
        description = "Export data identified by dataId to an Excel-compatible CSV file.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully exported dataset as Excel-compatible CSV file."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}/excel"],
        produces = ["text/csv"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @DataManager.isDatasetPublic(#dataId)")
    fun exportCompanyAssociatedDataToExcel(
        @PathVariable("dataId") dataId: String,
    ): ResponseEntity<InputStreamResource>

    /**
     * A method to export the CompanyAssociatedData for a dataId to JSON
     * @param dataId identifier used to uniquely identify a dataset
     * @return JSON of companyAssociatedData in form of InputStreamResource
     */
    @Operation(
        summary = "Export data identified by dataId to JSON.",
        description = "Export data identified by dataId to JSON.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully exported dataset as JSON file."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @DataManager.isDatasetPublic(#dataId)")
    fun exportCompanyAssociatedDataToJson(
        @PathVariable("dataId") dataId: String,
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
    @PreAuthorize("hasRole('ROLE_USER') or @CompanyQueryManager.isCompanyPublic(#companyId)")
    fun getFrameworkDatasetsForCompany(
        @PathVariable("companyId") companyId: String,
        @RequestParam(defaultValue = "true") showOnlyActive: Boolean,
        @RequestParam reportingPeriod: String? = null,
    ): ResponseEntity<List<DataAndMetaInformation<T>>>
}
