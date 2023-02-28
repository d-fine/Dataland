package org.dataland.datalandmessagequeueutils.messages

data class QaCompletedMessage(
    val dataId: String,
    val validationResult: String
    )