package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackendutils.model.ExportFileType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API regarding exporting datasets
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
@RequestMapping("/export")
interface DataExportApi {
    /**
     * A method to export the CompanyAssociatedData for a dataId to file of the specified format
     * @param dataId identifier used to uniquely specify a dataset
     * @return
     */
    @Operation(
        summary = "Export data identified by dataId.",
        description = "Export data identified by dataId.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully exported dataset."),
        ],
    )
    @GetMapping(
        value = ["/{format}/{dataId}"],
    )
    fun exportData(
        @PathVariable("format") format: ExportFileType,
        @PathVariable("dataId") dataId: String,
    ): ResponseEntity<String>

//    /**
//     * A method to export the CompanyAssociatedData for a dataId to JSON
//     * @param dataId identifier used to uniquely specify a dataset
//     * @return
//     */
//    @Operation(
//        summary = "Export data identified by dataId to JSON.",
//        description = "Export data identified by dataId to JSON.",
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(responseCode = "200", description = "Successfully exported dataset."),
//        ],
//    )
//    @GetMapping(
//        produces = ["application/json"],
//        value = ["/json/{dataId}"],
//    )
//    fun exportDataToJson(
//        @PathVariable("dataId") dataId: String
//    ): ResponseEntity<ByteArray>
//
//    /**
//     * A method to export the CompanyAssociatedData for a dataId to CSV
//     * @param dataId identifier used to uniquely specify a dataset
//     * @return
//     */
//    @Operation(
//        summary = "Export data identified by dataId to CSV.",
//        description = "Export data identified by dataId to CSV.",
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(responseCode = "200", description = "Successfully exported dataset."),
//        ],
//    )
//    @GetMapping(
//        produces = ["text/csv"],
//        value = ["/csv/{dataId}"],
//    )
//    fun exportDataToCsv(
//        @PathVariable("dataId") dataId: String,
//    )
}
