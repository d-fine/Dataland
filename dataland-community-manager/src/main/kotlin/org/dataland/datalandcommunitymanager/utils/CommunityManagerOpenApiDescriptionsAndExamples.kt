package org.dataland.datalandcommunitymanager.utils

object CommunityManagerOpenApiDescriptionsAndExamples {
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
    const val BULK_REQUEST_REPORTING_PERIODS_EXAMPLE =
        "[\"2023\",\"2024\"]"

    const val BULK_REQUEST_NOTIFY_ME_IMMEDIATELY_DESCRIPTION =
        "Whether an immediate notification email shall be sent when there is an update concerning one of " +
            "the posted requests. Otherwise, those updates are listed in the weekly summary email."
    const val BULK_REQUEST_NOTIFY_ME_IMMEDIATELY_EXAMPLE = "true"
}
