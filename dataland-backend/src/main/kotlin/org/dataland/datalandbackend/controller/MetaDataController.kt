package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.MetaDataApi
import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
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
    @Autowired var dataMetaInformationManager: DataMetaInformationManagerInterface,
) : MetaDataApi {

    override fun getListOfDataMetaInfo(companyId: String?, dataType: DataType?):
        ResponseEntity<List<DataMetaInformation>> {
        return ResponseEntity.ok(dataMetaInformationManager.searchDataMetaInfo(companyId ?: "", dataType).map { it.toApiModel() })
    }

    override fun getDataMetaInfo(dataId: String): ResponseEntity<DataMetaInformation> {
        return ResponseEntity.ok(dataMetaInformationManager.getDataMetaInformationByDataId(dataId).toApiModel())
    }
}
