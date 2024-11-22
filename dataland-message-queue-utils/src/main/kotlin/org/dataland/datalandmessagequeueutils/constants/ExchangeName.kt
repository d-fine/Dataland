package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as exchanges in rabbitmq.
 */

object ExchangeName {
    const val DATA_QUALITY_ASSURED = "dataQualityAssured"
    const val REQUEST_RECEIVED = "requestReceived"
    const val ITEM_STORED = "itemStored"
    const val DOCUMENT_RECEIVED = "documentReceived"
    const val DEAD_LETTER = "deadLetter"
    const val SEND_EMAIL = "sendEmail"
    const val PRIVATE_ITEM_STORED = "privateItemStored"
    const val PRIVATE_REQUEST_RECEIVED = "privateRequestReceived"
    const val BACKEND_DATA_POINT_EVENTS = "backend.datapoints"
}
