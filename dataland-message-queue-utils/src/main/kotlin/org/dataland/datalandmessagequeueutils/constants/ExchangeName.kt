package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as exchanges in rabbitmq.
 */

object ExchangeName {
    const val DataQualityAssured = "dataQualityAssured"
    const val RequestReceived = "requestReceived"
    const val ItemStored = "itemStored"
    const val DocumentReceived = "documentReceived"
    const val DeadLetter = "deadLetter"
    const val ManualQaRequested = "manualQaRequested"
    const val SendEmail = "sendEmail"
    const val PrivateItemStored = "privateItemStored"
    const val PrivateRequestReceived = "privateRequestReceived"
}
