package org.dataland.datalandbackendutils.utils.swaggerdocumentation

object CommunityManagerOpenApiDescriptionsAndExamples {
    const val BULK_REQUEST_COMPANY_IDENTIFIERS_DESCRIPTION =
        "A set of identifiers for companies on Dataland by. Use Dataland company IDs, " +
            "Leis or Isins for the best results. Requests will be posted for all companies that can " +
            "be identified."
    const val BULK_REQUEST_COMPANY_IDENTIFIERS_EXAMPLE =
        "[\"${GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE}\"," +
            "\"${GeneralOpenApiDescriptionsAndExamples.GENERAL_LEI_EXAMPLE}\"," +
            "\"US0378331005\"]"

    const val BULK_REQUEST_DATA_TYPES_DESCRIPTION =
        "A set of framework names. Use kebab-case names such as \"eutaxonomy-financials\". Using a framework " +
            "name that cannot be identified will result in an error, and no requests will be created."
    const val BULK_REQUEST_DATA_TYPES_EXAMPLE =
        "[\"sfdr\",\"eutaxonomy-financials\",\"eutaxonomy-non-financials\",\"nuclear-and-gas\",\"lksg\",\"vsme\"]"

    const val BULK_REQUEST_REPORTING_PERIODS_DESCRIPTION =
        "A set of strings which identify reporting periods on Dataland, such as year numbers. Requests will " +
            "be posted for all specified reporting periods."
    const val BULK_REQUEST_REPORTING_PERIODS_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_LIST_EXAMPLE

    const val BULK_REQUEST_NOTIFY_ME_IMMEDIATELY_DESCRIPTION =
        "Boolean indicating whether an immediate notification email shall be sent whenever there is an update concerning one of " +
            "the posted requests. If set to false, these updates are listed in the weekly summary email."
    const val BULK_REQUEST_NOTIFY_ME_IMMEDIATELY_EXAMPLE = "true"

    const val USER_ID_DESCRIPTION = "The ID of the user who created the data request."
    const val USER_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val COMPANY_ROLE_USER_ID_DESCRIPTION = "The unique identifier of the user."
    const val COMPANY_ROLE_USER_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val POST_MESSAGE_DESCRIPTION = "A text about the result of Dataland processing the data request."
    const val POST_MESSAGE_EXAMPLE = "Your data request was stored successfully."

    const val REPORTING_PERIODS_OF_STORED_DATA_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to the accepted and stored data requests."
    const val REPORTING_PERIODS_OF_STORED_DATA_REQUESTS_EXAMPLE =
        GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_LIST_EXAMPLE

    const val REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to data requests that were rejected due to being duplicates."
    const val REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_EXAMPLE = "[\"2022\"]"

    const val REPORTING_PERIODS_OF_STORED_ACCESS_REQUESTS_DESCRIPTION =
        "Reporting periods corresponding to the accepted and stored access requests."
    const val REPORTING_PERIODS_OF_STORED_ACCESS_REQUESTS_EXAMPLE =
        GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_LIST_EXAMPLE

    const val ACCEPTED_DATA_REQUESTS_DESCRIPTION = "Contains information about all accepted data requests"

    const val ALREADY_EXISTING_NON_FINAL_REQUESTS_DESCRIPTION = "Contains information about all already existing non-final data requests"

    const val ALREADY_EXISTING_DATASETS_DESCRIPTION = "Contains information about all already existing data sets"

    const val REJECTED_COMPANY_IDENTIFIERS_DESCRIPTION = "Contains all company identifiers that were rejected by Dataland"
    const val REJECTED_COMPANY_IDENTIFIERS_EXAMPLE = "[\"${GeneralOpenApiDescriptionsAndExamples.COMPANY_SINGLE_IDENTIFIER_EXAMPLE}\"]"

    const val USER_EMAIL_ADDRESS_DESCRIPTION = "The email address of the user who created this data request."
    const val USER_EMAIL_ADDRESS_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_EMAIL_EXAMPLE

    const val CREATION_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) when the user created the data request."
    const val CREATION_TIMESTAMP_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE

    const val LAST_MODIFIED_DATE_DESCRIPTION = "The timestamp (epoch milliseconds) when the data request has been modified the last time."
    const val LAST_MODIFIED_DATE_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE

    const val MESSAGE_HISTORY_DESCRIPTION =
        "The list of objects documenting the history of messages sent to company contacts regarding the data request."

    const val REQUEST_STATUS_DESCRIPTION =
        "The current request status of the data request."

    const val ACCESS_STATUS_DESCRIPTION =
        "The current access status of the data request."

    const val REQUEST_PRIORITY_DESCRIPTION = "The priority of the data request."

    const val ADMIN_COMMENT_DESCRIPTION = "The admin comment of the data request."
    const val ADMIN_COMMENT_EXAMPLE = "Should be processed with high priority."

    const val DATA_REQUEST_ID_DESCRIPTION = "The unique identifier of the data request on Dataland."
    const val DATA_REQUEST_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val CONTACTS_DESCRIPTION = "A list of company contact email addresses specified by the user creating the request."
    const val CONTACTS_EXAMPLE = "[\"${GeneralOpenApiDescriptionsAndExamples.GENERAL_EMAIL_EXAMPLE}\"]"

    const val MESSAGE_DESCRIPTION = "A message specified by the user creating the request to be sent to the company contacts."
    const val MESSAGE_EXAMPLE = "Please consider sharing your data on Dataland."

    const val MESSAGE_CREATION_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) when the message object was created."
    const val MESSAGE_CREATION_TIMESTAMP_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE

    const val STATUS_CREATION_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) when the status object was created."
    const val STATUS_CREATION_TIMESTAMP_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE

    const val REQUEST_STATUS_CHANGE_REASON_DESCRIPTION = "The reason for the status change."
    const val REQUEST_STATUS_CHANGE_REASON_EXAMPLE = "The company has not published data for this reporting period yet."

    const val ANSWERING_DATA_ID_DESCRIPTION = "The data ID of the data set that answered the request."
    const val ANSWERING_DATA_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val AGGREGATED_DATA_REQUEST_PRIORITY_DESCRIPTION =
        "The aggregated data request priority."

    const val DATA_REQUEST_COUNT_DESCRIPTION =
        "The count of existing data requests for this framework, reporting period, company and priority."
    const val DATA_REQUEST_COUNT_EXAMPLE = "23"

    const val COMPANY_ROLE_DESCRIPTION =
        "One of the Dataland specific roles which a user can have with respect to a company on Dataland."

    const val OWNERSHIP_COMMENT_DESCRIPTION = "An accompanying comment to a company ownership request."
    const val OWNERSHIP_COMMENT_EXAMPLE = "I am the CEO of this company, please make me its owner on Dataland."

    const val USER_PROVIDED_IDENTIFIER_DESCRIPTION =
        "The identifier (e.g., Dataland company ID or LEI) through which the user specified the respective company."
    const val USER_PROVIDED_IDENTIFIER_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_LEI_EXAMPLE

    const val RESOURCE_ID_DESCRIPTION =
        "A unique identifier of a resource on Dataland. In case of newly created or duplicate open requests, " +
            "it identifies the newly created or original open request. " +
            "In case of a request for an already existing dataset, it identifies that dataset."
    const val RESOURCE_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val RESOURCE_URL_DESCRIPTION = "A URL pointing to the resource identified by resourceId."
    const val RESOURCE_URL_EXAMPLE = "https://dataland.com/requests/$RESOURCE_ID_EXAMPLE"
}
