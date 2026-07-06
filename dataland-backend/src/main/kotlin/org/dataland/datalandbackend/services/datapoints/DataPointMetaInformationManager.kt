package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
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
        fun getDataPointMetaInformationByIds(dataPointIds: Collection<String>): List<DataPointMetaInformationEntity> =
            dataPointMetaInformationRepositoryInterface.findAllById(dataPointIds)

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
            val allPotentiallyInvolvedEntities = getActiveDataPointMetaInformationList(dataPointDimensions)
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
                        "(correlation ID: ${task.correlationId}) and (datapoint ID: ${task.newActiveDataId}).",
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

        /**
         * Retrieves active data point metadata entities matching the given filter criteria.
         * An empty list for any parameter means "match all" (wildcard).
         *
         * @param dataDimensionQuery filter specifying what to search for
         * @return list of DataPointMetaInformationEntity for active data points matching the filters
         */
        @Transactional(readOnly = true)
        fun getActiveDataPointMetaInformationList(dataDimensionQuery: DataDimensionQuery): List<DataPointMetaInformationEntity> =
            if (dataDimensionQuery.isEmpty()) {
                emptyList()
            } else {
                dataPointMetaInformationRepositoryInterface
                    .findActiveDataPointDimensionsByFilter(
                        defaultObjectMapper.writeValueAsString(dataDimensionQuery.companyIds),
                        defaultObjectMapper.writeValueAsString(dataDimensionQuery.dataTypes),
                        defaultObjectMapper.writeValueAsString(dataDimensionQuery.reportingPeriods),
                    )
            }

        /**
         * Retrieves active data point metadata for the given exact list of data point dimensions.
         *
         * @param dataDimensions the data point dimensions to look up
         * @return list of matching active DataPointMetaInformationEntity objects
         */
        @Transactional(readOnly = true)
        fun getActiveDataPointMetaInformationList(dataDimensions: List<DataPointDimensions>): List<DataPointMetaInformationEntity> {
            if (dataDimensions.isEmpty()) return emptyList()
            val jsonPayload =
                defaultObjectMapper.writeValueAsString(
                    dataDimensions.map {
                        mapOf(
                            "company_id" to it.companyId,
                            "data_point_type" to it.dataPointType,
                            "reporting_period" to it.reportingPeriod,
                        )
                    },
                )
            return dataPointMetaInformationRepositoryInterface.findActiveDataPointsByDimensionsJson(jsonPayload)
        }
    }
