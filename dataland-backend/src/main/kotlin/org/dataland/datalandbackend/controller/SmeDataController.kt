package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.PrivateDataApi
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.PrivateDataManager
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

/**
 * Controller for the SME framework endpoints
 * @param privateDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RestController
class SmeDataController(
    @Autowired var privateDataManager: PrivateDataManager,
    @Autowired var myObjectMapper: ObjectMapper,
) : PrivateDataApi {
    private val logger = LoggerFactory.getLogger(javaClass)
 //TODO what is this annotation for?
    // @Operation(operationId = "postCompanyAssociatedSmeData")
    override fun postSmeJsonAndDocuments(
        companyAssociatedSmeData: CompanyAssociatedData<SmeData>,
        documents: Array<MultipartFile>?,
    ): ResponseEntity<DataMetaInformation> {
        companyAssociatedSmeData.companyId
        logger.info("Received MiNaBo data for companyId ${companyAssociatedSmeData.companyId} to be stored.")
        val correlationId = UUID.randomUUID().toString()

        val uploadTime = Instant.now().toEpochMilli()
        val userId = DatalandAuthentication.fromContext().userId
        val storableDataSet = StorableDataSet(
            companyId = companyAssociatedSmeData.companyId,
            dataType = DataType.of(SmeData::class.java),
            uploaderUserId = userId,
            uploadTime = uploadTime,
            reportingPeriod = companyAssociatedSmeData.reportingPeriod,
            data = companyAssociatedSmeData.data.toString(),
        )
        val dataIdOfPostedData = privateDataManager.processPrivateSmeDataStorageRequest(
            storableDataSet,
            documents ?: emptyArray(),
            correlationId,
        )
        val dataMetaInformation = DataMetaInformation(
            dataId = dataIdOfPostedData,
            companyId = companyAssociatedSmeData.companyId,
            dataType = DataType.of(SmeData::class.java),
            uploadTime = uploadTime,
            reportingPeriod = companyAssociatedSmeData.reportingPeriod,
            currentlyActive = true,
            qaStatus = QaStatus.Accepted,
            uploaderUserId = userId,
        )
        return ResponseEntity.ok(dataMetaInformation)
    }
}
