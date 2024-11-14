package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as exchanges in rabbitmq.
 */

object RoutingKeyNames {
    const val DOCUMENT = "document"
    const val DOCUMENT_QA = "document.qa"
    const val DATA = "data"
    const val DATA_QA = "data.qa"
    const val PERSIST_BYPASS_QA_RESULT = "persistBypassQaResult"
    const val INTERNAL_EMAIL = "internalEmail"
    const val TEMPLATE_EMAIL = "templateEmail"
    const val META_DATA_PERSISTED = "metaDataPersisted"
    const val PRIVATE_DATA_AND_DOCUMENT = "privateDataAndDocument"
}
