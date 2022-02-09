package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.Identifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RequestMapping("/")
interface DataAPI {
    @Operation(
        summary = "Retrieve list of all existing data.",
        description = "List is composed of identifiers, which in turn contain the name and the id of the respective data set."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved list of data.")
        ]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/data"],
        produces = ["application/json"]
    )
    fun getData(): ResponseEntity<List<Identifier>> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    @Operation(
        summary = "Upload new data set.",
        description = "The uploaded data set is added to the data store."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data set to the data store.")
        ]
    )
    @PostMapping(
        value = ["/data"],
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    fun postData(@Valid @RequestBody dataSet: DataSet): ResponseEntity<String> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    @Operation(
        summary = "Retrieve specific data set from the data store.",
        description = "The data set identified via the provided id is retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data set.")
        ]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/data/{id}"],
        produces = ["application/json"]
    )
    fun getDataSet(@PathVariable("id") id: String): ResponseEntity<DataSet> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }
}