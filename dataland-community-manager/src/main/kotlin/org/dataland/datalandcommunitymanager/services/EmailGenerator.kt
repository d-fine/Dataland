package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.email.Email
import org.dataland.datalandcommunitymanager.model.email.EmailContact
import org.dataland.datalandcommunitymanager.model.email.EmailContent
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.stereotype.Component

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
@Component
class EmailGenerator {
    private val currentEnvironment = System.getenv("PROXY_PRIMARY_URL") ?: "local environment"
    // TODO from app props?

    private fun isEmailAddressFormatValid(emailAddress: String) {
        val regexForValidEmail = Regex("^[a-zA-Z0-9_.!-]+@[a-zA-Z0-9-]+.[a-z]{2,3}\$")
        if (!regexForValidEmail.matches(emailAddress)) {
            throw InternalServerErrorApiException(
                "The email addresses provided by the environment have a wrong format.",
            )
        }
    }

    private fun getEmailAddressesFromEnv(envContainingSemicolonDelimitedEmailAddresses: String): List<EmailContact> {
        val listOfEmailContacts: MutableList<EmailContact> = mutableListOf()
        val envWithSemicolonSeperatedEmailAddresses = System.getenv(envContainingSemicolonDelimitedEmailAddresses)
        if (envWithSemicolonSeperatedEmailAddresses == null) {
            listOfEmailContacts.add(EmailContact("dev.null@dataland.com"))
            // TODO later => fallback in app prop! For cc also null ok?
        } else {
            listOfEmailContacts.addAll(
                envWithSemicolonSeperatedEmailAddresses.split(";").map {
                        emailAddress ->
                    isEmailAddressFormatValid(emailAddress)
                    EmailContact(emailAddress)
                },
            ) }
        return listOfEmailContacts
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
            "Environment: $currentEnvironment " +
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
                    <span class="bold">Environment:</span> $currentEnvironment
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
    } // TODO we could also provide info on how Dataland parsed the identifiers (which types)

    /**
     * Builds a log message for the case that a bulk data request notification mail shall be sent.
     * @returns the log message
     */
    fun buildLogMessageForBulkDataRequestNotificationMail(
        receiversString: String,
        ccReceiversString: String?,
        bulkDataRequestId: String,
    ): String {
        var logMessage =
            "Sending email after ${CauseOfMail.BulkDataRequest} with bulkDataRequestId $bulkDataRequestId has been " +
                "processed -> receivers are $receiversString"
        if (ccReceiversString != null) {
            logMessage += ", and cc receivers are $ccReceiversString"
        }
        return logMessage
    }

    /**
     * Converts a list of EmailContact objects to a joined string with all email addresses seperated by commas.
     * @returns the joined string
     */
    fun convertListOfEmailContactsToJoinedString(listOfEmailContacts: List<EmailContact>): String {
        return listOfEmailContacts.joinToString(", ") {
                emailContact ->
            emailContact.emailAddress
        }
    }

    /**
     * Function that generates the email to be sent
     */
    fun generateBulkDataRequestEmail(
        bulkDataRequest: BulkDataRequest,
        rejectedCompanyIdentifiers: List<String>,
        acceptedCompanyIdentifiers: List<String>,
    ): Email {
        val content = EmailContent(
            "Dataland Bulk Data Request",
            buildBulkDataRequestEmailText(bulkDataRequest, rejectedCompanyIdentifiers, acceptedCompanyIdentifiers),
            buildBulkDataRequestEmailHtml(bulkDataRequest, rejectedCompanyIdentifiers, acceptedCompanyIdentifiers),
        )
        // TODO rename the envs later and to this stuff somewhere else
        val sender = EmailContact("info@dataland.com", "Dataland") // TODO app props?
        val receivers = getEmailAddressesFromEnv("NOTIFICATION_RECEIVERS_BULK_DATA_REQUEST") // TODO app props?
        val cc = getEmailAddressesFromEnv("NOTIFICATION_RECEIVERS_CC_BULK_DATA_REQUEST") // TODO app props?
        // TODO later you could add info about matched Dataland-company-IDs!
        return Email(sender, receivers, cc, content)
    }
}
