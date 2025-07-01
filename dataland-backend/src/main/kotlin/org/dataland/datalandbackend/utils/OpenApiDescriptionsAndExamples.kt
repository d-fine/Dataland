package org.dataland.datalandbackend.utils

object OpenApiDescriptionsAndExamples {
    const val BYPASS_QA_DESCRIPTION =
        "Boolean to decide whether data is sent to QA or not."
    const val BYPASS_QA_EXAMPLE = "false"

    const val DATA_ID_DESCRIPTION =
        "The ID under which the dataset can be found on Dataland. It is contained in the HTTP response " +
            "after posting a new dataset. It is not available for data points that are not posted as part of a dataset."
    const val DATA_ID_EXAMPLE =
        "3e35a2fa-b1d4-4c2c-b5b7-44f2b12865c2"

    const val COMPANY_ID_DESCRIPTION =
        "A unique ID for the company within Dataland (not the LEI)."
    const val COMPANY_ID_EXAMPLE =
        "536a58ed-bbea-4a12-b9f8-b20508582a0e"

    const val REPORTING_PERIOD_DESCRIPTION =
        "The reporting period associated with the data (e.g. fiscal year)."
    const val REPORTING_PERIOD_EXAMPLE =
        "2022"

    const val REPORTING_PERIODS_DESCRIPTION =
        "The reporting periods for which the data export is requested."
    const val REPORTING_PERIODS_EXAMPLE = """["2020", "2022", "2023"]"""

    const val COMPANY_IDS_DESCRIPTION =
        "A list of Dataland company ids for which the data export is requested"
    const val COMPANY_IDS_EXAMPLE =
        """["536a58ed-bbea-4a12-b9f8-b20508582a0e", "90ffd580-b99c-47f2-8955-dc5ff28549fe"]"""

    const val FILE_FORMAT_DESCRIPTION =
        "The file format for the data export."
    const val FILE_FORMAT_EXAMPLE = "CSV"

    const val KEEP_VALUE_FIELDS_ONLY_DESCRIPTION =
        "true, if data is to be exported without additional information like comments on the data or the data's qa status."
    const val KEEP_VALUE_FIELDS_ONLY_EXAMPLE = "true"

    const val SHOW_ONLY_ACTIVE_DESCRIPTION =
        "If set to true, only active datasets will be returned (e.g. no outdated ones)."
    const val SHOW_ONLY_ACTIVE_EXAMPLE = "true"
}
