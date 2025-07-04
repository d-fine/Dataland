package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackendutils.utils.BackendOpenApiDescriptionsAndExamples

object CommunityManagerOpenApiDescriptionsAndExamples {
    const val BULK_REQUEST_COMPANY_IDENTIFIERS_DESCRIPTION =
        "A set of identifier strings to identify companies on Dataland by. Use Dataland company IDs, " +
            "Leis or Isins for the best results. Requests will be posted for all companies that can " +
            "be identified."
    const val BULK_REQUEST_COMPANY_IDENTIFIERS_EXAMPLE =
        "[\"99c5e9b2-220e-40dc-aba5-f371566e73a4\",\"549300JSX0Z4CW0V5023\",\"US0378331005\"]"

    const val BULK_REQUEST_DATA_TYPES_DESCRIPTION =
        "A set of framework names. Use kebab-case names such as \"eutaxonomy-financials\". Using a framework " +
            "name that cannot be identified will result in an error, and no requests will be created."
    const val BULK_REQUEST_DATA_TYPES_EXAMPLE =
        "[\"sfdr\",\"eutaxonomy-financials\",\"eutaxonomy-non-financials\",\"nuclear-and-gas\",\"lksg\",\"vsme\"]"

    const val BULK_REQUEST_REPORTING_PERIODS_DESCRIPTION =
        "A set of strings which identify reporting periods on Dataland, such as year numbers. Requests will " +
            "be posted for all specified reporting periods."
    const val BULK_REQUEST_REPORTING_PERIODS_EXAMPLE =
        "[\"2023\",\"2024\"]"

    const val BULK_REQUEST_NOTIFY_ME_IMMEDIATELY_DESCRIPTION =
        "Whether an immediate notification email shall be sent when there is an update concerning one of " +
            "the posted requests. Otherwise, those updates are listed in the weekly summary email."
    const val BULK_REQUEST_NOTIFY_ME_IMMEDIATELY_EXAMPLE = "true"

    const val USER_ID_DESCRIPTION = "The ID of the user who created this data request."
    const val USER_ID_EXAMPLE = "814caf16-54de-4385-af6e-bd6b64b64634"

    const val POST_MESSAGE_DESCRIPTION = "Contains a text about the result of Dataland processing the data request."
    const val POST_MESSAGE_EXAMPLE = "Post message example"

    const val REPORTING_PERIODS_OF_STORED_DATA_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to the accepted and stored data requests."
    const val REPORTING_PERIODS_OF_STORED_DATA_REQUESTS_EXAMPLE = "description"

    const val REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to duplicate data requests on Dataland"
    const val REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_EXAMPLE = "description"

    const val REPORTING_PERIODS_OF_STORED_ACCESS_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to the accepted and stored access requests."
    const val REPORTING_PERIODS_OF_STORED_ACCESS_REQUESTS_EXAMPLE = "description"

    const val ACCEPTED_DATA_REQUESTS_DESCRIPTION = "Contains information about all accepted data requests"
    const val ACCEPTED_DATA_REQUESTS_EXAMPLE = "description"

    const val ALREADY_EXISTING_NON_FINAL_REQUESTS_DESCRIPTION = "Contains information about all already existing non-final data requests"
    const val ALREADY_EXISTING_NON_FINAL_REQUESTS_EXAMPLE = "description"

    const val ALREADY_EXISTING_DATASETS_DESCRIPTION = "Contains information about all already existing data sets"
    const val ALREADY_EXISTING_DATASETS_EXAMPLE = "description"

    const val REJECTED_COMPANY_IDENTIFIERS_DESCRIPTION = "Contains all company identifiers that were rejected by Dataland"
    const val REJECTED_COMPANY_IDENTIFIERS_EXAMPLE = "description"

    const val USER_EMAIL_ADDRESS_DESCRIPTION = "The email address of the user who created this data request."
    const val USER_EMAIL_ADDRESS_EXAMPLE = "description"

    const val CREATION_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) when the user created the data request."
    const val CREATION_TIMESTAMP_EXAMPLE = "description"

    const val DATA_TYPE_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION
    const val DATA_TYPE_EXAMPLE = BackendOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE

    const val REPORTING_PERIOD_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION
    const val REPORTING_PERIOD_EXAMPLE = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE

    const val COMPANY_ID_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION
    const val COMPANY_ID_EXAMPLE = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE

    const val COMPANY_NAME_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION
    const val COMPANY_NAME_EXAMPLE = BackendOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE

    const val LAST_MODIFIED_DATE_DESCRIPTION = "The date when the data request has been modified the last time."
    const val LAST_MODIFIED_DATE_EXAMPLE = "description"

    const val MESSAGE_HISTORY_DESCRIPTION = "The list of all message objects which were created during the life cycle."

    const val REQUEST_STATUS_DESCRIPTION =
        "The current status of the data request. Possible values are: Open, Answered, Resolved, Withdrawn, Closed, NonSourceable."

    const val ACCESS_STATUS_DESCRIPTION =
        "The current status of the access to the data request. Possible values are: Declined, Granted, Pending, Public, Revoked."

    const val REQUEST_PRIORITY_DESCRIPTION = "The priority of the data request. Possible values are: Low, Baseline, High, Urgent."

    const val ADMIN_COMMENT_DESCRIPTION = "The admin comment of the data request."
    const val ADMIN_COMMENT_EXAMPLE = "description"

    const val DATA_REQUEST_ID_DESCRIPTION = "description"
    const val DATA_REQUEST_ID_EXAMPLE = "description"

    const val CONTACTS_DESCRIPTION = "A list which includes all contact (mail) details."
    const val CONTACTS_EXAMPLE = "[\"testuser@example.com\"]"

    const val MESSAGE_DESCRIPTION = "A message in the form of a string that was created during the life cycle."
    const val MESSAGE_EXAMPLE = "description"

    const val MESSAGE_CREATION_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) when the message object was created."
    const val MESSAGE_CREATION_TIMESTAMP_EXAMPLE = "description"

    const val STATUS_CREATION_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) when the status object was created."
    const val STATUS_CREATION_TIMESTAMP_EXAMPLE = "description"

    const val REQUEST_STATUS_CHANGE_REASON_DESCRIPTION = "The reason for the status change."
    const val REQUEST_STATUS_CHANGE_REASON_EXAMPLE = "description"

    const val ANSWERING_DATA_ID_DESCRIPTION = "The data ID of the data set that answered the request."
    const val ANSWERING_DATA_ID_EXAMPLE = "description"

    const val CHUNK_SIZE_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.CHUNK_SIZE_DESCRIPTION
    const val CHUNK_INDEX_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.CHUNK_INDEX_DESCRIPTION

    const val AGGREGATED_DATA_REQUEST_PRIORITY_DESCRIPTION =
        "The aggregated data request priority. Possible values: Low, Normal, Baseline, High, VeryHigh, Urgent."

    const val DATA_REQUEST_COUNT_DESCRIPTION =
        "The count of existing data requests for this framework, identifierType and identifierValue."
    const val DATA_REQUEST_COUNT_EXAMPLE = "0"
}
