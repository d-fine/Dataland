package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandbackend.DATA_READER
import org.dataland.datalandbackend.DATA_UPLOADER
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.enums.StockIndex
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigDecimal

/**
 * Defines the restful dataland-backend API regarding meta data searches.
 */

@RequestMapping("/metadata")
interface MetaDataApi {

    @Operation(
        summary = "Search in Dataland for meta info about data.",
        description = "Meta info about data sets registered by Dataland can be retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved meta info.")
        ]
    )
    @GetMapping(
        produces = ["application/json"]
    )
    @Secured(DATA_READER)
    /**
     * A method to search for meta info about data sets registered by Dataland
     * @param companyId filters the requested meta info to a specific company.
     * @param dataType filters the requested meta info to a specific data type.
     * @return a list of matching DataMetaInformation
     */
    fun getListOfDataMetaInfo(@RequestParam companyId: String? = null, @RequestParam dataType: String? = null):
        ResponseEntity<List<DataMetaInformation>>

    @Operation(
        summary = "Look up meta info about a specific data set.",
        description = "Meta info about a specific data set registered by Dataland " +
            "and identified by its data ID is retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved specific meta info.")
        ]
    )
    @GetMapping(
        value = ["/{dataId}"],
        produces = ["application/json"]
    )
    @Secured(DATA_READER)
    /**
     * A method to retrieve meta info about a specific data set
     * @param dataId as unique identifier for a specific data set
     * @return the DataMetaInformation for the specified data set
     */
    fun getDataMetaInfo(@PathVariable dataId: String): ResponseEntity<DataMetaInformation>

    @Operation(
        summary = "Look up the green asset ratio according to EU taxonomy.",
        description = "The green asset ratio of a single or all indices in Dataland is returned."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved green asset ratio.")
        ]
    )
    @GetMapping(
        value = ["/greenAssetRatio"],
        produces = ["application/json"]
    )
    @Secured(DATA_READER)
    /**
     * A method to retrieve the green asset ratio of a specific index or for all indices (if none is selected)
     * @param selectedIndex determines which index the green asset ratio is retrieved for
     * @return a map of indices and the corresponding green asset ratios
     */
    fun getGreenAssetRatio(@RequestParam selectedIndex: StockIndex? = null):
        ResponseEntity<Map<StockIndex, BigDecimal>>
}
