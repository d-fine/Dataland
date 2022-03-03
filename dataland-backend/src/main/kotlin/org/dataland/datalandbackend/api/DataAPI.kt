package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.skyminderClient.model.ContactInformation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.validation.Valid

/**
 * Defines the restful dataland-backend API regarding data exchange
 */

@RequestMapping("/")
interface DataAPI {
    @Operation(
        summary = "Retrieve list of all existing data.",
        description = "List is composed of identifiers, which in turn contain the name id of the respective data set."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved list of data.")
        ]
    )
    @GetMapping(
        value = ["/data"],
        produces = ["application/json"]
    )
    /**
     * Returns the meta information (id and name) of all currently available data sets
     */
    fun getData(): ResponseEntity<List<DataSetMetaInformation>>

    @Operation(
        summary = "Upload new data set.",
        description = "The uploaded data set is added to the data store, the generated id is returned."
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
    /**
     * A method to store a provided data set via dataland into the data store
     * @param dataSet a set of data to be stored
     * @return meta information of the stored data (id and name)
     */
    fun postData(@Valid @RequestBody dataSet: DataSet): ResponseEntity<DataSetMetaInformation>

    @Operation(
        summary = "Retrieve specific data set from the data store.",
        description = "The data set identified via the provided id is retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data set.")
        ]
    )
    @GetMapping(
        value = ["/data/{id}"],
        produces = ["application/json"]
    )
    /**
     * A method to retrieve a specific data set identified by its id
     * @param id identifier used to uniquely determine the data set in the data store
     * @return the complete data stored under the provided id
     */
    fun getDataSet(@PathVariable("id") id: String): ResponseEntity<DataSet>

    @GetMapping(
        value = ["/data/skyminder/{code}/{name}"],
        produces = ["application/json"]
    )
    /**
     * A method to search for data using the skyminder API using the "/companies" endpoint
     * @param countryCode three-letter ISO country code (e.g. DEU for Germany)
     * @param name string to be used for searching the skyminder
     * @return the list of ContactInformation generated from all responses returned by skyminder API
     */
    fun getDataSkyminderRequest(
        @PathVariable("code") countryCode: String,
        @PathVariable("name") name: String
    ): ResponseEntity<List<ContactInformation>>
}
