package org.dataland.datalandmessagequeueutils.constants

/**
 * The type of content of a RabbitMQ message
 */
object MessageType {
    const val DataReceived = "Data received"
    const val DataStored = "Data stored"
    const val DocumentReceived = "Document received"
    const val DocumentStored = "Document stored"
    const val QACompleted = "QA completed"
}
