package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as exchanges in rabbitmq.
 */

object RoutingKeyNames {
    const val DOCUMENT = "document"
    const val DATA = "data"
    const val EMAIL = "email"
    const val PERSIST_AUTOMATED_QA_RESULT = "persistAutomatedQaResult"
    const val META_DATA_PERSISTED = "metaDataPersisted"
    const val PRIVATE_DATA_AND_DOCUMENT = "privateDataAndDocument"
    const val DATA_NONSOURCEABLE = "dataNonSourceable"
}
