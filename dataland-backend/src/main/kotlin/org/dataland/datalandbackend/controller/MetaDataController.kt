package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.MetaDataApi
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

/**
 * Implementation of the API for company data exchange
 * @param dataManager implementation of the DataManagerInterface that defines how
 * Dataland handles data
 */

@RestController
class MetaDataController(
    @Autowired var dataManager: DataManagerInterface,
) : MetaDataApi {

    override fun getListOfDataMetaInfo(companyId: String?, dataType: String?):
        ResponseEntity<List<DataMetaInformation>> {
        return ResponseEntity.ok(dataManager.searchDataMetaInfo(companyId ?: "", dataType ?: ""))
    }

    override fun getDataMetaInfo(dataId: String): ResponseEntity<DataMetaInformation> {
        return ResponseEntity.ok(dataManager.getDataMetaInfo(dataId))
    }

    override fun getGreenAssetRatio(selectedIndex: CompanyInformation.StockIndex?):
        ResponseEntity<Map<CompanyInformation.StockIndex, BigDecimal>> {
        return ResponseEntity.ok(dataManager.getGreenAssetRatio(selectedIndex))
    }
}
