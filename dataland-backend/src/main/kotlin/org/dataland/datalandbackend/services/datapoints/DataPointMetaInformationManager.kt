package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing data meta-information
 */
@Service
class DataPointMetaInformationManager
    @Autowired
    constructor(
        private val dataPointMetaInformationRepositoryInterface: DataPointMetaInformationRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Get meta info about one specific data point
         * @param dataPointId filters the requested meta info to one specific data ID
         * @return meta info about data behind the dataId
         */
        @Transactional(readOnly = true)
        fun getDataPointMetaInformationById(dataPointId: String): DataPointMetaInformationEntity =
            dataPointMetaInformationRepositoryInterface.findById(dataPointId).orElseThrow {
                ResourceNotFoundApiException(
                    "Data point not found",
                    "No data point with the id: $dataPointId could be found in the data store.",
                )
            }

        /**
         * Get meta info about a batch of data points
         * @param dataPointIds filters the requested meta info to one specific data ID
         * @return meta info about data behind the dataId
         */
        @Transactional(readOnly = true)
        fun getDataPointMetaInformationByIds(dataPointIds: List<String>): List<DataPointMetaInformationEntity> =
            dataPointMetaInformationRepositoryInterface.findAllById(dataPointIds)

        /**
         * Get the currently active data id for a specific set of data point dimensions
         * @param dataPointDimensions the data point dimensions to get the currently active data id for
         * @return the id of the currently active data point
         */
        @Transactional(readOnly = true)
        fun getCurrentlyActiveDataId(dataPointDimensions: BasicDataPointDimensions): String? =
            dataPointMetaInformationRepositoryInterface.getActiveDataPointId(dataPointDimensions)

        /**
         * Method to get the data point dimensions from a data id
         * @param dataPointIds the ids of the data point
         * @return the data point dimensions
         */
        @Transactional(readOnly = true)
        fun getDataPointDimensionsFromIds(dataPointIds: List<String>): Map<String, BasicDataPointDimensions> =
            dataPointMetaInformationRepositoryInterface.findAllById(dataPointIds).associate {
                it.dataPointId to
                    BasicDataPointDimensions(
                        reportingPeriod = it.reportingPeriod,
                        companyId = it.companyId,
                        dataPointType = it.dataPointType,
                    )
            }

        /**
         * Method to store the meta information of a data point
         * @param dataPointMetaInformation the meta information to store
         * @return the stored meta information
         */
        @Transactional
        fun storeDataPointMetaInformation(dataPointMetaInformation: DataPointMetaInformationEntity): DataPointMetaInformationEntity =
            dataPointMetaInformationRepositoryInterface.save(dataPointMetaInformation)

        /**
         * Method to update the QA status of multiple data points
         * @param messages the messages containing the data point id and the new QA status
         */
        @Transactional
        fun updateQaStatusOfDataPointsFromMessages(messages: Collection<QaStatusChangeMessage>) {
            val dataPointMetaInformation =
                dataPointMetaInformationRepositoryInterface
                    .findAllById(
                        messages.map {
                            it.dataId
                        },
                    ).associateBy { it.dataPointId }
            for (message in messages) {
                val dataPointId = message.dataId
                val dataPointMetaInformationEntity = dataPointMetaInformation[dataPointId]
                requireNotNull(dataPointMetaInformationEntity) {
                    "Data point with id $dataPointId not found in the data base. This should be impossible."
                }
                dataPointMetaInformationEntity.qaStatus = message.updatedQaStatus
            }
            dataPointMetaInformationRepositoryInterface.saveAll(dataPointMetaInformation.values)
        }

        /**
         * Task for updating the currently active data point for specific data point dimensions
         */
        data class UpdateCurrentlyActiveDataPointTask(
            val dataPointDimensions: BasicDataPointDimensions,
            val newActiveDataId: String?,
            val correlationId: String,
        )

        /**
         * Update the currently active data point for specific data point dimensions in bulk
         */
        @Transactional
        fun updateCurrentlyActiveDataPointBulk(tasks: List<UpdateCurrentlyActiveDataPointTask>) {
            val dataPointDimensions = tasks.map { it.dataPointDimensions }
            require(dataPointDimensions.toSet().size == tasks.size) {
                "The data point dimensions must be unique for each task."
            }
            val allPotentiallyInvolvedEntities =
                dataPointMetaInformationRepositoryInterface.getBulkActiveDataPoints(
                    companyIds = dataPointDimensions.map { it.companyId },
                    dataPointTypes = dataPointDimensions.map { it.dataPointType },
                    reportingPeriods = dataPointDimensions.map { it.reportingPeriod },
                )
            val currentlyActiveEntityByDimension =
                allPotentiallyInvolvedEntities.associateBy {
                    BasicDataPointDimensions(it.companyId, it.dataPointType, it.reportingPeriod)
                }
            val newlyActiveDataEntities =
                dataPointMetaInformationRepositoryInterface
                    .findAllById(tasks.mapNotNull { it.newActiveDataId })
                    .associateBy { it.dataPointId }

            for (task in tasks) {
                logger.info(
                    "Updating currently active data point for ${task.dataPointDimensions} " +
                        "(correlation ID: ${task.correlationId}).",
                )
                val currentlyActive = currentlyActiveEntityByDimension[task.dataPointDimensions]
                val newlyActive = newlyActiveDataEntities[task.newActiveDataId]
                logger
                    .info(
                        "Currently and newly active IDs are ${currentlyActive?.dataPointId} and " +
                            "${task.newActiveDataId} (correlation ID: ${task.correlationId}).",
                    )
                if (currentlyActive?.dataPointId == newlyActive?.dataPointId) {
                    logger.info(
                        "No update of the currently active flag required " +
                            "(correlation ID: ${task.correlationId}).",
                    )
                } else {
                    currentlyActive?.currentlyActive = null
                    newlyActive?.currentlyActive = true
                }
            }
            dataPointMetaInformationRepositoryInterface.saveAll(allPotentiallyInvolvedEntities)
            dataPointMetaInformationRepositoryInterface.saveAll(newlyActiveDataEntities.values)
        }

        /**
         * Method to get the reporting periods with active data points for a specific set of data point types and one company
         * @param dataPointTypes the data point types to filter for
         * @param companyId the company to filter for
         * @return the reporting periods with at least one active data point
         */
        fun getReportingPeriodsWithActiveDataPoints(
            dataPointTypes: Set<String>,
            companyId: String,
        ): Set<String> =
            dataPointMetaInformationRepositoryInterface
                .findByDataPointTypeInAndCompanyIdAndCurrentlyActiveTrue(
                    dataPointTypes = dataPointTypes,
                    companyId = companyId,
                ).map { it.reportingPeriod }
                .toSet()

        /**
         * Method to get the latest upload time of active data points given a set of data point types for a specific company
         * @param dataPointTypes the data point types to filter for
         * @param companyId the company to filter for
         * @return the latest upload time of active data points as a long
         */
        fun getLatestUploadTimeOfActiveDataPoints(
            dataPointTypes: Set<String>,
            companyId: String,
            reportingPeriod: String,
        ): Long =
            dataPointMetaInformationRepositoryInterface
                .findByDataPointTypeInAndCompanyIdAndReportingPeriodAndCurrentlyActiveTrue(
                    dataPointTypes = dataPointTypes,
                    companyId = companyId,
                    reportingPeriod = reportingPeriod,
                ).map { it.uploadTime }
                .maxOf { it }
    }
