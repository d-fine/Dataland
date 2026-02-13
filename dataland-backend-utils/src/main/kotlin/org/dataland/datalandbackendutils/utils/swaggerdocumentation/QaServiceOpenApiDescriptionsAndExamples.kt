package org.dataland.datalandbackendutils.utils.swaggerdocumentation

import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples.DATA_POINT_EXAMPLE
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_EXAMPLE

object QaServiceOpenApiDescriptionsAndExamples {
    const val QA_STATUS_DESCRIPTION =
        "The status with regard to Dataland's quality assurance process."

    const val QA_REPORT_ID_DESCRIPTION = "The unique identifier of the QA report"
    const val QA_REPORT_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val QA_REPORT_DATA_POINT_VERDICT_DESCRIPTION = "The quality decision of this qa report."

    const val QA_REPORT_CORRECTED_DATA_DESCRIPTION = "Contains suggested data corrections for the rejected data point."
    const val QA_REPORT_CORRECTED_DATA_EXAMPLE = DATA_POINT_EXAMPLE

    const val QA_REPORT_COMMENT_DESCRIPTION = "A comment explaining the verdict."
    const val QA_REPORT_COMMENT_EXAMPLE = "The data point is correct and hence accepted."

    const val QA_REPORT_SHOW_INACTIVE_DESCRIPTION =
        "Boolean flag to indicate if inactive QA reports should be included in the response."

    const val QA_REPORT_SHOW_ONLY_ACTIVE_DESCRIPTION =
        "Boolean flag. If true, only active QA reports are included in the response."

    const val QA_REPORT_MIN_UPLOAD_DATE_DESCRIPTION =
        "If set, only metadata of reports that were uploaded after the minUploadDate are returned."
    const val QA_REPORT_MIN_UPLOAD_DATE_EXAMPLE = "01-01-2024"

    const val QA_REPORT_MAX_UPLOAD_DATE_DESCRIPTION =
        "If set, only metadata of reports that were uploaded before the maxUploadDate are returned."
    const val QA_REPORT_MAX_UPLOAD_DATE_EXAMPLE = "01-01-2025"

    const val REVIEW_TIMESTAMP_DESCRIPTION = "The timestamp (epoch milliseconds) at which the dataset was reviewed."
    const val REVIEW_TIMESTAMP_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE

    const val REPORTER_USER_ID_DESCRIPTION = "The unique user ID of the user who uploaded the QA report."
    const val REPORTER_USER_ID_EXAMPLE = UPLOADER_USER_ID_EXAMPLE
    const val REPORTER_USER_ID_LIST_EXAMPLE = "[\"$REPORTER_USER_ID_EXAMPLE\"]"

    const val REVIEWER_ID_DESCRIPTION = "The unique user ID of the user who uploaded the review."
    const val REVIEWER_ID_EXAMPLE = UPLOADER_USER_ID_EXAMPLE

    const val IS_REPORT_ACTIVE_DESCRIPTION = "Boolean flag. True if and only if the QA report is marked as active."
    const val QA_REPORT_UPLOAD_TIME_DESCRIPTION =
        "The timestamp (epoch milliseconds) at which the QA report was uploaded."
    const val QA_REPORT_UPLOAD_TIME_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE
    const val COMMENT_DESCRIPTION = "Optional comment to explain the QA review status change."
    const val COMMENT_EXAMPLE = "comment"
    const val OVERWRITE_DATA_POINT_QA_STATUS_DESCRIPTION =
        "Boolean flag. If true, the QA status of the data points are overwritten."

    const val DATA_REVIEW_ID_DESCRIPTION = "The unique identifier of the data review."
    const val DATA_REVIEW_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val DATA_REVIEW_STATE_DESCRIPTION = "The state of the dataset review."

    const val DATA_REVIEW_REVIEWER_ID_DESCRIPTION = "The unique user ID of the user reviewing the dataset."
    const val DATA_REVIEW_REVIEWER_EXAMPLE = UPLOADER_USER_ID_EXAMPLE

    const val DATA_REVIEW_REVIEWER_USERNAME_DESCRIPTION = "The name or email of the user reviewing the dataset."
    const val DATA_REVIEW_REVIEWER_USERNAME_EXAMPLE = "Jane Doe"

    const val DATA_REVIEW_PREAPPROVED_DATA_POINTS_DESCRIPTION = "Datapoints automatically approved by Dataland."
    const val DATA_REVIEW_PREAPPROVED_DATA_POINTS_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_SET_OF_UUIDS_EXAMPLE

    const val DATA_REVIEW_QA_REPORTS_DESCRIPTION = "QA reports associated to the dataset."
    const val DATA_REVIEW_QA_REPORTS_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_SET_OF_UUIDS_EXAMPLE

    const val DATA_REVIEW_APPROVED_DATAPOINT_IDS_DESCRIPTION = "Data points approved and to be accepted."
    const val DATA_REVIEW_APPROVED_DATAPOINT_IDS_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_TO_UUID_MAP_EXAMPLE

    const val DATA_REVIEW_APPROVED_QA_REPORT_IDS_DESCRIPTION = "QA reports approved and to be accepted."
    const val DATA_REVIEW_APPROVED_QA_REPORT_IDS_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_TO_UUID_MAP_EXAMPLE

    const val DATA_REVIEW_CUSTOM_DATAPOINTS_DESCRIPTION = "Custom datapoints as strings to be uploaded, approved and accepted."
    const val DATA_REVIEW_CUSTOM_DATAPOINTS_EXAMPLE = "{ \"extendedDateFiscalYearEnd\": \" 2026-12-31 \" }"
}
