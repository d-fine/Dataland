package org.dataland.datalandbackendutils.utils.swaggerdocumentation

object GeneralOpenApiDescriptionsAndExamples {
    const val GENERAL_UUID_EXAMPLE = "1e63a842-1e65-43ed-b78a-5e7cec155c28"
    const val GENERAL_EMAIL_EXAMPLE = "test@example.com"
    const val GENERAL_TIMESTAMP_EXAMPLE = "1751291891271"
    const val GENERAL_LEI_EXAMPLE = "5493001KJX4BT0IHAG73"

    const val POST_MESSAGE_DESCRIPTION = "A text about the result of Dataland processing the data request."
    const val POST_MESSAGE_EXAMPLE = "Your data request was stored successfully."

    const val GENERAL_REPORTING_PERIODS_LIST_DESCRIPTION = "The associated reporting periods (e.g. fiscal years)."
    const val GENERAL_REPORTING_PERIODS_LIST_EXAMPLE = "[\"2023\",\"2024\"]"

    const val REPORTING_PERIODS_OF_STORED_DATA_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to the accepted and stored data requests."
    const val REPORTING_PERIODS_OF_STORED_DATA_REQUESTS_EXAMPLE = GENERAL_REPORTING_PERIODS_LIST_EXAMPLE

    const val REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to data requests that were rejected due to being duplicates."
    const val REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_EXAMPLE = "[\"2022\"]"

    const val REPORTING_PERIOD_DESCRIPTION = "The associated reporting period (e.g. a fiscal year)."
    const val REPORTING_PERIOD_EXAMPLE = "2023"

    const val UPLOAD_TIME_DESCRIPTION = "The timestamp (epoch milliseconds) at which the document was uploaded to Dataland."
    const val UPLOAD_TIME_EXAMPLE = GENERAL_TIMESTAMP_EXAMPLE

    const val QA_STATUS_DESCRIPTION =
        "The status with regard to Dataland's quality assurance process."

    const val CHUNK_SIZE_DESCRIPTION =
        "Only a chunk of all matching, ordered results is returned. This parameter specifies the maximum size " +
            "of a single chunk. All chunks except possibly the last will have that size."

    const val CHUNK_INDEX_DESCRIPTION =
        "Only a chunk of all matching, ordered results is returned. " +
            "This parameter specifies which chunk to return, starting at 0. " +
            "The default value is 0, meaning the first chunk is returned."

    const val COMPANY_ID_DESCRIPTION = "The unique identifier under which the company can be found on Dataland."
    const val COMPANY_ID_EXAMPLE = "c9710c7b-9cd6-446b-85b0-3773d2aceb48"

    const val COMPANY_NAME_DESCRIPTION = "The official name of the company."
    const val COMPANY_NAME_EXAMPLE = "ABC Corporation"

    const val DATA_TYPE_DESCRIPTION = "The associated reporting framework."
    const val DATA_TYPE_EXAMPLE = "sfdr"

    const val COMPANY_SINGLE_IDENTIFIER_DESCRIPTION = "Unique identifier to find a company."
    const val COMPANY_SINGLE_IDENTIFIER_EXAMPLE = GENERAL_LEI_EXAMPLE

    const val SINGLE_REQUEST_NOTIFY_ME_IMMEDIATELY_DESCRIPTION =
        "Boolean indicating whether an immediate notification email shall be " +
            "sent whenever there is an update concerning the request. " +
            "If set to false, these updates are listed in the weekly summary email."
}
