package org.dataland.datalandmessagequeueutils.constants

/**
 * Keys to the header values of a RabbitMQ message
 */
object MessageHeaderKey {
    const val Type = "cloudEvents:type"
    const val CorrelationId = "cloudEvents:id"
}
