package org.dataland.datalandmessagequeueutils.constants

/**
 * The type of content of a RabbitMQ message
 */
enum class MessageType {
    DataReceived,
    DataStored,
    QACompleted,
}
