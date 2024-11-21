package org.dataland.datalandmessagequeueutils.constants

/**
 * The type of content of a RabbitMQ message
 */
object MessageType {
    const val PUBLIC_DATA_RECEIVED = "Public Data received"
    const val DATA_STORED = "Data stored"
    const val DOCUMENT_RECEIVED = "Document received"
    const val DOCUMENT_STORED = "Document stored"
    const val QA_COMPLETED = "QA completed"
    const val MANUAL_QA_REQUESTED = "Manual QA requested"
    const val PERSIST_AUTOMATED_QA_RESULT = "Persist automated QA result"
    const val SEND_EMAIL = "Send email"
    const val PRIVATE_DATA_STORED = "Private Data Stored"
    const val PRIVATE_DATA_RECEIVED = "Private Data received"
    const val DATA_NONSOURCEABLE = "Data non-sourceable"
}
