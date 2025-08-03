package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.IsinLeiDataApi
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackend.services.IsinLeiManager
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
    override fun putIsinLeiMapping(isinLeiMappingData: List<IsinLeiMappingData>): ResponseEntity<Map<String?, String?>?> {
        isinLeiManager.putIsinLeiMapping(isinLeiMappingData)
        return ResponseEntity.ok(Collections.singletonMap("message", "Successfully received ISIN-LEI mapping data"))
    }

    override fun getIsinsByLei(lei: String): ResponseEntity<List<String>> = ResponseEntity.ok(isinLeiManager.getIsinsByLei(lei))
}
