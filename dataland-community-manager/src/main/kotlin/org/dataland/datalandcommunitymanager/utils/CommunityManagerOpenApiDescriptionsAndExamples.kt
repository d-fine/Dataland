package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackendutils.utils.BackendOpenApiDescriptionsAndExamples

object CommunityManagerOpenApiDescriptionsAndExamples {
    const val DATA_TYPE_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION
    const val DATA_TYPE_EXAMPLE = BackendOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE

    const val REPORTING_PERIOD_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION
    const val REPORTING_PERIOD_EXAMPLE = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE

    const val COMPANY_ID_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION
    const val COMPANY_ID_EXAMPLE = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE

    const val COMPANY_NAME_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION
    const val COMPANY_NAME_EXAMPLE = BackendOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE

    const val COMPANY_IDENTIFIER_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.SINGLE_IDENTIFIER_DESCRIPTION
    const val COMPANY_IDENTIFIER_EXAMPLE = BackendOpenApiDescriptionsAndExamples.SINGLE_IDENTIFIER_EXAMPLE

    const val REPORTING_PERIODS_LIST_EXAMPLE = "[\"2023\",\"2024\"]"
    const val TIMESTAMP_EXAMPLE = "1751291892373"

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
    const val BULK_REQUEST_REPORTING_PERIODS_EXAMPLE = REPORTING_PERIODS_LIST_EXAMPLE

    const val BULK_REQUEST_NOTIFY_ME_IMMEDIATELY_DESCRIPTION =
        "Whether an immediate notification email shall be sent when there is an update concerning one of " +
            "the posted requests. Otherwise, those updates are listed in the weekly summary email."
    const val BULK_REQUEST_NOTIFY_ME_IMMEDIATELY_EXAMPLE = "true"

    const val USER_ID_DESCRIPTION = "The ID of the user who created this data request."
    const val USER_ID_EXAMPLE = "814caf16-54de-4385-af6e-bd6b64b64634"

    const val POST_MESSAGE_DESCRIPTION = "Contains a text about the result of Dataland processing the data request."
    const val POST_MESSAGE_EXAMPLE = "Your data request was stored successfully."

    const val REPORTING_PERIODS_OF_STORED_DATA_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to the accepted and stored data requests."
    const val REPORTING_PERIODS_OF_STORED_DATA_REQUESTS_EXAMPLE = REPORTING_PERIODS_LIST_EXAMPLE

    const val REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to data requests that were rejected due to being duplicates."
    const val REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_EXAMPLE = "[\"2022\"]"

    const val REPORTING_PERIODS_OF_STORED_ACCESS_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to the accepted and stored access requests."
    const val REPORTING_PERIODS_OF_STORED_ACCESS_REQUESTS_EXAMPLE = REPORTING_PERIODS_LIST_EXAMPLE

    const val ACCEPTED_DATA_REQUESTS_DESCRIPTION = "Contains information about all accepted data requests"

    const val ALREADY_EXISTING_NON_FINAL_REQUESTS_DESCRIPTION = "Contains information about all already existing non-final data requests"

    const val ALREADY_EXISTING_DATASETS_DESCRIPTION = "Contains information about all already existing data sets"

    const val REJECTED_COMPANY_IDENTIFIERS_DESCRIPTION = "Contains all company identifiers that were rejected by Dataland"
    const val REJECTED_COMPANY_IDENTIFIERS_EXAMPLE = "[\"$COMPANY_IDENTIFIER_EXAMPLE\"]"

    const val USER_EMAIL_ADDRESS_DESCRIPTION = "The email address of the user who created this data request."
    const val USER_EMAIL_ADDRESS_EXAMPLE = "test@example.com"

    const val CREATION_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) when the user created the data request."
    const val CREATION_TIMESTAMP_EXAMPLE = TIMESTAMP_EXAMPLE

    const val LAST_MODIFIED_DATE_DESCRIPTION = "The timestamp (epoch milliseconds) when the data request has been modified the last time."
    const val LAST_MODIFIED_DATE_EXAMPLE = TIMESTAMP_EXAMPLE

    const val MESSAGE_HISTORY_DESCRIPTION =
        "The list of objects documenting the history of messages sent to company contacts regarding the data request."

    const val REQUEST_STATUS_DESCRIPTION =
        "The current status of the data request."

    const val ACCESS_STATUS_DESCRIPTION =
        "The current status of the access to the data request."

    const val REQUEST_PRIORITY_DESCRIPTION = "The priority of the data request."

    const val ADMIN_COMMENT_DESCRIPTION = "The admin comment of the data request."
    const val ADMIN_COMMENT_EXAMPLE = "Should be processed with high priority."

    const val DATA_REQUEST_ID_DESCRIPTION = "The unique identifier of the data request on Dataland."
    const val DATA_REQUEST_ID_EXAMPLE = "5e34d8ab-f790-4c2a-a371-8f62be90c483"

    const val CONTACTS_DESCRIPTION = "A list of company contact email addresses specified by the user creating the request."
    const val CONTACTS_EXAMPLE = "[\"testuser@example.com\"]"

    const val MESSAGE_DESCRIPTION = "A message specified by the user creating the request to be sent to the company contacts."
    const val MESSAGE_EXAMPLE = "Please consider sharing your data on Dataland."

    const val MESSAGE_CREATION_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) when the message object was created."
    const val MESSAGE_CREATION_TIMESTAMP_EXAMPLE = TIMESTAMP_EXAMPLE

    const val STATUS_CREATION_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) when the status object was created."
    const val STATUS_CREATION_TIMESTAMP_EXAMPLE = TIMESTAMP_EXAMPLE

    const val REQUEST_STATUS_CHANGE_REASON_DESCRIPTION = "The reason for the status change."
    const val REQUEST_STATUS_CHANGE_REASON_EXAMPLE = "The company has not published data for this reporting period yet."

    const val ANSWERING_DATA_ID_DESCRIPTION = "The data ID of the data set that answered the request."
    const val ANSWERING_DATA_ID_EXAMPLE = "9a73f4cd-2b68-401e-a8c5-d01b57e8f924"

    const val CHUNK_SIZE_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.CHUNK_SIZE_DESCRIPTION
    const val CHUNK_INDEX_DESCRIPTION = BackendOpenApiDescriptionsAndExamples.CHUNK_INDEX_DESCRIPTION

    const val AGGREGATED_DATA_REQUEST_PRIORITY_DESCRIPTION =
        "The aggregated data request priority."

    const val DATA_REQUEST_COUNT_DESCRIPTION =
        "The count of existing data requests for this framework, reporting period, company and priority."
    const val DATA_REQUEST_COUNT_EXAMPLE = "23"

    const val COMPANY_ROLE_DESCRIPTION =
        "One of a selection of Dataland specific roles which a user can have with respect to a company on Dataland."

    const val OWNERSHIP_COMMENT_DESCRIPTION = "An accompanying comment to a company ownership request."
    const val OWNERSHIP_COMMENT_EXAMPLE = "I am the CEO of this company, please make me its owner on Dataland."

    const val USER_PROVIDED_IDENTIFIER_DESCRIPTION =
        "The identifier (e.g., Dataland company ID or LEI) through which the user specified the respective company."
    const val USER_PROVIDED_IDENTIFIER_EXAMPLE = COMPANY_IDENTIFIER_EXAMPLE

    const val RESOURCE_ID_DESCRIPTION =
        "A unique identifier for identifying a resource on Dataland. In case of newly created or duplicate open requests, " +
            "it identifies the newly created or original open request. " +
            "In case of a request for an already existing dataset, it identifies that dataset."
    const val RESOURCE_ID_EXAMPLE = "d796f874-7a7e-457c-aa41-2cb73d4e9529"

    const val RESOURCE_URL_DESCRIPTION = "A URL pointing to the resource identified by resourceId."
    const val RESOURCE_URL_EXAMPLE = "https://dataland.com/requests/d796f874-7a7e-457c-aa41-2cb73d4e9529"
}
