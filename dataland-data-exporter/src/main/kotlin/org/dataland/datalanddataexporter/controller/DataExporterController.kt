package org.dataland.datalanddataexporter.controller

import org.dataland.datalanddataexporter.api.DataExporterAPI
import org.dataland.datalanddataexporter.services.CsvExporter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the data exporter
 */

@RestController
class DataExporterController(
    @Autowired private val csvExporter: CsvExporter,
) : DataExporterAPI {

    override fun respond(): ResponseEntity<String> {
        return ResponseEntity.ok(csvExporter.dummyFunction())
    }
}
