package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceableEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfo
import org.dataland.datalandbackend.repositories.NonSourceableDataRepository
import org.dataland.datalandbackend.repositories.utils.NonSourceableDataSearchFilter
import org.dataland.datalandbackend.utils.IdUtils.generateCorrelationId
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * A service class for managing information about the sourceabilty of data sets.
 */
@Service("NonSourceableDataManager")
class NonSourceableDataManager(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val nonSourceableDataRepository: NonSourceableDataRepository,
) {
    /**
     * The method stores a non-sourceable dataset in the nonSourceableDataRepository
     * @param nonSourceableInfo the of the dataset
     */
    fun storeNonSourceableData(nonSourceableInfo: NonSourceableInfo) {
        val creationTime = Instant.now().toEpochMilli()
        val nonSourceableEntity =
            NonSourceableEntity(
                eventId = null,
                companyId = nonSourceableInfo.companyId,
                dataType = nonSourceableInfo.dataType,
                reportingPeriod = nonSourceableInfo.reportingPeriod,
                isNonSourceable = nonSourceableInfo.isNonSourceable,
                reason = nonSourceableInfo.reason,
                creationTime = creationTime,
            )
        nonSourceableDataRepository.save(nonSourceableEntity)
    }

    /**
     * The method writes a message to a queue about the event of a dataset being labeled as non-sourceable
     * @param nonSourceableInfo the NonSourceableEntity of the dataset
     */
    fun createEventDatasetNonSourceable(nonSourceableInfo: NonSourceableInfo) {
        val correlationId = generateCorrelationId(nonSourceableInfo.companyId, null)

        storeNonSourceableData(nonSourceableInfo)
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(nonSourceableInfo),
            type = MessageType.DATA_NONSOURCEABLE,
            correlationId = correlationId,
            exchange = ExchangeName.DATA_NONSOURCEABLE,
            routingKey = RoutingKeyNames.DATA_NONSOURCEABLE,
        )
    }

    /**
     * The method retrieves non sourceable data sets by given filters.
     * @param companyId if not empty, it filters the requested info to a specific company
     * @param dataType if not empty, it filters the requested info to a specific data type
     * @param reportingPeriod if not empty, it filters the requested info to a specific reporting period
     */
    fun getNonSourceableDataByFilters(
        eventId: UUID?,
        companyId: String?,
        dataType: DataType?,
        reportingPeriod: String?,
        nonSourceable: Boolean?,
    ): List<NonSourceableEntity>? {
        val nonSourceableDataSets =
            nonSourceableDataRepository.searchNonSourceableData(
                NonSourceableDataSearchFilter(
                    eventId,
                    companyId,
                    dataType,
                    reportingPeriod,
                    nonSourceable,
                ),
            )
        return nonSourceableDataSets
    }

    /**
     * The method throws an exception if a specific data set is sourceable or not found.
     * @param companyId filters for the specific company
     * @param dataType filters for the specific data type
     * @param reportingPeriod filters for the specific reporting period
     */

    fun responseDataNonSourceable(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ) {
        if (isDataNonSourceable(companyId, dataType, reportingPeriod) != true) {
            throw ResourceNotFoundApiException(
                summary = "Dataset is sourceable or not found.",
                message =
                    "No non-sourceable dataset found for company $companyId, dataType $dataType, " +
                        "and reportingPeriod $reportingPeriod.",
            )
        }
    }

    /**
     * The method checks if a specific data set is non-sourceable.
     * @param companyId filters for the specific company
     * @param dataType filters for the specific data type
     * @param reportingPeriod filters for the specific reporting period
     */
    fun isDataNonSourceable(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ): Boolean? {
        NonSourceableDataSearchFilter(null, companyId, dataType, reportingPeriod, null)
        val latestNonSourceableEntity =
            nonSourceableDataRepository.getLatestNonSourceableData(
                NonSourceableDataSearchFilter(
                    null,
                    companyId,
                    dataType,
                    reportingPeriod,
                    null,
                ),
            )
        if (latestNonSourceableEntity != null) {
            return latestNonSourceableEntity.isNonSourceable
        }
        return null
    }

    /**
     * The method stores a sourceable dataset in the nonSourceableDataRepository
     * @param nonSourceableInfo the of the dataset
     */
    fun storeSourceableData(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
        uploaderId: String,
    ) {
        val creationTime = Instant.now().toEpochMilli()

        if (isDataNonSourceable(companyId, dataType, reportingPeriod) == true) {
            val nonSourceableEntity =
                NonSourceableEntity(
                    eventId = null,
                    companyId = companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    isNonSourceable = false,
                    reason = "Uploaded by a user with the Id:$uploaderId",
                    creationTime = creationTime,
                )
            nonSourceableDataRepository.save(nonSourceableEntity)
        }
    }
}
