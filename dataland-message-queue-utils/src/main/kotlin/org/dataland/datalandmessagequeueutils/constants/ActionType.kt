package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as actions in rabbitmq.
 */

object ActionType {
    const val STORE_PUBLIC_DATA = "storePublicData"
    const val DELETE_DATA = "deleteData"
    const val STORE_PRIVATE_DATA_AND_DOCUMENTS = "storePrivateDataAndDocuments"
}
