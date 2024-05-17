package org.dataland.datalandbackend.controller

import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import org.dataland.datalandbackend.api.AdminDataManipulationApi
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * Implementation of the controller for removing datasets from the dataland data storage
 * @param dataManager service to manage data storage
 */
@RestController
class AdminDataManipulationController(
    @Autowired var dataManager: DataManager,
) : AdminDataManipulationApi {
    private val logMessageBuilder = LogMessageBuilder()
    override fun deleteCompanyAssociatedData(dataId: String) {
        val correlationId = UUID.randomUUID().toString()
        logger.info(logMessageBuilder.generatedCorrelationIdMessage(correlationId, dataId))
        dataManager.deleteCompanyAssociatedDataByDataId(dataId, correlationId)
    }
}
