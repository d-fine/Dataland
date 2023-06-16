package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as exchanges in rabbitmq.
 */

object ExchangeName {
    const val DataQualityAssured = "dataQualityAssured"
    const val DataReceived = "dataReceived"
    const val ItemStored = "itemStored"
    const val DocumentReceived = "documentReceived"
    const val DeadLetter = "deadLetter"
}
