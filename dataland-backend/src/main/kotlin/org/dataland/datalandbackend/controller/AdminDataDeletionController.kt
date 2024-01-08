package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.AdminDataDeletionApi
import org.dataland.datalandbackend.services.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the controller for deleting stored data
 * @param dataManager service to manage data storage
 */
@RestController
class AdminDataDeletionController(
    @Autowired var dataManager: DataManager,
) : AdminDataDeletionApi {

    override fun deleteDataSet(dataId: String): ResponseEntity<String> {
        return ResponseEntity.ok(dataManager.deleteDataFromStorageService(dataId))
    }
}
