package org.dataland.datalandmessagequeueutils.constants

/**
 * Keys to the header values of a RabbitMQ message
 */
object MessageHeaderKey {
    const val TYPE = "cloudEvents:type"
    const val CORRELATION_ID = "cloudEvents:id"
}
