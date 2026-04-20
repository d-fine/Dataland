package org.dataland.datalandmessagequeueutils.constants

/**
 * The type of content of a RabbitMQ message
 */
object MessageType {
    const val PUBLIC_DATA_RECEIVED = "Public Data received"
    const val DELETE_DATA = "Delete Data"
    const val DATA_STORED = "Data stored"
    const val DATA_MIGRATED = "Data migrated"
    const val DOCUMENT_RECEIVED = "Document received"
    const val DOCUMENT_STORED = "Document stored"
    const val METAINFO_UPDATED = "DataMetaInformation updated"
    const val QA_REQUESTED = "QA requested"
    const val SEND_EMAIL = "Send email"
    const val PRIVATE_DATA_STORED = "Private Data Stored"
    const val PRIVATE_DATA_RECEIVED = "Private Data received"
    const val QA_STATUS_UPDATED = "QA status updated"
    const val LEGACY_DATA_SOURCING_NON_SOURCEABLE = "DataSourcing non-sourceable" // This is for user notifications, maybe change process
    const val NON_SOURCEABILITY_CREATED = "NonSourceability created"
    const val NON_SOURCEABILITY_AUTO_ACCEPTED = "NonSourceability auto-accepted"
    const val NON_SOURCEABILITY_QA_ACCEPTED = "NonSourceability QA accepted"
    const val NON_SOURCEABILITY_QA_REJECTED = "NonSourceability QA rejected"
    const val PORTFOLIO_UPDATE = "portfolio.update"
    const val REQUEST_SET_TO_PROCESSING = "Request set to processing"
    const val REQUEST_SET_TO_WITHDRAWN = "Request set to withdrawn"
}
