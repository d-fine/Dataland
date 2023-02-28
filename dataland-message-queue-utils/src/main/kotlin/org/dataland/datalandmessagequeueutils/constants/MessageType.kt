package org.dataland.datalandmessagequeueutils.constants

/**
 * The type of content of a RabbitMQ message
 */
enum class MessageType(val id: String) {
    DataReceived("Data received"),
    DataStored("Data stored"),
    QACompleted("QA completed"),
}
