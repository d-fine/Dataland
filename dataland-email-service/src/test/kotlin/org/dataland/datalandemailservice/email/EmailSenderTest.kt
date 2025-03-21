package org.dataland.datalandemailservice.email

import ch.qos.logback.classic.Level
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetResponse
import org.dataland.datalandemailservice.utils.InMemoryLogAppender
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

class EmailSenderTest {
    private var mockMailjetResponse = mock<MailjetResponse>()
    private var mockMailjetClient = mock<MailjetClient>()

    private var dummySender = EmailContact.create("sender@senderco.com")
    private var dummyReceiver1 = EmailContact.create("receiver1@receiverco1.com")
    private var dummyReceiver2 = EmailContact.create("receiver2@receiverco2.com")
    private var dummyCcReceiver = EmailContact.create("ccreceiver@ccreceiverco.com")
    private var dummySubject = "dummy-subject"

    private var dummyEmail =
        Email(
            sender = dummySender,
            receivers = listOf(dummyReceiver1, dummyReceiver2),
            cc = listOf(dummyCcReceiver),
            bcc = listOf<EmailContact>(),
            content = EmailContent(dummySubject, "", ""),
        )

    private var emptySendEmailsResponseAsJsonString = "{\"messages\":[]}"

    private lateinit var appender: InMemoryLogAppender
    private lateinit var emailSender: EmailSender

    @BeforeEach
    fun setUp() {
        reset(
            mockMailjetResponse,
            mockMailjetClient,
        )

        doReturn(emptySendEmailsResponseAsJsonString).whenever(mockMailjetResponse).rawResponseContent
        doReturn(mockMailjetResponse).whenever(mockMailjetClient).post(anyOrNull())

        appender = InMemoryLogAppender().getAppender()
    }

    @Test
    fun `check that an email is sent when dry run mode is off`() {
        emailSender =
            EmailSender(
                mailjetClient = mockMailjetClient,
                dryRunIsActive = false,
            )
        emailSender.sendEmail(dummyEmail)

        assertTrue(
            appender.contains(
                "Sending email with subject \"${dummySubject}\"\n" +
                    "(sender: ${dummySender.emailAddress})\n" +
                    "(receivers: ${dummyReceiver1.emailAddress}, ${dummyReceiver2.emailAddress})\n" +
                    "(cc receivers: ${dummyCcReceiver.emailAddress})",
                Level.INFO,
            ),
        )
    }

    @Test
    fun `check that no email is sent when dry run mode is on`() {
        emailSender =
            EmailSender(
                mailjetClient = mockMailjetClient,
                dryRunIsActive = true,
            )
        emailSender.sendEmail(dummyEmail)

        assertTrue(
            appender.contains(
                "Withholding email with subject \"${dummySubject}\"\n" +
                    "(sender: ${dummySender.emailAddress})\n" +
                    "(receivers: ${dummyReceiver1.emailAddress}, ${dummyReceiver2.emailAddress})\n" +
                    "(cc receivers: ${dummyCcReceiver.emailAddress})\n" +
                    "due to email service dry run!",
                Level.INFO,
            ),
        )
    }
}
