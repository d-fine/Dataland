package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.lksg.LksgData
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the Lksg framework endpoints
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/lksg")
@RestController
class LksgDataController(
    @Autowired var myDataManager: DataManager,
    @Autowired var myMetaDataManager: DataMetaInformationManager,
    @Autowired var myObjectMapper: ObjectMapper
) : DataController<LksgData>(
    myDataManager,
    myMetaDataManager,
    myObjectMapper,
    LksgData::class.java
) {
    @Operation(operationId = "getCompanyAssociatedLksgData")
    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<LksgData>> {
        return super.getCompanyAssociatedData(dataId)
    }
    @Operation(operationId = "postCompanyAssociatedLksgData")
    override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<LksgData>):
        ResponseEntity<DataMetaInformation> {
        return super.postCompanyAssociatedData(companyAssociatedData)
    }
}
