package org.dataland.datalandemailservice.services.templateemail

/**
 * abstract factory email class for a data request response
 */

abstract class DataRequestResponseEmailBaseFactory protected constructor(
    proxyPrimaryUrl: String,
    senderEmail: String,
    senderName: String,
) : TemplateEmailFactory(
        proxyPrimaryUrl = proxyPrimaryUrl,
        senderEmail = senderEmail,
        senderName = senderName,
    ) {
    protected object Keys {
        const val COMPANY_ID = "companyId"
        const val COMPANY_NAME = "companyName"
        const val DATA_TYPE = "dataType"
        const val REPORTING_PERIOD = "reportingPeriod"
        const val CREATION_DATE = "creationDate"
        const val DATA_REQUEST_ID = "dataRequestId"
        const val CLOSED_IN_DAYS = "closedInDays"
        const val DATA_TYPE_DESCRIPTION = "dataTypeDescription"
    }

    override val requiredProperties =
        setOf(
            Keys.COMPANY_ID, Keys.COMPANY_NAME, Keys.DATA_TYPE, Keys.REPORTING_PERIOD, Keys.CREATION_DATE,
            Keys.DATA_REQUEST_ID, Keys.CLOSED_IN_DAYS,
        )
    override val optionalProperties = setOf(Keys.DATA_TYPE_DESCRIPTION)
}
