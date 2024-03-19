package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.PrivateDataApi
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.PrivateDataManager
import org.dataland.datalandbackendutils.model.QaStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.*

/**
 * Controller for the SME framework endpoints
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RestController
class SmeDataController(
    @Autowired var myDataManager: PrivateDataManager,
    @Autowired var myObjectMapper: ObjectMapper,
) : PrivateDataApi {

    // @Operation(operationId = "postCompanyAssociatedSmeData")
    override fun postSmeJsonAndDocuments(
        companyAssociatedSmeData: CompanyAssociatedData<SmeData>,
        documents: Array<MultipartFile>,
    ):
        ResponseEntity<DataMetaInformation> {
        val correlationId = UUID.randomUUID().toString()
        myDataManager.storePrivateData("Hi", "Hey", correlationId)
        val dummyResponse = DataMetaInformation(
            dataId = "hi",
            companyId = "hey",
            dataType = DataType.of(SmeData::class.java),
            uploadTime = 0,
            reportingPeriod = "2023",
            currentlyActive = true,
            qaStatus = QaStatus.Accepted,
        )
        return ResponseEntity.ok(dummyResponse)
    }
}
