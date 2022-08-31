package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.MetaDataApi
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
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
    @Autowired var dataManager: DataManagerInterface,
) : MetaDataApi {

    override fun getListOfDataMetaInfo(companyId: String?, dataType: DataType?):
        ResponseEntity<List<DataMetaInformation>> {
        return ResponseEntity.ok(dataManager.searchDataMetaInfo(companyId ?: "", dataType))
    }

    override fun getDataMetaInfo(dataId: String): ResponseEntity<DataMetaInformation> {
        return ResponseEntity.ok(dataManager.getDataMetaInfo(dataId))
    }
}
