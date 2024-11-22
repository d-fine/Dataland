package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as routing keys in rabbitmq.
 */

object RoutingKeyNames {
    const val DOCUMENT = "document"
    const val DOCUMENT_QA = "document.qa"
    const val DATA = "data"
    const val DATA_QA = "data.qa"
    const val DELETE_QA_INFO = "delete.qa.info"
    const val EMAIL = "email"
    const val META_DATA_PERSISTED = "metaDataPersisted"
    const val PRIVATE_DATA_AND_DOCUMENT = "privateDataAndDocument"
}
