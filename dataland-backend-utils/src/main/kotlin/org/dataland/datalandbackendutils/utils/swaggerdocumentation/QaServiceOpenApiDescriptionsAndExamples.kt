package org.dataland.datalandbackendutils.utils.swaggerdocumentation

import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples.DATA_POINT_EXAMPLE
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_EXAMPLE

object QaServiceOpenApiDescriptionsAndExamples {
    const val QA_STATUS_DESCRIPTION =
        "The status with regard to Dataland's quality assurance process."

    const val QA_REPORT_ID_DESCRIPTION = "The unique identifier of the QA report"
    const val QA_REPORT_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val QA_REPORT_DATA_POINT_VERDICT_DESCRIPTION = "The verdict of a QA report regarding a data point."

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

    const val REVIEWER_USER_ID_DESCRIPTION = "The unique user ID of the user who uploaded the QA report."
    const val REVIEWER_USER_ID_EXAMPLE = UPLOADER_USER_ID_EXAMPLE
    const val REVIEWER_USER_ID_LIST_EXAMPLE = "[\"$REVIEWER_USER_ID_EXAMPLE\"]"

    const val REVIEWER_USERNAME_EXAMPLE = "Bot_1_of_Dataland"
    const val REVIEWER_USERNAME_DESCRIPTION = "The name of the user who uploaded the QA report."

    const val REVIEWER_USER_EMAIL_EXAMPLE = "bot1@dataland.com"
    const val REVIEWER_USER_EMAIL_DESCRIPTION = "The email of the user who uploaded the QA report."

    const val JUDGE_ID_DESCRIPTION = "The unique user ID of the user who uploaded the review."
    const val JUDGE_ID_EXAMPLE = UPLOADER_USER_ID_EXAMPLE

    const val IS_REPORT_ACTIVE_DESCRIPTION = "Boolean flag. True if and only if the QA report is marked as active."
    const val QA_REPORT_UPLOAD_TIME_DESCRIPTION =
        "The timestamp (epoch milliseconds) at which the QA report was uploaded."
    const val QA_REPORT_UPLOAD_TIME_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE
    const val COMMENT_DESCRIPTION = "Optional comment to explain the QA review status change."
    const val COMMENT_EXAMPLE = "comment"
    const val OVERWRITE_DATA_POINT_QA_STATUS_DESCRIPTION =
        "Boolean flag. If true, the QA status of the data points are overwritten."

    const val DATA_JUDGEMENT_ID_DESCRIPTION = "The unique identifier of the data judgment."
    const val DATA_JUDGEMENT_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val DATA_JUDGEMENT_STATE_DESCRIPTION = "The state of the dataset judgement."

    const val DATA_JUDGEMENT_JUDGE_ID_DESCRIPTION = "The unique user ID of the user judging the dataset review."
    const val DATA_JUDGEMENT_JUDGE_ID_EXAMPLE = UPLOADER_USER_ID_EXAMPLE

    const val DATA_JUDGEMENT_JUDGE_USERNAME_DESCRIPTION = "The name or email of the user judging the dataset review."
    const val DATA_JUDGEMENT_JUDGE_USERNAME_EXAMPLE = "Jane Doe"

    const val DATA_JUDGEMENT_CUSTOM_DATAPOINTS_DESCRIPTION = "Custom datapoints as json string to be uploaded."
    const val DATA_JUDGEMENT_CUSTOM_DATAPOINTS_EXAMPLE = "{ \"extendedDateFiscalYearEnd\": \" 2026-12-31 \" }"

    const val NUMBER_QA_REPORTS_DESCRIPTION = "The number of QA reports associated with the dataset."
    const val NUMBER_QA_REPORTS_EXAMPLE = "5"

    const val QA_REPORTER_DESCRIPTION = "List of reporters that contributed a QA report to this dataset."
    const val QA_REPORTER_EXAMPLE =
        "[{ \"reporterUserId\": \"c9710c7b-9cd6-446b-85b0-3773d2aceb48\", " +
            "\"reporterUserName\": \"Data Reviewer\", " +
            "\"reporterEmailAddress\": \"data.reviewer@dataland.com\" }]"

    const val DATA_POINTS_MAP_DESCRIPTION =
        "Map with details for all datapoints in the dataset. " +
            "The key is the datapoint type."

    const val DATA_POINT_ID_DESCRIPTION = "The unique identifier of the datapoint."
    const val DATA_POINT_ID_EXAMPLE = "32c30bc5-ecfd-46ec-b849-628d5328e2e6"

    const val QA_REPORTS_DESCRIPTION =
        "List of QA reports associated with this data point, including details " +
            "about the reporter company."

    const val ACCEPTED_REPORTER_USER_ID_DESCRIPTION =
        "The unique identifier of the user whose QA report was " +
            "accepted for this data point, if applicable."
    const val ACCEPTED_REPORTER_USER_ID_EXAMPLE = UPLOADER_USER_ID_EXAMPLE

    const val REASON_FOR_CUSTOM_DATA_POINT_DESCRIPTION =
        "An optional comment explaining why a custom data point was created instead of " +
            "accepting the original uploaded value or the QA bot suggestion. " +
            "Only relevant when acceptedSource is Custom; null for all other sources."
    const val REASON_FOR_CUSTOM_DATA_POINT_EXAMPLE = "The original value was incorrect and the QA report claimed there was no data found."

    const val PRE_APPROVAL_CHECK_RESULTS_DESCRIPTION =
        "Structured results of the automatic pre-approval checks for this data point. " +
            "Each flag indicates whether the data point passed one prerequisite for automatic pre-approval of the original value."
    const val PRE_APPROVAL_CHECK_RESULTS_EXAMPLE =
        "{ \"areAllQaReportsAccepted\": true, \"dataPointEligible\": true, " +
            "\"passesRandomSampling\": true, \"passesSignificanceCheck\": true }"

    const val PRE_APPROVAL_EXEMPT_FIELDS_DESCRIPTION =
        "Map of framework to the set of data point type identifiers that are exempt from automatic pre-approval " +
            "for that framework. Exempt fields must always be reviewed manually, regardless of their QA report verdicts."
    const val PRE_APPROVAL_EXEMPT_FIELDS_EXAMPLE =
        "{ \"sfdr\": [\"extendedDecimalScope3UpstreamGhgEmissionsInTonnes\"] }"

    const val PRE_APPROVAL_SAMPLING_PROBABILITY_DESCRIPTION =
        "The probability (between 0.0 and 1.0) with which an eligible data point is randomly selected for " +
            "automatic pre-approval."
    const val PRE_APPROVAL_SAMPLING_PROBABILITY_EXAMPLE = "0.25"

    const val PRE_APPROVAL_DECIMAL_RELATIVE_THRESHOLD_DESCRIPTION =
        "The global relative change threshold for decimal data points. If the relative change between the " +
            "reviewed value and the currently live value exceeds this threshold, the change is considered significant " +
            "and automatic pre-approval is suppressed, unless an individual override applies (see " +
            "individualDecimalThresholds)."
    const val PRE_APPROVAL_DECIMAL_RELATIVE_THRESHOLD_EXAMPLE = "0.5"

    const val PRE_APPROVAL_INTEGER_ABSOLUTE_THRESHOLD_DESCRIPTION =
        "The global absolute change threshold for integer data points. If the absolute change between the " +
            "reviewed value and the currently live value exceeds this threshold, the change is considered significant " +
            "and automatic pre-approval is suppressed, unless an individual override applies (see " +
            "individualIntegerThresholds)."
    const val PRE_APPROVAL_INTEGER_ABSOLUTE_THRESHOLD_EXAMPLE = "5"

    const val PRE_APPROVAL_INDIVIDUAL_DECIMAL_THRESHOLDS_DESCRIPTION =
        "Per-data-point relative threshold overrides for decimal fields, keyed by framework and then by data " +
            "point type identifier. If no override is present for a given framework/data point type, " +
            "decimalRelativeThreshold is used instead."
    const val PRE_APPROVAL_INDIVIDUAL_DECIMAL_THRESHOLDS_EXAMPLE =
        "{ \"sfdr\": { \"extendedDecimalScope3UpstreamGhgEmissionsInTonnes\": 0.3 } }"

    const val PRE_APPROVAL_INDIVIDUAL_INTEGER_THRESHOLDS_DESCRIPTION =
        "Per-data-point absolute threshold overrides for integer fields, keyed by framework and then by data " +
            "point type identifier. If no override is present for a given framework/data point type, " +
            "integerAbsoluteThreshold is used instead."
    const val PRE_APPROVAL_INDIVIDUAL_INTEGER_THRESHOLDS_EXAMPLE =
        "{ \"sfdr\": { \"extendedIntegerCasesOfInsufficientActionAgainstBriberyAndCorruption\": 2 } }"

    const val PRE_APPROVAL_AUTO_PRE_APPROVAL_ENABLED_DESCRIPTION =
        "Boolean flag. If true, automatic pre-approval of QA-accepted data points is enabled. If false, all data " +
            "points require manual review regardless of the other pre-approval checks."
    const val PRE_APPROVAL_AUTO_PRE_APPROVAL_ENABLED_EXAMPLE = "true"

    const val PRE_APPROVAL_SUBMIT_USER_ID_DESCRIPTION =
        "The unique user ID of the reviewer or admin who last submitted (created or updated) this pre-approval " +
            "configuration. Null if the configuration has never been updated since being seeded. This field is " +
            "read-only and can only be set server-side; it cannot be set via the PATCH or PUT request bodies."
    const val PRE_APPROVAL_SUBMIT_USER_ID_EXAMPLE = UPLOADER_USER_ID_EXAMPLE
}
