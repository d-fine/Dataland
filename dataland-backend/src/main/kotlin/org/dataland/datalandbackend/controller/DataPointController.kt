package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataPointApi
import org.dataland.datalandbackend.services.DataManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**

 */

@RestController
class DataPointController(
    @Autowired val dataManager: DataManager,
) : DataPointApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getDataPoint(dataId: String): ResponseEntity<String> {
        logger.info("Received Request to retrieve data point with ID $dataId.")
        val storedDataPoint = dataManager.getDataPoint(dataId = dataId, correlationId = dataId)
        return ResponseEntity.ok(storedDataPoint.data)
    }
}
