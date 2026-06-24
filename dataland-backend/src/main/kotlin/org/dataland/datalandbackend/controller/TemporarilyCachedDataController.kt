package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.TemporarilyCachedDataApi
import org.dataland.datalandbackend.services.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the controller for delivering and removing temporarily stored data
 * @param dataManager service to manage data storage
 */
@RestController
class TemporarilyCachedDataController(
    @Autowired var dataManager: DataManager,
) : TemporarilyCachedDataApi {
    override fun getReceivedPublicData(dataId: String): ResponseEntity<String> =
        ResponseEntity.ok(dataManager.selectPublicDatasetFromTemporaryStorage(dataId))

    override fun getBatchReceivedPublicData(dataIds: List<String>): ResponseEntity<Map<String, String>> {
        val responseObject = dataIds.associateWith { dataManager.selectRawPublicDatasetFromTemporaryStorage(it) }
        return ResponseEntity.ok(responseObject)
    }
}
