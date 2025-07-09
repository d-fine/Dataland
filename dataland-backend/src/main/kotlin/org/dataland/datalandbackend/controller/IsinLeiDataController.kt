package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.IsinLeiDataApi
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackend.services.IsinLeiManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the company data endpoints
 * @param isinLeiManager the ISIN-LEI manager service to handle ISIN-LEI mappings
 */
@RestController
class IsinLeiDataController(
    @Autowired private val isinLeiManager: IsinLeiManager,
) : IsinLeiDataApi {
    override fun putIsinLeiMapping(isinLeiMappingData: List<IsinLeiMappingData>): ResponseEntity<List<IsinLeiMappingData>> {
        isinLeiManager.clearAllMappings()
        isinLeiMappingData.forEach { data ->
            isinLeiManager.putIsinLeiMapping(data.isin, data.lei)
        }
        return ResponseEntity.ok(isinLeiMappingData)
    }
}
