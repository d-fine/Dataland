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
    const val SECTOR_EXAMPLE = "Information Technology"

    const val SECTOR_CODE_WZ_DESCRIPTION = "The industry classification code according to the NACE compliant WZ method."
    const val SECTOR_CODE_WZ_EXAMPLE = "62.10.4"

    const val IDENTIFIERS_DESCRIPTION = "Unique identifiers associated with the company, such as LEI, PermId, ..."
    const val IDENTIFIERS_EXAMPLE = "\n{\n\t\"Lei\": [\"5493001KJX4BT0IHAG73\"]\n}"

    const val COUNTRY_CODE_DESCRIPTION = "The ISO 3166-1 alpha-2 code representing the country of origin."
    const val COUNTRY_CODE_EXAMPLE = "DE"

    const val IS_TEASER_COMPANY_DESCRIPTION = "A boolean indicating if the company is a teaser company."
    const val IS_TEASER_COMPANY_EXAMPLE = "true"

    const val WEBSITE_DESCRIPTION = "The official website URL of the company."
    const val WEBSITE_EXAMPLE = "www.abccorp.com"

    const val PARENT_COMPANY_LEI_DESCRIPTION = "The LEI of the parent company, if applicable."
    const val PARENT_COMPANY_LEI_EXAMPLE = "null"

    const val COMPANY_ID_DESCRIPTION = "The ID under which the company can be found on Dataland."
    const val COMPANY_ID_EXAMPLE = "string"
}
