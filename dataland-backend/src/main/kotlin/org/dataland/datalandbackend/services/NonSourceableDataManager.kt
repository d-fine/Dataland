package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceableEntity
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * A service class for managing information about non-sourceable datasets
 */
@Service("NonSourceableDataManager")
class NonSourceableDataManager(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    /**
     * The method sets a dataset as non-sourceable in the nonSourceableDataRepository
     * @param nonSourceableInfo the NonSourceableEntity of the dataset
     */
    fun setDatasetAsNonSourceable(nonSourceableInfo: NonSourceableEntity) {
        nonSourceableInfo.nonSourceable = true

        // send message to community manager
        createEventDatasetNonSourceable(nonSourceableInfo)
    }

    /**
     * The method writes a message to a queue about the event of a dataset being labeled as non-sourceable
     * @param nonSourceableInfo the NonSourceableEntity of the dataset
     */
    fun createEventDatasetNonSourceable(nonSourceableInfo: NonSourceableEntity) {
        val correlationId = UUID.randomUUID().toString()

        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(nonSourceableInfo),
            MessageType.DATA_NONSOURCEABLE,
            correlationId,
            ExchangeName.DATA_NONSOURCEABLE,
            RoutingKeyNames.DATA_NONSOURCEABLE,
        )
    }
}
