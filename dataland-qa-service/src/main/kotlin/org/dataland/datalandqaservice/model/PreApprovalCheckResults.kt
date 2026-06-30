package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

/**
 * Stores the outcome of each automatic pre-approval check for a data point.
 *
 * @property areAllQaReportsAccepted `true` if the data point has at least one QA report and all reports were accepted.
 * @property dataPointEligible `true` if the data point type is not excluded from automatic pre-approval.
 * @property passesRandomSampling `true` if the data point was not filtered out by random sampling.
 * @property passesSignificanceCheck `true` if the change from the live data point is not considered significant.
 */
data class PreApprovalCheckResults(
    val areAllQaReportsAccepted: Boolean,
    val dataPointEligible: Boolean,
    val passesRandomSampling: Boolean,
    val passesSignificanceCheck: Boolean,
) {
    /**
     * Returns whether all prerequisite checks for automatic pre-approval passed.
     */
    fun passes(): Boolean = areAllQaReportsAccepted && passesRandomSampling && dataPointEligible && passesSignificanceCheck
}
