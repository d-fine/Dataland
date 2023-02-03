package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.sfdr.SfdrData
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the Sfdr framework endpoints
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/public/data/sfdr")
@RestController
class SfdrDataController(
    @Autowired var myDataManager: DataManager,
    @Autowired var myMetaDataManager: DataMetaInformationManager,
    @Autowired var myObjectMapper: ObjectMapper,
) : DataController<SfdrData>(
    myDataManager,
    myMetaDataManager,
    myObjectMapper,
    SfdrData::class.java
) {
    @Operation(operationId = "getCompanyAssociatedSfdrData")
    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<SfdrData>> {
        return super.getCompanyAssociatedData(dataId)
    }
    @Operation(operationId = "postCompanyAssociatedSfdrData")
    override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<SfdrData>):
        ResponseEntity<DataMetaInformation> {
        return super.postCompanyAssociatedData(companyAssociatedData)
    }

    @Operation(operationId = "getAllCompanySfdrData")
    override fun getAllCompanyData(companyId: String): ResponseEntity<List<SfdrData>> {
        return super.getAllCompanyData(companyId)
    }
}
