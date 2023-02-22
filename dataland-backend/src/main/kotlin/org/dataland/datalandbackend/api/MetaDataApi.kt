package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding meta data searches.
 */

@RequestMapping("/metadata")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface MetaDataApi {

    /**
     * A method to search for meta info about data sets registered by Dataland
     * @param companyId filters the requested meta info to a specific company.
     * @param dataType filters the requested meta info to a specific data type.
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
    @PreAuthorize("hasRole('ROLE_USER') or @CompanyManager.isCompanyPublic(#companyId)")
    fun getListOfDataMetaInfo(
        @RequestParam companyId: String? = null,
        @RequestParam dataType: DataType? = null,
        @RequestParam showVersionHistoryForReportingPeriod: Boolean = false, // TODO could be an enum  "latest" and "history"
        @RequestParam reportingPeriod: String? = null,
    ):
        ResponseEntity<List<DataMetaInformation>>

    /**
     * A method to retrieve meta info about a specific data set
     * @param dataId as unique identifier for a specific data set
     * @return the DataMetaInformation for the specified data set
     */
    @Operation(
        summary = "Look up meta info about a specific data set.",
        description = "Meta info about a specific data set registered by Dataland " +
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
    fun getDataMetaInfo(@PathVariable dataId: String): ResponseEntity<DataMetaInformation>
}
