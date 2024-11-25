package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceableEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.NonSourceableData
import org.dataland.datalandbackend.repositories.NonSourceableDataRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * A service class for managing information about non-sourceable datasets
 */
@Service("NonSourceableDataManager")
class NonSourceableDataManager(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val nonSourceableDataRepository: NonSourceableDataRepository,
) {
    /**
     * The method sets a dataset as non-sourceable in the nonSourceableDataRepository
     * @param nonSourceableInfo the NonSourceableEntity of the dataset
     */
    fun storeNonSourceableData(nonSourceableInfo: NonSourceableData) {
        val creationTime = Instant.now().toEpochMilli()

        val nonSourceableEntity =
            NonSourceableEntity(
                eventId = UUID.randomUUID().toString(),
                companyId = nonSourceableInfo.companyId,
                dataType = nonSourceableInfo.dataType.toString(),
                reportingPeriod = nonSourceableInfo.reportingPeriod,
                nonSourceable = nonSourceableInfo.nonSourceable,
                reason = nonSourceableInfo.reason,
                creationTime = creationTime,
            )

        nonSourceableEntity.nonSourceable = true
        nonSourceableDataRepository.save(nonSourceableEntity)
    }

    /**
     * The method writes a message to a queue about the event of a dataset being labeled as non-sourceable
     * @param nonSourceableInfo the NonSourceableEntity of the dataset
     */
    fun createEventDatasetNonSourceable(
        correlationId: String,
        nonSourceableInfo: NonSourceableData,
    ) {
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
     * The method retrieves non sourceable data sets by filtering for query params
     * @param companyId
     * @param dataType
     * @param reportingPeriod
     */
    fun getNonSourceableDataByTriple(
        companyId: String?,
        dataType: DataType?,
        reportingPeriod: String?,
    ): List<NonSourceableEntity>? {
        val nonSourceableDataSets =
            nonSourceableDataRepository.findByCompanyIdAndDataTypeAndReportingPeriod(
                companyId,
                dataType.toString(),
                reportingPeriod,
            )
        return nonSourceableDataSets
    }
}
