package org.dataland.datalandmessagequeueutils.messages.data

/**
 * The payload for an event announcing that a company is missing its fiscal year end and/or its sector
 */
data class CompanyInformationIncompletePayload(
    val companyId: String,
)
