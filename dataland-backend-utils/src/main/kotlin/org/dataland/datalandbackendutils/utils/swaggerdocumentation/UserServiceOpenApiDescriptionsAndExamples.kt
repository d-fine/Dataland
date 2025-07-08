package org.dataland.datalandbackendutils.utils.swaggerdocumentation

object UserServiceOpenApiDescriptionsAndExamples {
    const val PORTFOLIO_ID_DESCRIPTION = "The unique identifier to identify the portfolio."
    const val PORTFOLIO_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val PORTFOLIO_NAME_DESCRIPTION = "The name of the portfolio on Dataland."
    const val PORTFOLIO_NAME_EXAMPLE = "My Portfolio"

    const val PORTFOLIO_USER_ID_DESCRIPTION = "The user ID of the user who created the portfolio."
    const val PORTFOLIO_USER_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val PORTFOLIO_CREATION_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) at which the portfolio was created."
    const val PORTFOLIO_CREATION_TIMESTAMP_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE

    const val PORTFOLIO_LAST_UPDATE_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) at which the portfolio was last updated."
    const val PORTFOLIO_LAST_UPDATE_TIMESTAMP_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE

    const val PORTFOLIO_COMPANY_IDS_DESCRIPTION = "A list of company IDs that are contained in the portfolio."
    const val PORTFOLIO_COMPANY_IDS_EXAMPLE = "[\"${GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE}\"]"

    const val PORTFOLIO_IS_MONITORED_DESCRIPTION = "Boolean flag that indicates whether the portfolio is monitored."
    const val PORTFOLIO_IS_MONITORED_EXAMPLE = "true"

    const val PORTFOLIO_STARTING_MONITORING_PERIOD_DESCRIPTION =
        "The reporting period from which the companies in the portfolio are actively monitored for data updates."
    const val PORTFOLIO_STARTING_MONITORING_PERIOD_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE

    const val PORTFOLIO_MONITORED_FRAMEWORKS_DESCRIPTION =
        "A list of frameworks for which the companies in the portfolio are actively monitored."
    const val PORTFOLIO_MONITORED_FRAMEWORKS_EXAMPLE = "[\"${GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE}\"]"

    const val AVAILABLE_REPORTING_PERIODS_DESCRIPTION = "The reporting periods that are available per framework."
    const val AVAILABLE_REPORTING_PERIODS_EXAMPLE =
        "{\"${GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE}\":\"2024, 2023, 2022\"}"

    const val COMPANY_COCKPIT_REF_DESCRIPTION = "The link to the company cockpit page."
    const val COMPANY_COCKPIT_REF_EXAMPLE = "https://dataland.com/companies/${GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE}"

    const val FRAMEWORK_HYPHENATED_NAMES_TO_DATA_REF_DESCRIPTION = "The links to the data pages per framework."
    const val FRAMEWORK_HYPHENATED_NAMES_TO_DATA_REF_EXAMPLE =
        "{\"${GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE}\":" +
            "\"https://dataland.com/companies/${GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE}" +
            "/frameworks/${GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE}\"}"
}
