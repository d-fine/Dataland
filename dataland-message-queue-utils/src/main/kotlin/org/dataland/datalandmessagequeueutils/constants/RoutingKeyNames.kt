package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as exchanges in rabbitmq.
 */

object RoutingKeyNames {
    const val DOCUMENT = "document"
    const val DATA = "data"
    const val INTERNAL_EMAIL = "internalEmail"
    const val TEMPLATE_EMAIL = "templateEmail"
    const val PERSIST_AUTOMATED_QA_RESULT = "persistAutomatedQaResult"
    const val META_DATA_PERSISTED = "metaDataPersisted"
    const val PRIVATE_DATA_AND_DOCUMENT = "privateDataAndDocument"
}
