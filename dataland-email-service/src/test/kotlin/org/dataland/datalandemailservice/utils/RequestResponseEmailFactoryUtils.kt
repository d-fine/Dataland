package org.dataland.datalandemailservice.utils

import org.dataland.datalandemailservice.email.Email
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.UUID

private const val PROXY_PRIMARY_URL = "local-dev.dataland.com"
private const val COMPANY_NAME = "Test Inc."
private const val REPORTING_PERIOD = "2022"
private const val COMPANY_ID = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
private const val DATA_TYPE = "sfdr"
private const val DATA_TYPE_DESCRIPTION = "SFDR"
private const val CREATION_TIMESTAMP_AS_DATE = "07 Mar 2024, 15:03"
private val DATA_REQUEST_ID = UUID.randomUUID().toString()
private const val CLOSED_IN_DAYS = "100"

fun getProperties(setOptionalProperties: Boolean): Map<String, String> {
    var properties =
        mapOf(
            "companyId" to COMPANY_ID,
            "companyName" to COMPANY_NAME,
            "dataRequestId" to DATA_REQUEST_ID,
            "dataType" to DATA_TYPE,
            "reportingPeriod" to REPORTING_PERIOD,
            "creationDate" to CREATION_TIMESTAMP_AS_DATE,
            "closedInDays" to CLOSED_IN_DAYS,
        )
    if (setOptionalProperties) {
        properties = properties +
            mapOf(
                "dataTypeDescription" to DATA_TYPE_DESCRIPTION,
            )
    }
    return properties
}

fun validateTextContentOfBasicRequestResponseProperties(email: Email) {
    assertTrue(email.content.textContent.contains("$CLOSED_IN_DAYS days"))
    assertTrue(email.content.textContent.contains("Company: $COMPANY_NAME \n"))
    assertTrue(email.content.textContent.contains("Reporting period: $REPORTING_PERIOD \n\n"))
    assertTrue(email.content.textContent.contains("Framework: $DATA_TYPE \n"))
    assertTrue(
        email.content.textContent.contains("Request created: $CREATION_TIMESTAMP_AS_DATE \n\n"),
    )
    assertTrue(
        email.content.textContent.contains("$PROXY_PRIMARY_URL/requests/$DATA_REQUEST_ID"),
    )
}

fun validateHtmlContentOfBasicRequestResponseProperties(email: Email) {
    assertTrue(email.content.htmlContent.contains("DATALAND"))
    assertTrue(email.content.htmlContent.contains("Copyright"))

    assertTrue(email.content.htmlContent.contains("$CLOSED_IN_DAYS days"))
    assertTrue(email.content.htmlContent.contains(COMPANY_NAME))
    assertTrue(email.content.htmlContent.contains(REPORTING_PERIOD))
    assertTrue(email.content.htmlContent.contains(CREATION_TIMESTAMP_AS_DATE))
    assertTrue(
        email.content.htmlContent.contains(
            "href=\"https://$PROXY_PRIMARY_URL/requests/$DATA_REQUEST_ID\"",
        ),
    )
}

fun validateEmailHtmlFormatContainsDefaultProperties(email: Email) {
    assertTrue(email.content.htmlContent.contains(DATA_TYPE))
}

fun validateEmailHtmlFormatContainsOptionalProperties(email: Email) {
    assertTrue(email.content.htmlContent.contains(DATA_TYPE_DESCRIPTION))
}
