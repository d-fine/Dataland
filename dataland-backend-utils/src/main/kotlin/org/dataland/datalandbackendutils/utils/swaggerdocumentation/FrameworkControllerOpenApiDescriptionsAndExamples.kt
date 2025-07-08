package org.dataland.datalandbackendutils.utils.swaggerdocumentation

object FrameworkControllerOpenApiDescriptionsAndExamples {
    const val BYPASS_QA_DESCRIPTION =
        "If true, data is not sent to QA."
    const val BYPASS_QA_EXAMPLE = "false"

    const val REPORTING_PERIODS_DESCRIPTION =
        "The reporting periods for which the data export is requested."
    const val REPORTING_PERIODS_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_REPORTING_PERIODS_LIST_EXAMPLE

    const val COMPANY_IDS_DESCRIPTION =
        "A list of Dataland company ids for which the data export is requested."
    const val COMPANY_IDS_EXAMPLE =
        """["${GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE}",
            |"${GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE}"]"""

    const val FILE_FORMAT_DESCRIPTION =
        "The file format for the data export."

    const val KEEP_VALUE_FIELDS_ONLY_DESCRIPTION =
        "If set to true, data is to be exported without additional information like comments on the data or the data's qa status."
    const val KEEP_VALUE_FIELDS_ONLY_EXAMPLE = "true"

    const val SHOW_ONLY_ACTIVE_DESCRIPTION =
        "Boolean parameter. If set to true or empty, only currently active data is returned. " +
            "If set to false, all data is returned regardless of active status."
    const val SHOW_ONLY_ACTIVE_EXAMPLE = "true"
}
