package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceableEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.NonSourceableDataRepository
import org.dataland.datalandbackend.utils.IdUtils.generateCorrelationId
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

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
    fun setDatasetAsNonSourceable(nonSourceableInfo: NonSourceableEntity) {
        nonSourceableInfo.nonSourceable = true
        nonSourceableDataRepository.save(nonSourceableInfo)
        val correlationId = generateCorrelationId(nonSourceableInfo.companyId, null)
        createEventDatasetNonSourceable(correlationId, nonSourceableInfo)
    }

    /**
     * The method writes a message to a queue about the event of a dataset being labeled as non-sourceable
     * @param nonSourceableInfo the NonSourceableEntity of the dataset
     */
    fun createEventDatasetNonSourceable(
        correlationId: String,
        nonSourceableInfo: NonSourceableEntity,
    ) {
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(nonSourceableInfo),
            correlationId,
            MessageType.DATA_NONSOURCEABLE,
            ExchangeName.DATA_NONSOURCEABLE,
            RoutingKeyNames.DATA_NONSOURCEABLE,
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
