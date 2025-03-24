package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationPatch
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * A Service class used to update dataMetaInformation
 */
@Service
class DataMetaInfoAlterationManager
    @Autowired
    constructor(
        private val dataManager: DataManager,
        private val dataMetaInformationManager: DataMetaInformationManager,
        private val dataPointUtils: DataPointUtils,
        private val keycloakUserService: KeycloakUserService,
        private val messageQueuePublications: MessageQueuePublications,
    ) {
        private val logger = LoggerFactory.getLogger(DataMetaInfoAlterationManager::class.java)

        /**
         * Patch dataMetaInformation for dataset with given [dataId]
         * This function retrieves behaves differently for assembled and non-assembled datasets.
         * For
         * and  the full dataset from either temporary or internal storage, patches it, and then sends a
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

            if (!keycloakUserService.isKeycloakUserId(dataMetaInformationPatch.uploaderUserId)) {
                throw InvalidInputApiException(
                    summary = "KeycloakUserId is invalid.",
                    message = "The uploaderUserId does not belong to a Keycloak user.",
                )
            }

            when (dataPointUtils.getFrameworkSpecificationOrNull(dataMetaInformation.dataType)) {
                null -> {
                    logger.info("Retrieving StorableDataset with dataId $dataId from Storage. CorrelationId: $correlationId.")
                    val updatedStorableDataset: StorableDataset =
                        dataManager
                            .getPublicDataset(dataId, DataType.valueOf(dataMetaInformation.dataType), correlationId)
                            .copy(uploaderUserId = dataMetaInformationPatch.uploaderUserId)

                    logger.info(
                        "Updating uploaderUserId to ${dataMetaInformationPatch.uploaderUserId} in metaInformation. " +
                            "CorrelationId: $correlationId.",
                    )
                    dataMetaInformation.uploaderUserId = dataMetaInformationPatch.uploaderUserId

                    logger.info("Storing updated StorableDataset with dataId $dataId. CorrelationId: $correlationId.")

                    dataManager.storeDatasetInTemporaryStoreAndSendPatchMessage(
                        dataId = dataId,
                        storableDataset = updatedStorableDataset,
                        correlationId = correlationId,
                    )
                }

                else -> {
                    logger.info(
                        "Updating uploaderUserId to ${dataMetaInformationPatch.uploaderUserId} in metaInformation. " +
                            "CorrelationId: $correlationId.",
                    )
                    dataMetaInformation.uploaderUserId = dataMetaInformationPatch.uploaderUserId
                    messageQueuePublications.publishDatasetMetaInfoPatchMessage(
                        dataId,
                        dataMetaInformation.uploaderUserId,
                        correlationId,
                    )
                }
            }

            return dataMetaInformationManager.storeDataMetaInformation(dataMetaInformation)
        }
    }
