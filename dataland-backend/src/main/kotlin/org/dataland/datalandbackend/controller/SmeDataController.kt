package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.api.PrivateDataApi
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.PrivateDataManager
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

    @Operation(operationId = "postSmeJsonAndDocuments")
    override fun postSmeJsonAndDocuments(
        companyAssociatedSmeData: CompanyAssociatedData<SmeData>,
        documents: Array<MultipartFile>?,
    ): ResponseEntity<DataMetaInformation> {
        val uploadTime = Instant.now().toEpochMilli()
        val correlationId = UUID.randomUUID().toString()
        logger.info(
            "Received MiNaBo data for companyId ${companyAssociatedSmeData.companyId} to be stored. " +
                "Will be processed with correlationId $correlationId",
        )

        val storableDataSet = StorableDataSet(
            companyId = companyAssociatedSmeData.companyId,
            dataType = DataType.of(SmeData::class.java),
            uploaderUserId = DatalandAuthentication.fromContext().userId,
            uploadTime = uploadTime,
            reportingPeriod = companyAssociatedSmeData.reportingPeriod,
            data = companyAssociatedSmeData.data.toString(),
        )

        val dataMetaInformation = privateDataManager.processPrivateSmeDataStorageRequest(
            storableDataSet,
            documents ?: emptyArray(),
            correlationId,
        )

        return ResponseEntity.ok(dataMetaInformation)
    }
}
