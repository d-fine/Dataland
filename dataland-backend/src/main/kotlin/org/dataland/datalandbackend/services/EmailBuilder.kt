package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.email.Email
import org.dataland.datalandbackend.model.email.EmailContact
import org.dataland.datalandbackend.model.email.EmailContent
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
    DataOwnershipRequest("bulk data request"),
    ;

    override fun toString(): String {
        return description
    }
}

/**
 * A class that manages generating emails
 */
@Component("EmailBuilder")
class EmailBuilder(
    @Value("\${dataland.proxy.primary.url}") private val propProxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") private val propNotificationSenderAddress: String,
    @Value("\${dataland.notification.sender.name}") private val propNotificationSenderName: String,
    @Value("\${dataland.notification.receivers.data.ownership.request}")
    private val propNotificationReceiversDataOwnershipRequest: String,
    @Value("\${dataland.notification.receivers.cc.data.ownership.request}")
    private val propNotificationReceiversCcDataOwnershipRequest: String,
) {
    private val datalandNotificatorEmailContact =
        EmailContact(assertEmailAddressFormatAndReturnIt(propNotificationSenderAddress), propNotificationSenderName)

    private val notificationReceiversDataOwnershipRequest =
        getEmailContactsFromProp(propNotificationReceiversDataOwnershipRequest)
    private val notificationReceiversCcDataOwnershipRequest =
        getEmailContactsFromProp(propNotificationReceiversCcDataOwnershipRequest)

    private val dataOwnershipRequestNotificationMailStyle =
        """
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
    """

    private fun assertEmailAddressFormatAndReturnIt(emailAddress: String): String {
        val regexForValidEmail = Regex("^[a-zA-Z0-9_.!-]+@[a-zA-Z0-9-]+.[a-z]{2,3}\$")
        if (!regexForValidEmail.matches(emailAddress)) {
            val logger = LoggerFactory.getLogger(javaClass)
            logger.error(
                "The email addresses provided by the Spring properties have a wrong format. " +
                    "The following email address was parsed from that prop and caused this error: $emailAddress" +
                    "The Spring application is shutting down because sending notifications might not work as expected.",
            )
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

    private fun buildUserInfo(
        userAuthentication: DatalandAuthentication,
    ): String {
        return if(userAuthentication is DatalandJwtAuthentication) {
            "User ${userAuthentication.username} (Keycloak id: ${userAuthentication.userId})"
        } else {
            "User (Keycloak id: ${userAuthentication.userId})"

        }
    }

    private fun buildDataOwnershipRequestEmailText(
        companyId: String,
        userAuthentication: DatalandAuthentication,
    ): String {
        return "A data ownership request has been submitted: " +
            "Environment: $propProxyPrimaryUrl " +
            "User: ${buildUserInfo(userAuthentication)} " +
            "Company (Dataland ID): $companyId"
    }

    private fun buildDataOwnershipRequestEmailHtml(
        companyId: String,
        userAuthentication: DatalandAuthentication,
    ): String {
        return """
        <html>
        <head>
                $dataOwnershipRequestNotificationMailStyle
        </head>
        <body>
            <div class="container">
                <div class="header">Data Ownership Request</div>
                <div class="section"> <span class="bold">Environment: </span> $propProxyPrimaryUrl </div>
                <div class="section"> <span class="bold">User: </span> ${buildUserInfo(userAuthentication)} </div>
                <div class="section"> <span class="bold">Company (Dataland ID): </span> $companyId </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Function that generates an email for a data ownership request
     */
    fun buildDataOwnershipRequest(
        companyId: String,
        userAuthentication: DatalandAuthentication,
    ): Email {
        val content = EmailContent(
            "Dataland Data Ownership Request",
            buildDataOwnershipRequestEmailText(companyId, userAuthentication),
            buildDataOwnershipRequestEmailHtml(companyId, userAuthentication),
        )
        return Email(
            datalandNotificatorEmailContact,
            notificationReceiversDataOwnershipRequest,
            notificationReceiversCcDataOwnershipRequest,
            content,
        )
    }
}
