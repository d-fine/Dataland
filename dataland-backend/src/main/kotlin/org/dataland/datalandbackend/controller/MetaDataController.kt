package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.MetaDataApi
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the company metadata endpoints
 * @param dataMetaInformationManager service for handling data meta information
 */

@RestController
class MetaDataController(
    @Autowired var dataMetaInformationManager: DataMetaInformationManager,
) : MetaDataApi {

    override fun getListOfDataMetaInfo(companyId: String?, dataType: DataType?):
        ResponseEntity<List<DataMetaInformation>> {
        return ResponseEntity.ok(
            dataMetaInformationManager.searchDataMetaInfo(companyId ?: "", dataType).map { it.toApiModel() }
        )
    }

    override fun getDataMetaInfo(dataId: String): ResponseEntity<DataMetaInformation> {
        return ResponseEntity.ok(dataMetaInformationManager.getDataMetaInformationByDataId(dataId).toApiModel())
    }
}
