package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.NonPersistedDataApi
import org.dataland.datalandbackend.services.NonPersistedDataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Abstract implementation of the controller for data exchange of an abstract type T
 * @param nonPersistedDataManager service to handle temporarily stored data
 */
@RestController
class NonPersistedDataController(
    @Autowired var nonPersistedDataManager: NonPersistedDataManager,
) : NonPersistedDataApi {

    override fun getCompanyAssociatedDataForInternalStorage(dataId: String): ResponseEntity<String>{
        return ResponseEntity.ok(nonPersistedDataManager.selectDataSetForInternalStorage(dataId))
    }
}
