package org.dataland.documentmanager.utils

object OpenApiDescriptionsAndExamples {
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
}
