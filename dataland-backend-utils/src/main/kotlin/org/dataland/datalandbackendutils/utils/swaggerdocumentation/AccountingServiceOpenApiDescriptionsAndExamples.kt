package org.dataland.datalandbackendutils.utils.swaggerdocumentation

object AccountingServiceOpenApiDescriptionsAndExamples {
    const val TRANSACTION_VALUE_OF_CHANGE_DESCRIPTION =
        "The amount by which the transaction changes the company's credits balance (may be positive or negative)."
    const val TRANSACTION_VALUE_OF_CHANGE_EXAMPLE = "100.0"

    const val TRANSACTION_REASON_FOR_CHANGE_DESCRIPTION = "An optional reason for the transaction."
    const val TRANSACTION_REASON_FOR_CHANGE_EXAMPLE = "Annual credits top-up."

    const val TRANSACTION_COMPANY_ID_DESCRIPTION = "The Dataland ID of the company for which the transaction is made."

    const val TRANSACTION_TRIGGERING_USER_DESCRIPTION = "The Dataland ID of the user who triggered the transaction."

    const val TRANSACTION_TIMESTAMP_DESCRIPTION = "The timestamp (in milliseconds since epoch) when the transaction was posted."
}
