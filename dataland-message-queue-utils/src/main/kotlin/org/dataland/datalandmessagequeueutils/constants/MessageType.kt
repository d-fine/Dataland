package org.dataland.datalandmessagequeueutils.constants

/**
 * The type of content of a RabbitMQ message
 */
object MessageType {
    const val PUBLIC_DATA_RECEIVED = "Public Data received"
    const val DATA_STORED = "Data stored"
    const val DOCUMENT_RECEIVED = "Document received"
    const val DOCUMENT_STORED = "Document stored"
    const val AUTOMATED_QA_COMPLETED = "Automated QA complete"
    const val PERSIST_AUTOMATED_QA_RESULT = "Persist automated QA result"
    const val SEND_INTERNAL_EMAIL = "Send internal E-Mail"
    const val SEND_TEMPLATE_EMAIL = "Send template E-Mail"
    const val PRIVATE_DATA_STORED = "Private Data Stored"
    const val PRIVATE_DATA_RECEIVED = "Private Data received"
    const val QA_STATUS_CHANGED = "QA status changed"
}
