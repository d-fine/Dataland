package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as exchanges in rabbitmq.
 */

object RoutingKeyNames {
    const val document = "document"
    const val data = "data"
    const val internalEmail = "internalEmail"
    const val templateEmail = "templateEmail"
    const val persistAutomatedQaResult = "persistAutomatedQaResult"
    const val metaDataPersisted = "metaDataPersisted"
    const val privateDataAndDocument = "privateDataAndDocument"
}
