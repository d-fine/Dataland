package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfo
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfoResponse
import org.dataland.datalandbackendutils.model.QaStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

/**
 * Defines the restful dataland-backend API regarding meta data searches.
 */

@RequestMapping("/metadata")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface MetaDataApi {
    /**
     * A method to search for meta info about datasets registered by Dataland
     * @param companyId if set, filters the requested meta info to a specific company.
     * @param dataType if set, filters the requested meta info to a specific data type.
     * @param showOnlyActive if set to true or empty, only metadata of QA reports are returned that are active.
     *   If set to false, all QA reports will be returned regardless of their active status.
     * @param reportingPeriod if set, the method only returns meta info with this reporting period
     * @param uploaderUserIds if set, the method will only return meta info for datasets uploaded by these user ids.
     * @param qaStatus if set, the method only returns meta info for datasets with this qa status
     * @return a list of matching DataMetaInformation
     */
    @Operation(
        summary = "Search in Dataland for meta info about data.",
        description = "Meta info about datasets registered by Dataland can be retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved meta info."),
        ],
    )
    @GetMapping(
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @CompanyQueryManager.isCompanyPublic(#companyId)")
    fun getListOfDataMetaInfo(
        @RequestParam companyId: String? = null,
        @RequestParam dataType: DataType? = null,
        @RequestParam(defaultValue = "true") showOnlyActive: Boolean,
        @RequestParam reportingPeriod: String? = null,
        @RequestParam uploaderUserIds: Set<UUID>? = null,
        @RequestParam qaStatus: QaStatus? = null,
    ): ResponseEntity<List<DataMetaInformation>>

    /**
     * A method to retrieve meta info about a specific dataset
     * @param dataId as unique identifier for a specific dataset
     * @return the DataMetaInformation for the specified dataset
     */
    @Operation(
        summary = "Look up meta info about a specific dataset.",
        description =
            "Meta info about a specific dataset registered by Dataland " +
                "and identified by its data ID is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved specific meta info."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @DataManager.isDatasetPublic(#dataId)")
    fun getDataMetaInfo(
        @PathVariable dataId: String,
    ): ResponseEntity<DataMetaInformation>

    /**
     * A method to retrieve a list of all data IDs of the data-points contained in the dataset
     * @param dataId identifier used to uniquely specify the dataset in question
     * @return a list of data point IDs contained in the dataset
     */
    @Operation(
        summary = "Retrieve a map of data point IDs the dataset is composed of to their corresponding technical ID.",
        description = "The IDs of the data points contained in the dataset specified are returned as a map of strings to string.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved list of data point IDs."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}/data-points"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @DataManager.isDatasetPublic(#dataId)")
    fun getContainedDataPoints(
        @PathVariable("dataId") dataId: String,
    ): ResponseEntity<Map<String, String>>

    /**
     * A method to retrieve information about the sourceability of datasets.
     * @param companyId if set, filters the requested info by companyId
     * @param dataType if set, filters the requested info by data type.
     * @param reportingPeriod if set, the method only returns meta info with this reporting period
     * @param nonSourceable if set true, the method only returns meta info for datasets which are
     * non-sourceable and if set false, it returns sourceable data.
     * @return A list of NonSourceableInfo matching the filters, or an empty list if none found.
     */
    @Operation(
        summary = "Retrieve information about the sourceability of datasets",
        description =
            "Retrieve information about the sourceability of datasets by the filters.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved datasets."),
        ],
    )
    @GetMapping(
        value = ["/nonSourceable"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getInfoOnNonSourceabilityOfDatasets(
        @RequestParam companyId: String? = null,
        @RequestParam dataType: DataType? = null,
        @RequestParam reportingPeriod: String? = null,
        @RequestParam nonSourceable: Boolean? = null,
    ): ResponseEntity<List<NonSourceableInfoResponse>>

    /**
     * Adds a dataset with information on sourceability.
     * @param nonSourceableInfo includes the information on the sourceability of a specific dataset.
     */
    @Operation(
        summary = "Adds a dataset with information on sourceability.",
        description = "A dataset is added with information on its sourceability.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added dataset."),
            ApiResponse(
                responseCode = "400",
                description = "Bad request has been submitted.",
            ),
            ApiResponse(responseCode = "404", description = "Invalid input parameters."),
        ],
    )
    @PostMapping(
        value = ["/nonSourceable"],
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun postNonSourceabilityOfADataset(
        @Valid @RequestBody
        nonSourceableInfo: NonSourceableInfo,
    )

    /**
     * A method to check if a dataset is non-sourceable.
     * @param companyId the company identifier
     * @param dataType the data type
     * @param reportingPeriod the reporting period
     */
    @Operation(
        summary = "Checks if a dataset is non-sourceable.",
        description = "Checks if a specific dataset is non-sourceable.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully checked that dataset is non-sourceable."),
            ApiResponse(responseCode = "404", description = "Successfully checked that dataset is sourceable."),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/nonSourceable/{companyId}/{dataType}/{reportingPeriod}"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun isDataNonSourceable(
        @PathVariable("companyId") companyId: String,
        @PathVariable("dataType") dataType: DataType,
        @PathVariable("reportingPeriod") reportingPeriod: String,
    )
}
