package org.dataland.datalandbackendutils.utils

object BackendOpenApiDescriptionsAndExamples {
    const val COMPANY_NAME_DESCRIPTION = "The official name of the company."
    const val COMPANY_NAME_EXAMPLE = "ABC Corporation"

    const val COMPANY_ALTERNATIVE_NAMES_DESCRIPTION = "Any alternative names or abbreviations the company might be known by."
    const val COMPANY_ALTERNATIVE_NAMES_EXAMPLE = "\n[\"ABC Corp.\"\n]"

    const val COMPANY_CONTACT_DETAILS_DESCRIPTION = "The email addresses of the company."
    const val COMPANY_CONTACT_DETAILS_EXAMPLE = "\n[\"contact@abccorp.com\"\n]"

    const val COMPANY_LEGAL_FORM_DESCRIPTION = "The legal structure under which the company operates."
    const val COMPANY_LEGAL_FORM_EXAMPLE = "Private Limited Company (Ltd)"

    const val HEADQUARTERS_DESCRIPTION = "The city where the registered office of the company is located."
    const val HEADQUARTERS_EXAMPLE = "Berlin"

    const val HEADQUARTERS_POSTAL_CODE_DESCRIPTION = "The postal code of the headquarters."
    const val HEADQUARTERS_POSTAL_CODE_EXAMPLE = "10123"

    const val SECTOR_DESCRIPTION = "The industry or sector in which the company operates."
    const val SECTOR_EXAMPLE = "web services"

    const val SECTOR_CODE_WZ_DESCRIPTION = "The industry classification code according to the NACE compliant WZ method."
    const val SECTOR_CODE_WZ_EXAMPLE = "62.10.4"

    const val IDENTIFIERS_DESCRIPTION = "Unique identifiers associated with the company, such as LEI, PermId, ..."
    const val IDENTIFIERS_EXAMPLE = "{\"Lei\":[\"5493001KJX4BT0IHAG73\"]}"

    const val COUNTRY_CODE_DESCRIPTION = "The ISO 3166-1 alpha-2 code representing the country of origin."
    const val COUNTRY_CODE_EXAMPLE = "DE"

    const val IS_TEASER_COMPANY_DESCRIPTION = "A boolean indicating if the company is a teaser company."
    const val IS_TEASER_COMPANY_EXAMPLE = "true"

    const val WEBSITE_DESCRIPTION = "The official website URL of the company."
    const val WEBSITE_EXAMPLE = "www.abccorp.com test"

    const val PARENT_COMPANY_LEI_DESCRIPTION = "The LEI of the parent company, if applicable."
    const val PARENT_COMPANY_LEI_EXAMPLE = "5493001KJX4BT0IHAG72"

    const val COMPANY_ID_DESCRIPTION = "The ID under which the company can be found on Dataland."
    const val COMPANY_ID_EXAMPLE = "c9710c7b-9cd6-446b-85b0-3773d2aceb48"

    const val DATA_ID_DESCRIPTION = "The unique identifier to identify the data in the data store."
    const val DATA_ID_EXAMPLE = "1e63a842-1e65-43ed-b78a-5e7cec155c28"

    const val DATA_TYPE_DESCRIPTION = "The framework of the dataset on Dataland."
    const val DATA_TYPE_EXAMPLE = "sfdr"

    const val CURRENTLY_ACTIVE_DESCRIPTION = "Boolean flag whether the dataset is currently active."
    const val CURRENTLY_ACTIVE_EXAMPLE = "true"

    const val QA_STATUS_DESCRIPTION =
        "The status of the document with regard to Dataland's Quality Assurance process. " +
            "Possible values are: Pending, Accepted, Rejected."

    const val REF_DESCRIPTION = "The direct link to the page displaying the specified dataset."
    const val REF_EXAMPLE = "https://dataland.com/companies/$COMPANY_ID_EXAMPLE/frameworks/eutaxonomy-non-financials/$DATA_ID_EXAMPLE"

    const val LEI_DESCRIPTION = "The LEI of the company."
    const val LEI_EXAMPLE = "5493001KJX4BT0IHAG73"

    const val SINGLE_IDENTIFIER_DESCRIPTION = "Unique identifier to find a company."
    const val SINGLE_IDENTIFIER_EXAMPLE = "5493001KJX4BT0IHAG73"

    const val IDENTIFIER_TYPE_DESCRIPTION = "The type of identifier that is used."

    const val NUMBER_OF_PROVIDED_REPORTING_PERIODS_DESCRIPTION = "The number of reporting periods that are used."
    const val NUMBER_OF_PROVIDED_REPORTING_PERIODS_EXAMPLE = "2"

    const val LIST_OF_COUNTRY_CODES_DESCRIPTION = "The list of country codes in ISO 3166-1 alpha-2 format."
    const val LIST_OF_COUNTRY_CODES_EXAMPLE = "\n[\"$COUNTRY_CODE_EXAMPLE\"\n]"

    const val LIST_OF_SECTORS_DESCRIPTION = "The list of sectors."
    const val LIST_OF_SECTORS_EXAMPLE = "\n[\"$SECTOR_EXAMPLE\"\n]"

    const val REPORTING_PERIOD_DESCRIPTION = "The reporting period the document belongs to (e.g. a fiscal year)."
    const val REPORTING_PERIOD_EXAMPLE = "2023"

    const val UPLOADER_USER_ID_DESCRIPTION = "The Dataland user ID of the user who uploaded the document."
    const val UPLOADER_USER_ID_EXAMPLE = "814caf16-54de-4385-af6e-bd6b64b64634"

    const val UPLOAD_TIME_DESCRIPTION = "The timestamp (epoch milliseconds) at which the document was uploaded to Dataland."
    const val UPLOAD_TIME_EXAMPLE = "1751291891271"

    const val CHUNK_SIZE_DESCRIPTION =
        "Only a chunk of all matching, ordered results is returned. This parameter specifies the maximum size " +
            "of a single chunk. All chunks except possibly the last will have that size."

    const val CHUNK_INDEX_DESCRIPTION =
        "Only a chunk of all matching, ordered results is returned. This parameter specifies the number of the " +
            "returned chunk in the ordering, with counting starting at 0. The default value is 0, i.e., by " +
            "default, the first chunk (containing the meta information of the latest published documents) is " +
            "returned."

    const val IS_NON_SOURCEABLE_DESCRIPTION =
        "If true, the method only returns meta info for datasets which are" +
            "non-sourceable and if false, it returns sourceable data."
    const val IS_NON_SOURCEABLE_EXAMPLE = "true"

    const val REASON_DESCRIPTION = "The reason why there is no source available"
    const val REASON_EXAMPLE = "Parent Uploaded"

    const val CREATION_TIME_DESCRIPTION = "The timestamp (epoch milliseconds) at which the dataset was created."
    const val CREATION_TIME_EXAMPLE = "1751291891271"

    const val IS_ONLY_ACTIVE_DESCRIPTION = "Boolean flag whether the dataset is currently the only active dataset."
    const val IS_ONLY_ACTIVE_EXAMPLE = "true"

    const val ALL_UPLOADER_USER_IDS_DESCRIPTION = "A set of Dataland user IDs of the users who uploaded the document."

    const val SHOW_ONLY_ACTIVE_DESCRIPTION =
        "Boolean parameter. If set to true or empty, only metadata of QA reports " +
            "are returned that are active. If set to false, all QA reports will be returned regardless of their active status."

    const val DATA_POINT_ID_DESCRIPTION = "The unique identifier to identify the datapoint."
    const val DATA_POINT_ID_EXAMPLE = "2a7fa2ba-a65f-4b5d-8c7b-423bcfb0d41d"

    const val DATA_POINT_DESCRIPTION = "The content of the data point as a JSON string."
    const val DATA_POINT_EXAMPLE =
        "{\"value\":\"No\",\"quality\":\"Incomplete\",\"comment\":\"program neural circuit\"," +
            "\"dataSource\":{\"page\":\"1026\",\"tagName\":\"web services\",\"fileName\":\"SustainabilityReport\"," +
            "\"fileReference\":\"1902e40099c913ecf3715388cb2d9f7f84e6f02a19563db6930adb7b6cf22868\",\"publicationDate\":\"2024-01-07\"}}"

    const val DATA_POINT_TYPE_DESCRIPTION = "Which data point the provided content is associated to."
    const val DATA_POINT_TYPE_EXAMPLE = "extendedEnumYesNoNfrdMandatory"

    const val LIST_OF_DATA_POINT_IDS_EXAMPLE =
        "{" +
            "\"extendedDecimalRevenueSubstantialContributionToClimateChangeAdaptationInPercentEnablingShare\"" +
            ":\"68c9ced1-47cb-4240-9014-90234f1ad6d6\"," +
            "\"extendedDecimalCapexSubstantialContributionToTransitionToACircularEconomyInPercentEnablingShare\"" +
            ":\"56607e79-1caf-4448-aadd-1823583de314\"," +
            "\"extendedDecimalCapexSubstantialContributionToPollutionPreventionAndControlInPercentEligible\"" +
            ":\"03eedebf-f882-4d2d-b481-0e233dc641fb\"," +
            "\"...\":\"...\"" +
            "}"

    const val AGGREGATED_FRAMEWORK_DATA_SUMMARY_EXAMPLE =
        "{" +
            "\"sfdr\":{" +
            "\"numberOfProvidedReportingPeriods\":0" +
            "}," +
            "  \"eutaxonomy-financials\":{" +
            "    \"numberOfProvidedReportingPeriods\":0" +
            "  }," +
            "  \"eutaxonomy-non-financials\":{" +
            "    \"numberOfProvidedReportingPeriods\":1" +
            "  }," +
            "  \"nuclear-and-gas\":{" +
            "    \"numberOfProvidedReportingPeriods\":0" +
            "  }" +
            "}"

    const val COMMENT_DESCRIPTION = "Optional comment to explain the QA review status change."
    const val COMMENT_EXAMPLE = "comment"

    const val OVERWRITE_DATA_POINT_QA_STATUS_DESCRIPTION =
        "Boolean flag. If true, the QA status of the data points are overwritten."

    const val REVIEWER_ID_DESCRIPTION = "The unique user ID of the user who uploaded the review."
    const val REVIEWER_ID_EXAMPLE = UPLOADER_USER_ID_EXAMPLE

    const val REVIEW_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) at which the dataset was reviewed."
    const val REVIEW_TIMESTAMP_EXAMPLE = UPLOAD_TIME_EXAMPLE

    const val QA_REPORT_ID_DESCRIPTION = "The unique identifier of the QA report"
    const val QA_REPORT_ID_EXAMPLE = "3f87b4ac-b3e2-4f7d-95d3-8e20c9ad6f1e"

    const val REPORTER_USER_ID_DESCRIPTION = "The unique user ID of the user who uploaded this QA report."
    const val REPORTER_USER_ID_EXAMPLE = UPLOADER_USER_ID_EXAMPLE

    const val QA_REPORT_UPLOAD_TIME_DESCRIPTION = "The timestamp (epoch milliseconds) at which the QA report was uploaded."
    const val QA_REPORT_UPLOAD_TIME_EXAMPLE = UPLOAD_TIME_EXAMPLE

    const val IS_REPORT_ACTIVE_DESCRIPTION = "Boolean flag. True if and only if the QA report is marked as active."
    const val IS_REPORT_ACTIVE_EXAMPLE = "true"

    const val QA_REPORT_DATA_POINT_VERDICT_DESCRIPTION =
        "The quality decision of this qa report. " +
            "Possible values are: QaAccepted, QaRejected, QaInconclusive, QaNotAttempted."

    const val QA_REPORT_CORRECTED_DATA_DESCRIPTION = "If rejected, contains suggested data corrections for the data point."
    const val QA_REPORT_CORRECTED_DATA_EXAMPLE = DATA_POINT_EXAMPLE

    const val QA_REPORT_COMMENT_DESCRIPTION = "A comment explaining the verdict."
    const val QA_REPORT_COMMENT_EXAMPLE = "comment"

    const val QA_REPORT_SHOW_INACTIVE_DESCRIPTION = "Boolean flag to indicate if inactive QA reports should be included in the response."

    const val QA_REPORT_SHOW_ONLY_ACTIVE_DESCRIPTION = "Boolean flag. If true, only active QA reports are included in the response."

    const val QA_REPORT_MIN_UPLOAD_DATE_DESCRIPTION =
        "If set, only metadata of reports are returned that were uploaded after the minUploadDate."
    const val QA_REPORT_MIN_UPLOAD_DATE_EXAMPLE = "01-01-2024"

    const val QA_REPORT_MAX_UPLOAD_DATE_DESCRIPTION =
        "If set, only metadata of reports are returned that were uploaded before the maxUploadDate."
    const val QA_REPORT_MAX_UPLOAD_DATE_EXAMPLE = "01-01-2025"
}
