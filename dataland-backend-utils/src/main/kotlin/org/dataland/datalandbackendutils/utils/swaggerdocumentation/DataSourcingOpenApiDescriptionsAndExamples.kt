package org.dataland.datalandbackendutils.utils.swaggerdocumentation

object DataSourcingOpenApiDescriptionsAndExamples {
    const val ID_DESCRIPTION = "The unique identifier of the data sourcing object."
    const val ID_EXAMPLE = "ef6d806c-4bd3-4ea2-b8e6-dcc590385e15"

    const val STATE_DESCRIPTION = "The current state of the data sourcing object."

    const val DOCUMENT_IDS_DESCRIPTION = "The documents from which the data for this data sourcing object are extracted."
    const val DOCUMENT_IDS_EXAMPLE = "[\"91a6f38ece1ae347d1b0dc9f6b6a0ef7593ed051595e3f138e922cb1df39c86c\"]"

    const val EXPECTED_PUBLICATION_DATES_DESCRIPTION =
        "The expected publication dates of future documents relevant for this data sourcing object."

    const val DATE_DOCUMENT_SOURCING_ATTEMPT_DESCRIPTION =
        "The date of the next planned attempt to source documents for this data sourcing object."

    const val DOCUMENT_COLLECTOR_DESCRIPTION =
        "The Dataland company ID of the company which is responsible for the collection of documents concerning " +
            "this data sourcing object."
    const val DOCUMENT_COLLECTOR_EXAMPLE = "d23d16de-e9c8-470f-b08b-e576e0c4193e"

    const val DATA_EXTRACTOR_DESCRIPTION =
        "The Dataland company ID of the company which is responsible for the extraction of data from documents " +
            "concerning this data sourcing object."
    const val DATA_EXTRACTOR_EXAMPLE = "8248c02a-8958-4518-b61f-a5fff1afbe2e"

    const val ADMIN_COMMENT_DESCRIPTION = "A comment that can be set by Dataland admins to provide additional information."
    const val ADMIN_COMMENT_EXAMPLE = "The data sourcing process is delayed due to unforeseen circumstances."

    const val ASSOCIATED_REQUEST_IDS_DESCRIPTION = "The IDs of stored data requests associated with this data sourcing object."

    const val COMMENT_DESCRIPTION = "A comment that accompanies the data sourcing request."
    const val COMMENT_EXAMPLE = "The previously provided data is lacking scope 3 emissions, but they should be available."

    const val STORED_REQUEST_IDS_DESCRIPTION =
        "The list of Dataland IDs of the created data requests. The position of an ID in this " +
            "list corresponds to the position of the associated reporting period in the field reportingPeriodsOfStoredRequests."
    const val STORED_REQUEST_IDS_EXAMPLE = "[\"40c84975-529a-41d2-b5dc-dbb4c803d2bc\"]"

    const val DUPLICATE_REQUEST_IDS_DESCRIPTION =
        "The list of Dataland IDs of the data requests that were identified as duplicates and hence not created. " +
            "The position of an ID in this list corresponds to the position of the associated reporting period in the field " +
            "reportingPeriodsOfDuplicateRequests."
    const val DUPLICATE_REQUEST_IDS_EXAMPLE = "[\"821d400c-059f-4ce0-96d0-668679d744ca\"]"
}
