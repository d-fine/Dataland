package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationPatch
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataMetaInfoAlterationManager @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val dataMetaInformationManager: DataMetaInformationManager,
    private val dataManager: DataManager,
    private val messageQueuePublications: MessageQueuePublications,
    private val keycloakUserService: KeycloakUserService,
) {

    private val logger = LoggerFactory.getLogger(DataMetaInfoAlterationManager::class.java)

    /**
     * Patch dataMetaInformation for dataset with given [dataId]
     * This function retrieves the full dataset from either temporary or internal storage, patches it, and then sends a
     * message to the internal storage to persist the changed dataset.
     * Currently, only public datasets are supported.
     */
    @Transactional
    fun patchDataMetaInformation(
        dataId: String,
        dataMetaInformationPatch: DataMetaInformationPatch,
        correlationId: String,
    ): DataMetaInformationEntity {
        val dataMetaInformation: DataMetaInformationEntity =
            dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
//        if (!dataManager.isDataSetPublic(dataId)) throw InvalidInputApiException(
//            summary = "Not a public dataset.",
//            message = "The provided dataId does not belong to a public dataset. Patching of meta information is only " +
//                    "supported for public dataset."
//        )

        val uploaderUserId = dataMetaInformationPatch.uploaderUserId?.let {
            keycloakUserService.getUser(it).userId.isEmpty()
        }?: throw InvalidInputApiException(
            summary = "UploaderUserId invalid.",
            message = "The provided uploaderUserId could not be found."
        )
        logger.info("Updating uploaderUserId to $uploaderUserId")


        val storableDataSet: StorableDataSet =
            dataManager.getPublicDataSet(dataId, DataType.valueOf(dataMetaInformation.dataType), correlationId)

//        messageQueuePublications.publishDataMetaInfoPatchMessage(
//            dataId,
//            dataMetaInformationPatch,
//            correlationId,
//        )
//        dataMetaInformationPatch.uploaderUserId?.let { dataMetaInformationEntity.uploaderUserId = it }
//        return dataMetaInformationRepository.save(dataMetaInformationEntity)
        return dataMetaInformation
    }
}
