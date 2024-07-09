package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.GeneralDataManipulationApi
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.utils.IdUtils.generateCorrelationId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the controller for removing datasets from the dataland data storage
 * @param dataManager service to manage data storage
 */
@RestController
class GeneralDataManipulationController(
    @Autowired var dataManager: DataManager,
) : GeneralDataManipulationApi {
    override fun deleteCompanyAssociatedData(dataId: String) {
        val correlationId = generateCorrelationId(companyId = null, dataId = dataId)
        dataManager.deleteCompanyAssociatedDataByDataId(dataId, correlationId)
    }
}
