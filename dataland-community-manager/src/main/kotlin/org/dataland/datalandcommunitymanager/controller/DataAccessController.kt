package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandcommunitymanager.api.DataAccessApi
import org.dataland.datalandcommunitymanager.services.DataAccessManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the data access endpoints
 * @param dataAccessManager service for all operations concerning data access
 */
@RestController
class DataAccessController(
    @Autowired private val dataAccessManager: DataAccessManager,
) : DataAccessApi {
    override fun hasAccessToDataset(
        companyId: UUID,
        dataType: String,
        reportingPeriod: String,
        userId: UUID,
    ) {
        dataAccessManager.hasAccessToDataset(companyId.toString(), reportingPeriod, dataType, userId.toString())
    }
}
