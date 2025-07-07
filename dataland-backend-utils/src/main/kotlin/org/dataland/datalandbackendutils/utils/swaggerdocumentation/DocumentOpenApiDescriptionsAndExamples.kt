package org.dataland.datalandbackendutils.utils.swaggerdocumentation

object DocumentOpenApiDescriptionsAndExamples {
    const val DOCUMENT_NAME_DESCRIPTION =
        "Name under which the document is saved on Dataland. " +
            "Does not need to coincide with the name of the uploaded file " +
            "nor include the file type suffix (such as '.pdf')."
    const val DOCUMENT_NAME_EXAMPLE = "Company_X_Annual_Report_2024"

    const val DOCUMENT_CATEGORY_DESCRIPTION = "The Dataland document category to which the document belongs."
    const val DOCUMENT_CATEGORY_EXAMPLE = "AnnualReport"

    const val COMPANY_IDS_DESCRIPTION =
        "The set of Dataland company IDs of the companies using this document " +
            "as a referenced report."
    const val COMPANY_IDS_EXAMPLE =
        "[\n\t\"72c5cbdc-4244-49dd-8368-be4e64b399ae\"," +
            "\n\t\"a31733e0-42ed-47c9-9909-e1d2ecf08083\"\n]"

    const val PUBLICATION_DATE_DESCRIPTION =
        "The date on which this document was published by the responsible company, specified in format " +
            "'yyyy-MM-dd'."
    const val PUBLICATION_DATE_EXAMPLE = "2024-02-13"

    const val REPORTING_PERIOD_DESCRIPTION = "The reporting period the document belongs to (e.g. a fiscal year)."
    const val REPORTING_PERIOD_EXAMPLE = "2023"

    const val DOCUMENT_ID_DESCRIPTION =
        "The ID under which the document can be found on Dataland. It is contained in the HTTP response " +
            "after posting a new document and is a SHA-256 value."
    const val DOCUMENT_ID_EXAMPLE =
        "afa44f6138e4e3925f9da5ae355a9ee60f1630e52ddd755c4b2762cf61b4f7b4"

    const val UPLOADER_ID_DESCRIPTION = "The Dataland user ID of the user who uploaded the document."
    const val UPLOADER_ID_EXAMPLE = "814caf16-54de-4385-af6e-bd6b64b64634"

    const val DOCUMENT_TYPE_DESCRIPTION = "The file type of the document."
    const val DOCUMENT_TYPE_EXAMPLE = "Pdf"

    const val UPLOAD_TIME_DESCRIPTION =
        "The timestamp (epoch milliseconds) at which the document was uploaded to Dataland."
    const val UPLOAD_TIME_EXAMPLE =
        "1751291891271"

    const val QA_STATUS_DESCRIPTION =
        "The status of the document with regard to Dataland's Quality Assurance process."
    const val QA_STATUS_EXAMPLE = "Accepted"

    const val ADDED_COMPANY_ID_DESCRIPTION =
        "Dataland ID of the company to newly associate with the document."
    const val ADDED_COMPANY_ID_EXAMPLE = "2dfff91b-18d8-489f-8abe-64febd8be9c4"

    const val COMPANY_ID_SEARCH_PARAMETER_DESCRIPTION =
        "If specified, only returns meta information of documents associated with the company " +
            "having this Dataland company ID."
    const val COMPANY_ID_SEARCH_PARAMETER_EXAMPLE = "536a58ed-bbea-4a12-b9f8-b20508582a0e"

    const val DOCUMENT_CATEGORIES_SEARCH_PARAMETER_DESCRIPTION =
        "If specified, only returns meta information of documents having one of these document " +
            "categories."

    const val REPORTING_PERIOD_SEARCH_PARAMETER_DESCRIPTION =
        "If specified, only returns meta information of documents associated with this reporting" +
            "period (e.g., fiscal year)."
    const val REPORTING_PERIOD_SEARCH_PARAMETER_EXAMPLE = "2024"

    const val CHUNK_SIZE_DESCRIPTION =
        "Only a chunk of all matching, ordered results is returned. This parameter specifies the maximum size " +
            "of a single chunk. All chunks except possibly the last will have that size. The default " +
            "value is 100."
    const val CHUNK_SIZE_EXAMPLE = "50"

    const val CHUNK_INDEX_DESCRIPTION =
        "Only a chunk of all matching, ordered results is returned. This parameter specifies the number of the " +
            "returned chunk in the ordering, with counting starting at 0. The default value is 0, i.e., by " +
            "default, the first chunk (containing the meta information of the latest published documents) is " +
            "returned."
    const val CHUNK_INDEX_EXAMPLE = "0"
}
