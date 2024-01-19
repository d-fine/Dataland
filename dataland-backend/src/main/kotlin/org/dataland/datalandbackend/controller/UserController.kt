package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.UserApi
import org.dataland.datalandbackend.entities.DataMetaInformationForMyDatasets
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Defines the restful dataland-backend API regarding data exchange
 */
@RestController
class UserController(
    @Autowired val dataMetaInformationManager: DataMetaInformationManager
) : UserApi {
    override fun getUserDataMetaInformation(userId: String): ResponseEntity<List<DataMetaInformationForMyDatasets>> {
        return ResponseEntity.ok(dataMetaInformationManager.getUserDataMetaInformation(userId))
    }
}
