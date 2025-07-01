package org.dataland.datalandbackend.utils

object CompanyControllerDescriptionsAndExamples {
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
    const val IDENTIFIERS_EXAMPLE = "\n{\n\t\"Lei\": [\"5493001KJX4BT0IHAG73\"]\n}"

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

    const val CURRENTLY_ACTIVE_DESCRIPTION = "Boolean flag whether the dataset is currently active."
    const val CURRENTLY_ACTIVE_EXAMPLE = "true"

    const val QA_STATUS_DESCRIPTION =
        "The status of the document with regard to Dataland's Quality Assurance process. " +
            "Possible values are: Pending, Accepted, Rejected."

    const val REF_DESCRIPTION = "The direct link to the page displaying the specified dataset."
    const val REF_EXAMPLE = "null"

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
            "of a single chunk. All chunks except possibly the last will have that size. The default " +
            "value is 100."

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
    const val REASON_EXAMPLE = "null"

    const val CREATION_TIME_DESCRIPTION = "The timestamp (epoch milliseconds) at which the dataset was created."
    const val CREATION_TIME_EXAMPLE = "1751291891271"

    const val IS_ONLY_ACTIVE_DESCRIPTION = "Boolean flag whether the dataset is currently the only active dataset."
    const val IS_ONLY_ACTIVE_EXAMPLE = "true"

    const val ALL_UPLOADER_USER_IDS_DESCRIPTION = "A set of Dataland user IDs of the users who uploaded the document."

    const val SHOW_ONLY_ACTIVE_DESCRIPTION = "Boolean parameter. If set to true, the results will only display active datasets."
}
