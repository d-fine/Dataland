package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.MetaDataApi
import org.dataland.datalandbackend.interfaces.DataProcessorInterface
import org.dataland.datalandbackend.model.DataMetaInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for company data exchange
 * @param dataManager implementation of the DataManagerInterface that defines how
 * Dataland handles data
 */

@RestController
class MetaDataController(
    @Autowired var dataManager: DataProcessorInterface,
) : MetaDataApi {

    override fun getListOfDataMetaInfo(companyId: String?, dataType: String?):
        ResponseEntity<List<DataMetaInformation>> {
        return ResponseEntity.ok(
            dataManager.searchDataMetaInfo(
                companyId = companyId ?: "", dataType = dataType ?: ""
            )
        )
    }

    override fun getDataMetaInfo(dataId: String): ResponseEntity<List<DataMetaInformation>> {
        return ResponseEntity.ok(dataManager.searchDataMetaInfo(dataId = dataId))
    }
}
