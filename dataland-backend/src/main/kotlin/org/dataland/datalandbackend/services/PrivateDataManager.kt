package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataDocumentMappingEntity
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.repositories.DataDocumentsMappingRepository
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param companyManager service for managing company data
 * @param metaDataManager service for managing metadata
 * @param storageClient service for managing data
 * @param cloudEventMessageHandler service for managing CloudEvents messages
*/
@Component("PrivateDataManager")
class PrivateDataManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyQueryManager: CompanyQueryManager,
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val storageClient: StorageControllerApi,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val messageUtils: MessageQueueUtils,
    @Autowired private val dataDocumentsMappingRepositoryInterface: DataDocumentsMappingRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dataInMemoryStorage = mutableMapOf<String, String>()

    fun storePrivateData(
        companyAssociatedSmeData: CompanyAssociatedData<SmeData>,
        documents: Array<MultipartFile>,
        correlationId: String,
    ) {
        logger.info(
            " " + // TODO Emanuel: check later for injection of logger
                "Correlation ID: $correlationId",
        )
        // val dataDocumentMappingPair = DataDocumentMappingEntity(dataId, documentId) TODO
        // storeDataDocumentMapping(dataDocumentMappingPair) TODO
    }

    /**
     * Method to associate data information with a specific company
     * @param dataDocumentMapping The data meta information which should be stored
     */
    fun storeDataDocumentMapping(
        dataDocumentMapping: DataDocumentMappingEntity,
    ): DataDocumentMappingEntity {
        return dataDocumentsMappingRepositoryInterface.save(dataDocumentMapping)
    }
}
