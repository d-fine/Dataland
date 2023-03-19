package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as exchanges in rabbitmq.
 */

object ExchangeNames {
    const val dataQualityAssured = "dataQualityAssured"
    const val dataReceived = "dataReceived"
    const val dataStored = "dataStored"
    const val deadLetter = "deadLetter"
}
