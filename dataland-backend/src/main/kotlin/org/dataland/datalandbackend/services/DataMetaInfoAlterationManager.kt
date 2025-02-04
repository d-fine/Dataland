package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationPatch
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
        private val dataMetaInformationManager: DataMetaInformationManager,
        private val dataManager: DataManager,
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

            logger.info("Retrieving StorableDataset with dataId $dataId from Storage. CorrelationId: $correlationId.")
            val storableDataset: StorableDataset =
                dataManager.getPublicDataset(dataId, DataType.valueOf(dataMetaInformation.dataType), correlationId)

            if (dataMetaInformationPatch.uploaderUserId == null ||
                !keycloakUserService.isKeycloakUserId(
                    dataMetaInformationPatch.uploaderUserId,
                )
            ) {
                throw InvalidInputApiException(
                    summary = "KeycloakUserId is invalid.",
                    message = "The uploaderUserId does not belong to a Keycloak user.",
                )
            }

            logger.info("Updating uploaderUserId to ${dataMetaInformationPatch.uploaderUserId}")
            dataMetaInformation.uploaderUserId = dataMetaInformationPatch.uploaderUserId

            dataMetaInformationManager.storeDataMetaInformation(dataMetaInformation)

            logger.info("Updating MetaInformation within StorableDataset with dataId $dataId. CorrelationId: $correlationId.")
            this.updateStorableDatasetFromMetaInfo(storableDataset, dataMetaInformationPatch)

            dataManager.storeDatasetInTemporaryStoreAndSendPatchMessage(
                dataId = dataId,
                storableDataset = storableDataset,
                correlationId = correlationId,
            )
            return dataMetaInformation
        }

        /**
         * Update MetaInformation within StorableDataset
         */
        private fun updateStorableDatasetFromMetaInfo(
            storableDataset: StorableDataset,
            dataMetaInformationPatch: DataMetaInformationPatch,
        ): StorableDataset {
            dataMetaInformationPatch.uploaderUserId?.let {
                storableDataset.uploaderUserId = it
            }
            return storableDataset
        }
    }
