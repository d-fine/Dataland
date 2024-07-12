package org.dataland.datalandmessagequeueutils.constants

/**
 * The type of content of a RabbitMQ message
 */
object MessageType {
    const val PublicDataReceived = "Public Data received"
    const val DataStored = "Data stored"
    const val DocumentReceived = "Document received"
    const val DocumentStored = "Document stored"
    const val QaCompleted = "QA completed"
    const val ManualQaRequested = "Manual QA requested"
    const val PersistAutomatedQaResult = "Persist automated QA result"
    const val SendInternalEmail = "Send internal E-Mail"
    const val SendTemplateEmail = "Send template E-Mail"
    const val PrivateDataStored = "Private Data Stored"
    const val PrivateDataReceived = "Private Data received"
}
