package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.IsinLeiDataApi
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackend.services.IsinLeiManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.Collections

/**
 * Controller for the company data endpoints
 * @param isinLeiManager the ISIN-LEI manager service to handle ISIN-LEI mappings
 */
@RestController
class IsinLeiDataController(
    @Autowired private val isinLeiManager: IsinLeiManager,
) : IsinLeiDataApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun putIsinLeiMapping(isinLeiMappingData: List<IsinLeiMappingData>): ResponseEntity<Map<String?, String?>?> {
        logger.info("Start dropping previous entries")
        isinLeiManager.clearAllMappings()
        logger.info("Dropped previous entries")
        logger.info("Preparing to add new ISIN-LEI mappings: ${isinLeiMappingData.size} entries")
        val entities = isinLeiMappingData.map { it.toIsinLeiEntity() }
        isinLeiManager.saveAllJdbcBatchCallable(entities)
        return ResponseEntity.ok(Collections.singletonMap("message", "Successfully received ISIN-LEI mapping data"))
    }
}
