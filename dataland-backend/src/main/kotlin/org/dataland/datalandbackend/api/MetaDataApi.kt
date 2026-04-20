package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationPatch
import org.dataland.datalandbackend.model.metainformation.NonSourceabilityInformationResponse
import org.dataland.datalandbackend.model.metainformation.NonSourceabilityRequest
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CompanyIdParameterNonRequired
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CompanyIdParameterRequired
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataIdParameterRequired
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataTypeParameterNonRequired
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.ReportingPeriodParameterNonRequired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getListOfDataMetaInfo(
        @RequestParam
        @CompanyIdParameterNonRequired
        companyId: String? = null,
        @RequestParam
        @DataTypeParameterNonRequired
        dataType: DataType? = null,
        @RequestParam(defaultValue = "true")
        @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.SHOW_ONLY_ACTIVE_DESCRIPTION,
            required = false,
        )
        showOnlyActive: Boolean,
        @RequestParam
        @ReportingPeriodParameterNonRequired
        reportingPeriod: String? = null,
        @RequestParam
        @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.ALL_UPLOADER_USER_IDS_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.ALL_UPLOADER_USER_IDS_EXAMPLE,
            required = false,
        )
        uploaderUserIds: Set<UUID>? = null,
        @RequestParam
        @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
            required = false,
        )
        qaStatus: QaStatus? = null,
    ): ResponseEntity<List<DataMetaInformation>>

    /**
     * A method to post multiple filters at once to search for meta information about datasets registered by Dataland
     * @param dataMetaInformationSearchFilters A list of data meta information request filters
     * @return a list of matching DataMetaInformation
     */
    @Operation(
        summary = "Search in Dataland for meta info about data using multiple filters.",
        description = "Meta info about datasets registered by Dataland can be retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved meta info."),
        ],
    )
    @PostMapping(
        value = ["/filters"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun postListOfDataMetaInfoFilters(
        @RequestBody dataMetaInformationSearchFilters: List<DataMetaInformationSearchFilter>,
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
        @DataIdParameterRequired
        @PathVariable dataId: String,
    ): ResponseEntity<DataMetaInformation>

    /**
     * A method to update meta info for a specific dataset
     * @param dataId unique identifier of the dataset to be patched
     * @param dataMetaInformationPatch request body containing the meta information to be patched
     * @return updated information about the dataset
     */
    @Operation(
        summary = "Update meta data of dataset selectively",
        description = "Provided fields of the meta data with the given dataId are updated.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully updated dataset."),
        ],
    )
    @PatchMapping(
        value = ["/{dataId}"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun patchDataMetaInfo(
        @DataIdParameterRequired
        @PathVariable("dataId") dataId: String,
        @Valid @RequestBody(required = true)
        dataMetaInformationPatch: DataMetaInformationPatch,
    ): ResponseEntity<DataMetaInformation>

    /**
     * A method to retrieve a map with all data IDs of the data-points contained in the dataset
     * @param dataId identifier used to uniquely specify the dataset in question
     * @return a map of data point types to data point ids.
     */
    @Operation(
        summary = "Retrieve a map of data point IDs the dataset is composed of to their corresponding technical ID.",
        description = "Returns a map with keys being the datapoint types and the values being the data point Ids.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved map of data point IDs.",
                content = [
                    Content(
                        schema =
                            Schema(
                                type = "object",
                                additionalPropertiesSchema = String::class,
                                description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_MAP_DESCRIPTION,
                                example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_MAP_EXAMPLE,
                            ),
                    ),
                ],
            ),
        ],
    )
    @GetMapping(
        value = ["/{dataId}/data-points"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @DataManager.isDatasetPublic(#dataId)")
    fun getContainedDataPoints(
        @DataIdParameterRequired
        @PathVariable("dataId") dataId: String,
    ): ResponseEntity<Map<String, String>>

    /**
     * Retrieves non-sourceability information records using the canonical model.
     * @param companyId optional filter
     * @param dataType optional filter
     * @param reportingPeriod optional filter
     * @param qaStatus optional filter on QA status
     */
    @Operation(
        summary = "Retrieve non-sourceability information for datasets",
        description = "Retrieves NonSourceabilityInformation records filtered by the provided parameters.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved non-sourceability records."),
        ],
    )
    @GetMapping(
        value = ["/nonSourceable"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getInfoOnNonSourceabilityOfDatasets(
        @RequestParam
        @CompanyIdParameterNonRequired
        companyId: String? = null,
        @RequestParam
        @DataTypeParameterNonRequired
        dataType: DataType? = null,
        @RequestParam
        @ReportingPeriodParameterNonRequired
        reportingPeriod: String? = null,
        @RequestParam
        @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
            required = false,
        )
        qaStatus: QaStatus? = null,
    ): ResponseEntity<List<NonSourceabilityInformationResponse>>

    /**
     * Submits a non-sourceability request for a dataset.
     * When bypassQa=true the caller must hold ROLE_ADMIN; otherwise ROLE_UPLOADER is required.
     * When bypassQa=true and currentlyActive=false the request reverses a non-sourceability entry.
     * @param nonSourceabilityRequest request body containing dataset dimensions, bypass flag, and currentlyActive flag.
     */
    @Operation(
        summary = "Submit a non-sourceability request for a dataset.",
        description =
            "Creates a NonSourceabilityInformation entry. With bypassQa=false (default) and currentlyActive=false " +
                "the entry enters QA review. With bypassQa=true and currentlyActive=true (admin only) it is " +
                "immediately activated. With bypassQa=true and currentlyActive=false (admin only) the active " +
                "entry is deactivated (triple marked as sourceable again).",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully submitted non-sourceability request."),
            ApiResponse(responseCode = "400", description = "Invalid combination of bypassQa and currentlyActive."),
            ApiResponse(responseCode = "403", description = "Insufficient role for bypassQa."),
            ApiResponse(responseCode = "409", description = "Conflict — duplicate entry or invalid state transition."),
        ],
    )
    @PostMapping(
        value = ["/nonSourceable"],
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER') or hasRole('ROLE_ADMIN')")
    fun postNonSourceabilityOfADataset(
        @Valid @RequestBody
        nonSourceabilityRequest: NonSourceabilityRequest,
    ): ResponseEntity<NonSourceabilityInformationResponse>

    /**
     * Checks whether an active non-sourceability entry exists for the given tuple.
     * Returns 200 if active, 404 otherwise.
     */
    @Operation(
        summary = "Checks if a dataset is currently non-sourceable.",
        description = "Returns 200 if an active NonSourceabilityInformation entry exists for the tuple; 404 otherwise.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Dataset has an active non-sourceability entry."),
            ApiResponse(responseCode = "404", description = "Dataset has no active non-sourceability entry."),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/nonSourceable/{companyId}/{dataType}/{reportingPeriod}"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun isDataNonSourceable(
        @CompanyIdParameterRequired
        @PathVariable("companyId") companyId: String,
        @Parameter(
            name = "dataType",
            description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
            required = true,
        )
        @PathVariable("dataType") dataType: DataType,
        @Parameter(
            name = "reportingPeriod",
            description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
            required = true,
        )
        @PathVariable("reportingPeriod") reportingPeriod: String,
    )

    /**
     * A method to retrieve all  available data dimensions filtered by the provided parameters.
     * @param companyIds a list of company identifiers to filter for
     * @param frameworksOrDataPointTypes a list of frameworks or data point types (or mixture thereof) to filter for
     * @param reportingPeriods a list of reporting periods to filter for
     */
    @Operation(
        summary = "Checks if any data is available applying the provided filters.",
        description = "Checks if any data is available using the given filters and returns the corresponding data dimensions.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully checked if data is available."),
        ],
    )
    @GetMapping(
        value = ["/available-data-dimensions"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getAvailableDataDimensions(
        @RequestParam
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.COMPANY_IDS_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_IDS_EXAMPLE,
            required = false,
        )
        companyIds: List<String>? = null,
        @RequestParam
        @Parameter(
            description = BackendOpenApiDescriptionsAndExamples.FRAMEWORKS_OR_DATA_POINT_TYPES_DESCRIPTION,
            example = BackendOpenApiDescriptionsAndExamples.FRAMEWORKS_OR_DATA_POINT_TYPES_EXAMPLE,
            required = false,
        )
        frameworksOrDataPointTypes: List<String>? = null,
        @RequestParam
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_EXAMPLE,
            required = false,
        )
        reportingPeriods: List<String>? = null,
    ): ResponseEntity<List<BasicDataDimensions>>

    /**
     * A method to retrieve all meta data for active datasets matching the provided data dimensions.
     * @param dataDimensions a list of data dimensions to search for
     */
    @Operation(
        summary = "Checks if active datasets are available.",
        description =
            "Checks if any active datasets are available applying the provided data dimensions " +
                "and returns the corresponding meta data.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved meta data of active datasets."),
        ],
    )
    @PostMapping(
        value = ["/active-dataset-search"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun retrieveMetaDataOfActiveDatasets(
        @RequestBody dataDimensions: List<BasicDataDimensions>,
    ): ResponseEntity<List<DataMetaInformation>>
}
