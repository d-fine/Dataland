package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataAndMetaInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.sme.SmeData
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the SME framework endpoints
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/sme")
@RestController
class SmeDataController(
    @Autowired var myDataManager: DataManager,
    @Autowired var myMetaDataManager: DataMetaInformationManager,
    @Autowired var myObjectMapper: ObjectMapper
) : DataController<SmeData>(
    myDataManager,
    myMetaDataManager,
    myObjectMapper,
    SmeData::class.java
) {
    @Operation(operationId = "getCompanyAssociatedSmeData")
    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<SmeData>> {
        return super.getCompanyAssociatedData(dataId)
    }
    @Operation(operationId = "postCompanyAssociatedSmeData")
    override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<SmeData>):
        ResponseEntity<DataMetaInformation> {
        return super.postCompanyAssociatedData(companyAssociatedData)
    }

    @Operation(operationId = "getAllCompanySmeData")
    override fun getAllCompanyData(companyId: String): ResponseEntity<List<DataAndMetaInformation<SmeData>>> {
        return super.getAllCompanyData(companyId)
    }
}
