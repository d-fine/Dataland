package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.email.Email
import org.dataland.datalandcommunitymanager.model.email.EmailContact
import org.dataland.datalandcommunitymanager.model.email.EmailContent
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

/**
 * This enum contains possible causes to generate an email. This cause can then be included in the log message.
 */
enum class CauseOfMail(val description: String) {
    BulkDataRequest("bulk data request"),
    ;

    override fun toString(): String {
        return description
    }
}

/**
 * A class that manages generating emails
 */
@Component ("EmailBuilder")
class EmailBuilder(
    @Value("\${dataland.proxy.primary.url}") private val propProxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") private val propNotificationSenderAddress: String,
    @Value("\${dataland.notification.sender.name}") private val propNotificationSenderName: String,
    @Value("\${dataland.notification.receivers.bulk.data.request}")
    private val propNotificationReceiversBulkDataRequest: String,
    @Value("\${dataland.notification.receivers.cc.bulk.data.request}")
    private val propNotificationReceiversCcBulkDataRequest: String
)
{
    private val datalandNotificatorEmailContact =
        EmailContact(assertEmailAddressFormatAndReturnIt(propNotificationSenderAddress), propNotificationSenderName)

    private val notificationReceiversBulkDataRequest =
        getEmailContactsFromProp(propNotificationReceiversBulkDataRequest)
    private val notificationReceiversCcBulkDataRequest =
        getEmailContactsFromProp(propNotificationReceiversCcBulkDataRequest)

    private fun assertEmailAddressFormatAndReturnIt(emailAddress: String): String {
        val regexForValidEmail = Regex("^[a-zA-Z0-9_.!-]+@[a-zA-Z0-9-]+.[a-z]{2,3}\$")
        if (!regexForValidEmail.matches(emailAddress)) {
            val logger = LoggerFactory.getLogger(javaClass)
            logger.error("The email addresses provided by the Spring properties have a wrong format. " +
                    "The following email address was parsed from that prop and caused this error: $emailAddress" +
                    "The Spring application is shutting down because sending notifications might not work as expected.")
            exitProcess(1)
        }
        return emailAddress
    }

    private fun getEmailContactsFromProp(propWithSemicolonSeperatedEmailAddresses: String): List<EmailContact> {
            return propWithSemicolonSeperatedEmailAddresses.split(";").map {
                        emailAddressString ->
                    EmailContact(assertEmailAddressFormatAndReturnIt(emailAddressString))
                }
             }

    private fun buildUserInfo(): String {
        // TODO the "as" in the next line breaks the whole thing if you use api key auth!
        val user = DatalandAuthentication.fromContext() as DatalandJwtAuthentication
        return "User ${user.username} (Keycloak id: ${user.userId})"
    }

    private fun buildBulkDataRequestEmailText(
        bulkDataRequest: BulkDataRequest,
        rejectedCompanyIdentifiers: List<String>,
        acceptedCompanyIdentifiers: List<String>,
    ): String {
        return "A bulk data request has been submitted: " +
            "Environment: $propProxyPrimaryUrl " +
            "User: ${buildUserInfo()} " +
            "Requested company identifiers: ${bulkDataRequest.listOfCompanyIdentifiers.joinToString(", ")}. " +
            "Requested frameworks: ${bulkDataRequest.listOfFrameworkNames.joinToString(", ")}. " +
            "Rejected company identifiers: ${rejectedCompanyIdentifiers.joinToString(", ")}. " +
            "Accepted company identifiers: ${acceptedCompanyIdentifiers.joinToString(", ")}."
    }

    private fun buildBulkDataRequestEmailHtml(
        bulkDataRequest: BulkDataRequest,
        rejectedCompanyIdentifiers: List<String>,
        acceptedCompanyIdentifiers: List<String>,
    ): String {
        return """
        <html>
        <head>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    color: #333;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    border-radius: 10px;
                }
                .header {
                    font-size: 24px;
                    font-weight: bold;
                    margin-bottom: 10px;
                }
                .section {
                    margin-bottom: 10px;
                }
                .bold {
                    font-weight: bold;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">Bulk Data Request</div>
                <div class="section">
                    <span class="bold">Environment:</span> $propProxyPrimaryUrl
                </div>
                <div class="section">
                    <span class="bold">User:</span> ${buildUserInfo()}
                </div>
                <div class="section">
                    <span class="bold">Requested Company Identifiers:</span> ${bulkDataRequest.listOfCompanyIdentifiers.joinToString(", ")}
                </div>
                <div class="section">
                    <span class="bold">Requested Frameworks:</span> ${bulkDataRequest.listOfFrameworkNames.joinToString(", ")}
                </div>
                <div class="section">
                    <span class="bold">Rejected Company Identifiers:</span> ${rejectedCompanyIdentifiers.joinToString(", ")}
                </div>
                <div class="section">
                    <span class="bold">Accepted Company Identifiers:</span> ${acceptedCompanyIdentifiers.joinToString(", ")}
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
    // TODO IF time at the end:  Make email have table (and it can also contain the regex-matched types);  Requested and Rejected rauswerfen




    /**
     * Function that generates the email to be sent
     */
    fun buildBulkDataRequestEmail(
        bulkDataRequest: BulkDataRequest,
        rejectedCompanyIdentifiers: List<String>,
        acceptedCompanyIdentifiers: List<String>,
    ): Email {
        val content = EmailContent(
            "Dataland Bulk Data Request",
            buildBulkDataRequestEmailText(bulkDataRequest, rejectedCompanyIdentifiers, acceptedCompanyIdentifiers),
            buildBulkDataRequestEmailHtml(bulkDataRequest, rejectedCompanyIdentifiers, acceptedCompanyIdentifiers),
        )
        // TODO later you could add info about matched Dataland-company-IDs!
        return Email(
            datalandNotificatorEmailContact,
            notificationReceiversBulkDataRequest,
            notificationReceiversCcBulkDataRequest,
            content)
    }
}
