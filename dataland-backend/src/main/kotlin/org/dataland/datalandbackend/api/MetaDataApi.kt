package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.metainformation.NonSourcableData
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
     * A method to search for meta info about data sets registered by Dataland
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
        description = "Meta info about data sets registered by Dataland can be retrieved.",
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
     * A method to retrieve meta info about a specific data set
     * @param dataId as unique identifier for a specific data set
     * @return the DataMetaInformation for the specified data set
     */
    @Operation(
        summary = "Look up meta info about a specific data set.",
        description =
            "Meta info about a specific data set registered by Dataland " +
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
    @PreAuthorize("hasRole('ROLE_USER') or @DataManager.isDataSetPublic(#dataId)")
    fun getDataMetaInfo(
        @PathVariable dataId: String,
    ): ResponseEntity<DataMetaInformation>

    /**
     * A method to retrieve meta info about a non sourcable data set
     * @param companyId if set, filters the requested meta info to a specific company.
     * @param dataType if set, filters the requested meta info to a specific data type.
     * @param showOnlyActive if set to true or empty, only metadata of QA reports are returned that are active.
     *   If set to false, all QA reports will be returned regardless of their active status.
     * @param reportingPeriod if set, the method only returns meta info with this reporting period
     * @param uploaderUserIds if set, the method will only return meta info for datasets uploaded by these user ids.
     * @param qaStatus if set, the method only returns meta info for datasets with this qa status
     * @return the DataMetaInformation for the specified data set
     */
    @Operation(
        summary = "Look up meta info about a non sourcable data set.",
        description =
            "Meta info about a specific data set registered by Dataland " +
                "and identified by its data ID is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data set."),
        ],
    )
    @GetMapping(
        value = ["/nonSourcable"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @DataManager.isDataSetPublic(#dataId)")
    fun getNonSourcableDatasets(
        @RequestParam companyId: String? = null,
        @RequestParam dataType: DataType? = null,
        @RequestParam(defaultValue = "true") showOnlyActive: Boolean,
        @RequestParam reportingPeriod: String? = null,
        @RequestParam uploaderUserIds: Set<UUID>? = null,
        @RequestParam qaStatus: QaStatus? = null,
    ): ResponseEntity<NonSourcableData>

    /**
     * A method to save information on the sourceability of a specific data set.
     * @param nonSourcableData includes the information on the sourceability of a specific data set.
     */
    @Operation(
        summary = "Add a data set with information on sourceability.",
        description = "A data set is added with information on its sourcability.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data set."),
        ],
    )
    @PostMapping(
        value = ["/nonSourcable"],
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postNonSourcableDataSet(
        @Valid @RequestBody
        nonSourcableData: NonSourcableData,
    )

    /**
     * A method to check if a dataset is non-sourcable
     * @param companyId the company identifier
     * @param dataType the data type
     * @param reportingPeriod the reporting period
     */
    @Operation(
        summary = "Checks that a data set is non sourcable.",
        description = "Checks that a specific data set is non sourcable.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully checked that data set is non-sourcable."),
            ApiResponse(responseCode = "404", description = "Successfully checked that data set is sourcable."),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/nonSourcable/{companyId}/{dataType}/{reportingPeriod}"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun isDataNonSourcable(
        @PathVariable("companyId") companyId: String,
        @PathVariable("dataType") dataType: DataType,
        @PathVariable("reportingPeriod") reportingPeriod: String,
    )
}
