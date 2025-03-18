package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.JsonComparator
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Manages the migration of data from stored datasets to assembled datasets
 */
@Service
class AssembledDataMigrationManager
    @Autowired
    constructor(
        private val dataMetaInformationManager: DataMetaInformationManager,
        private val storedDataManager: DataManager,
        private val assembledDataManager: AssembledDataManager,
        private val objectMapper: ObjectMapper,
        private val dataControllerProviderService: DataControllerProviderService,
        private val messageQueuePublications: MessageQueuePublications,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)
        private val migrationCheckJsonComparisonOptions =
            JsonComparator.JsonComparisonOptions(
                ignoredKeys = setOf("referencedReports", "publicationDate", "fileName"),
                fullyNullObjectsAreEqualToNull = true,
            )

        private fun performMigration(
            dataMetaInfo: DataMetaInformationEntity,
            data: String,
            correlationId: String,
        ) {
            logger.info("Migrating data with dataId: ${dataMetaInfo.dataId} to an assembled dataset (correlationId: $correlationId)")

            val splitDataset = assembledDataManager.splitDatasetIntoDataPoints(data, dataMetaInfo.dataType)
            assembledDataManager.storeDataPointsForDataset(
                uploadedDataset =
                    StorableDataset(
                        uploaderUserId = dataMetaInfo.uploaderUserId,
                        companyId = dataMetaInfo.company.companyId,
                        reportingPeriod = dataMetaInfo.reportingPeriod,
                        dataType = DataType.valueOf(dataMetaInfo.dataType),
                        uploadTime = dataMetaInfo.uploadTime,
                        data = "",
                    ),
                datasetId = dataMetaInfo.dataId,
                correlationId = correlationId,
                dataContent = splitDataset.dataContent,
                fileReferenceToPublicationDateMapping = splitDataset.fileReferenceToPublicationDateMapping,
                fileReferenceToFileNameMapping = splitDataset.fileReferenceToFileNameMapping,
                initialQaStatus = dataMetaInfo.qaStatus,
                initialQaComment = "Status copied from stored dataset during migration.",
            )
        }

        private fun verifyNoDataWasLostDuringMigration(
            dataMetaInfo: DataMetaInformationEntity,
            expectedData: String,
            correlationId: String,
        ) {
            val expectedDataAsJson = objectMapper.readTree(expectedData)
            val assembledDataController =
                dataControllerProviderService.getAssembledDataControllerForFramework(
                    DataType.valueOf(dataMetaInfo.dataType),
                )
            val retrievedData =
                assembledDataController.getCompanyAssociatedData(
                    dataMetaInfo.dataId,
                )

            // objectMapper.valueToTree does not work as it uses inconsistent floating point representation
            val actualDataAsJson = objectMapper.readTree(objectMapper.writeValueAsString(retrievedData.body?.data))

            val differences =
                JsonComparator
                    .compareJson(
                        expectedDataAsJson,
                        actualDataAsJson,
                        migrationCheckJsonComparisonOptions,
                    )
            if (differences.isNotEmpty()) {
                logger.error(
                    "Migration failed for dataId: ${dataMetaInfo.dataId} with correlationId: $correlationId." +
                        "Assembled dataset does not match the expected data. Differences: $differences",
                )
                throw IllegalStateException(
                    "Migration failed for dataId: ${dataMetaInfo.dataId} " +
                        "with correlationId: $correlationId. Assembled dataset does not match the expected data. " +
                        "Differences: $differences",
                )
            }
        }

        /**
         * Migrates the data from stored datasets to assembled datasets
         * @param dataId to migrate
         */
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        fun migrateStoredDatasetToAssembledDataset(dataId: String) {
            val dataMetaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
            val dataPoints = assembledDataManager.getDataPointIdsForDataset(dataId)

            if (dataPoints.isNotEmpty()) {
                throw InvalidInputApiException(
                    summary = "Data already migrated.",
                    message = "Data with dataId '$dataId' has already been migrated to an assembled dataset.",
                )
            }

            val correlationId = IdUtils.generateCorrelationId(dataMetaInfo.company.companyId, dataMetaInfo.dataId)
            val storedDataset =
                storedDataManager.getPublicDataset(
                    dataId = dataMetaInfo.dataId,
                    dataType = DataType.valueOf(dataMetaInfo.dataType),
                    correlationId = correlationId,
                )
            performMigration(dataMetaInfo, storedDataset.data, correlationId)
            verifyNoDataWasLostDuringMigration(dataMetaInfo, storedDataset.data, correlationId)
            messageQueuePublications.publishDatasetMigratedMessage(dataId, correlationId)
        }
    }
