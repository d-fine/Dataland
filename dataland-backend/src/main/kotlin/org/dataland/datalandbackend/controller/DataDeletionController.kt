package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataDeletionApi
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.utils.IdUtils.generateCorrelationId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the controller for removing datasets from the dataland data storage
 * @param dataManager service to manage data storage
 */
@RestController
class DataDeletionController(
    @Autowired var dataManager: DataManager,
) : DataDeletionApi {
    override fun deleteCompanyAssociatedData(dataId: String) {
        val correlationId = generateCorrelationId(companyId = null, dataId = dataId)
        dataManager.deleteCompanyAssociatedDataByDataId(dataId, correlationId)
    }
}
