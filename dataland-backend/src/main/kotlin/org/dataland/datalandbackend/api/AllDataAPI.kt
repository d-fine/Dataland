package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RequestMapping("/data")
interface AllDataAPI {

    @Operation(
        summary = "Search for data meta data on Dataland.",
        description = "Meta info about data sets that are registered by Dataland can be retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved meta data.")
        ]
    )
    @GetMapping(
        value = [""],
        produces = ["application/json"]
    )
    /**
     * A method to search for meta data on all data sets registered by Dataland
     * @param dataId as unique identifier for a specific data set
     * @param dataType filters the requested meta data to a specific data type.
     * @return the ID of the created entry in the data store
     */
    fun getData(@RequestParam dataId: String? = null, @RequestParam dataType: String? = null):
        List<DataSetMetaInformation>
}
