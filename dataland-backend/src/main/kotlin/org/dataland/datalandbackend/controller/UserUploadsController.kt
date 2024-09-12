package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.UserUploadsApi
import org.dataland.datalandbackend.entities.DataMetaInformationForMyDatasets
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Defines the restful dataland-backend API regarding user data exchange
 */
@RestController
class UserUploadsController(
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
) : UserUploadsApi {
    override fun getUserUploadsDataMetaInformation(userId: String):
        ResponseEntity<List<DataMetaInformationForMyDatasets>> {
        return ResponseEntity.ok(dataMetaInformationManager.getUserDataMetaInformation(userId))
    }
}
