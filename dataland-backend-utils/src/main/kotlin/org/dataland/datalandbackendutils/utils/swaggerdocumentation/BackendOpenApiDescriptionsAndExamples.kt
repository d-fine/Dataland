package org.dataland.datalandbackendutils.utils.swaggerdocumentation

object BackendOpenApiDescriptionsAndExamples {
    const val COMPANY_ALTERNATIVE_NAMES_DESCRIPTION =
        "Any alternative names or abbreviations the company might be known by."
    const val COMPANY_ALTERNATIVE_NAMES_EXAMPLE = "[\"ABC Corp.\"]"

    const val COMPANY_CONTACT_DETAILS_DESCRIPTION = "The email addresses of the company."
    const val COMPANY_CONTACT_DETAILS_EXAMPLE = "[\"${GeneralOpenApiDescriptionsAndExamples.GENERAL_EMAIL_EXAMPLE}\"]"

    const val COMPANY_LEGAL_FORM_DESCRIPTION = "The legal structure under which the company operates."
    const val COMPANY_LEGAL_FORM_EXAMPLE = "Private Limited Company (Ltd)"

    const val HEADQUARTERS_DESCRIPTION = "The city where the registered office of the company is located."
    const val HEADQUARTERS_EXAMPLE = "Berlin"

    const val HEADQUARTERS_POSTAL_CODE_DESCRIPTION = "The postal code of the headquarters."
    const val HEADQUARTERS_POSTAL_CODE_EXAMPLE = "10123"

    const val FISCAL_YEAR_END_DESCRIPTION = "The fiscal year-end of the company."
    const val FISCAL_YEAR_END_EXAMPLE = "31-Mar"

    const val REPORTING_PERIOD_SHIFT_DESCRIPTION = "Difference between fiscal year-end and reporting period."
    const val REPORTING_PERIOD_SHIFT_EXAMPLE = "-1"

    const val SECTOR_DESCRIPTION = "The industry or sector in which the company operates."
    const val SECTOR_EXAMPLE = "Information Technology"
    const val SECTOR_LIST_EXAMPLE = "[\"$SECTOR_EXAMPLE\"]"

    const val SECTOR_CODE_WZ_DESCRIPTION = "The industry classification code according to the NACE compliant WZ method."
    const val SECTOR_CODE_WZ_EXAMPLE = "62.10.4"

    const val IDENTIFIERS_DESCRIPTION = "Unique identifiers associated with the company, such as LEI, PermId, ..."
    const val IDENTIFIERS_EXAMPLE =
        "[\"" +
            "${GeneralOpenApiDescriptionsAndExamples.GENERAL_LEI_EXAMPLE}\"," +
            "\"${GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE}" +
            "\"]"

    const val IDENTIFIER_MAP_DESCRIPTION = IDENTIFIERS_DESCRIPTION
    const val IDENTIFIER_MAP_EXAMPLE = "{\"Lei\":[\"${GeneralOpenApiDescriptionsAndExamples.GENERAL_LEI_EXAMPLE}\"]}"

    const val COUNTRY_CODE_DESCRIPTION = "The ISO 3166-1 alpha-2 code representing the country of origin."
    const val COUNTRY_CODE_EXAMPLE = "DE"
    const val COUNTRY_CODE_LIST_EXAMPLE = "[\"$COUNTRY_CODE_EXAMPLE\"]"

    const val IS_TEASER_COMPANY_DESCRIPTION = "A boolean indicating if the company is a teaser company."

    const val WEBSITE_DESCRIPTION = "The official website URL of the company."
    const val WEBSITE_EXAMPLE = "www.abccorp.com"

    const val PARENT_COMPANY_LEI_DESCRIPTION = "The LEI of the parent company if it exists."
    const val PARENT_COMPANY_LEI_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_LEI_EXAMPLE

    const val DATA_ID_DESCRIPTION = "The unique identifier of the dataset."
    const val DATA_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val CURRENTLY_ACTIVE_DESCRIPTION = "Boolean flag indicating whether the dataset is currently active."

    const val REF_DESCRIPTION = "The direct link to the page displaying the specified dataset."
    const val REF_EXAMPLE =
        "https://dataland.com/companies/${GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE}/" +
            "frameworks/sfdr/$DATA_ID_EXAMPLE"

    const val LEI_DESCRIPTION = "The LEI of the company."
    const val LEI_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_LEI_EXAMPLE

    const val IDENTIFIER_TYPE_DESCRIPTION = "The type of identifier that is used."

    const val AGGREGATED_FRAMEWORK_DATA_SUMMARY_DESCRIPTION = "The amount of available reporting periods per framework."
    const val AGGREGATED_FRAMEWORK_DATA_SUMMARY_EXAMPLE =
        "{\"${GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE}\":2}"

    const val LIST_OF_COUNTRY_CODES_DESCRIPTION = "The list of country codes in ISO 3166-1 alpha-2 format."
    const val LIST_OF_COUNTRY_CODES_EXAMPLE = "[\"$COUNTRY_CODE_EXAMPLE\"]"

    const val LIST_OF_SECTORS_DESCRIPTION = "The list of sectors."
    const val LIST_OF_SECTORS_EXAMPLE = "[\"$SECTOR_EXAMPLE\"]"

    const val UPLOADER_USER_ID_DESCRIPTION = "The Dataland user ID of the user who uploaded the dataset."
    const val UPLOADER_USER_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val IS_NON_SOURCEABLE_DESCRIPTION =
        "If true, the method only returns meta info for datasets which are" +
            "non-sourceable. If false, it returns sourceable data."

    const val REASON_DESCRIPTION = "The reason why there is no source available"
    const val REASON_EXAMPLE = "Parent Uploaded"

    const val CREATION_TIME_DESCRIPTION = "The timestamp (epoch milliseconds) at which the dataset was created."
    const val CREATION_TIME_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE

    const val ALL_UPLOADER_USER_IDS_DESCRIPTION =
        "A list of Dataland user IDs corresponding to the users who uploaded the data."
    const val ALL_UPLOADER_USER_IDS_EXAMPLE = "[\"$UPLOADER_USER_ID_EXAMPLE\"]"

    const val SHOW_ONLY_ACTIVE_DESCRIPTION =
        "Boolean parameter. If set to true or empty, only currently active data is returned. " +
            "If set to false, all data is returned regardless of active status."

    const val DATA_POINT_ID_DESCRIPTION = "The unique identifier of the datapoint."
    const val DATA_POINT_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val DATA_POINT_DESCRIPTION = "The data point as a JSON string."
    const val DATA_POINT_EXAMPLE =
        "{\"value\":\"No\",\"quality\":\"Incomplete\",\"comment\":\"program neural circuit\"," +
            "\"dataSource\":{\"page\":\"1026\",\"tagName\":\"web services\",\"fileName\":\"SustainabilityReport\"," +
            "\"fileReference\":\"1902e40099c913ecf3715388cb2d9f7f84e6f02a19563db6930adb7b6cf22868\",\"publicationDate\":\"2024-01-07\"}}"

    const val DATA_POINT_TYPE_DESCRIPTION = "The data point type of the provided data point."
    const val DATA_POINT_TYPE_EXAMPLE = "extendedEnumYesNoIsNfrdMandatory"

    const val REVIEWER_USER_NAME_DESCRIPTION = "The name of the user who is reviewing the dataset."
    const val REVIEWER_USER_NAME_EXAMPLE = "John Doe"

    const val NUMBER_QA_REPORTS_DESCRIPTION = "The number of QA reports associated with the dataset."
    const val NUMBER_QA_REPORTS_EXAMPLE = "5"

    const val FRAMEWORKS_OR_DATA_POINT_TYPES_DESCRIPTION =
        "A list of the frameworks of the wanted datasets and of the dataPointTypes of the wanted data points."
    const val FRAMEWORKS_OR_DATA_POINT_TYPES_EXAMPLE =
        "[\"" +
            "${GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE}\"," +
            "\"${GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DATA_POINT_TYPE_EXAMPLE}" +
            "\"]"

    const val BYPASS_QA_DESCRIPTION =
        "If true, data is not sent to QA."

    const val DATA_POINT_MAP_DESCRIPTION = "A map from data point IDs to the associated technical IDs."
    const val DATA_POINT_MAP_EXAMPLE =
        "{\"extendedEnumFiscalYearDeviation\":" +
            "\"${GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE}\"}"

    const val REPORTING_PERIODS_LIST_DESCRIPTION =
        "The reporting periods for which the data export is requested."
    const val REPORTING_PERIODS_LIST_EXAMPLE =
        GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_EXAMPLE

    const val COMPANY_IDS_LIST_DESCRIPTION =
        "A list of Dataland company ids for which the data export is requested."
    const val COMPANY_IDS_LIST_EXAMPLE =
        """["${GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE}",
            |"${GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE}"]"""

    const val FILE_FORMAT_DESCRIPTION =
        "The file format for the data export."

    const val KEEP_VALUE_FIELDS_ONLY_DESCRIPTION =
        "If set to true, data is to be exported without additional information like comments on the data or the data's qa status."

    const val ASSOCIATED_SUBDOMAINS_DESCRIPTION =
        "List of email subdomains associated with the company. Subdomain labels immediately follow the '@' and end immediately before " +
            "one of the subsequent periods '.'. They represent the root domain registered by the company together with subdomains " +
            "if applicable. They are used to suggest registered Dataland users when managing company roles."
    const val ASSOCIATED_SUBDOMAINS_EXAMPLE = "[\"my-company\",\"hr.my-company\"]"
}
