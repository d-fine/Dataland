package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.api.SmeDataApi
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.PrivateDataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * Controller for the SME framework endpoints
 * @param privateDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RestController
class SmeDataController(
    @Autowired var privateDataManager: PrivateDataManager,
    @Autowired var myObjectMapper: ObjectMapper,
) : SmeDataApi {

    @Operation(operationId = "postSmeJsonAndDocuments")
    override fun postSmeJsonAndDocuments(
        companyAssociatedSmeData: CompanyAssociatedData<SmeData>,
        documents: Array<MultipartFile>?,
    ): ResponseEntity<DataMetaInformation> {
        val dataMetaInformation = privateDataManager.processPrivateSmeDataStorageRequest(
            companyAssociatedSmeData,
            documents,
        )
        return ResponseEntity.ok(dataMetaInformation)
    }
}
