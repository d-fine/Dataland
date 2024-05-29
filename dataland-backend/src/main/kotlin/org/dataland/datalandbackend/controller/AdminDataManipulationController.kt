package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.AdminDataManipulationApi
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.utils.IdUtils.generateCorrelationIdAndLogIt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the controller for removing datasets from the dataland data storage
 * @param dataManager service to manage data storage
 */
@RestController
class AdminDataManipulationController(
    @Autowired var dataManager: DataManager,
) : AdminDataManipulationApi {
    override fun deleteCompanyAssociatedData(dataId: String) {
        val correlationId = generateCorrelationIdAndLogIt(companyId = null, dataId = dataId)
        dataManager.deleteCompanyAssociatedDataByDataId(dataId, correlationId)
    }
}
