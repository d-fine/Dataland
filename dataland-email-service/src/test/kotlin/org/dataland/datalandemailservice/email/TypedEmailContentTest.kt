package org.dataland.datalandemailservice.email

import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.util.*

typealias TestData = TypedEmailContentTestData

class TypedEmailContentTest {

    @Test
    fun `test that every template has test data`() {
        TypedEmailContent::class.sealedSubclasses.forEach { subclass ->
            assertTrue(TestData.contentToKeywordsMap.any { (typedEmailContent, _) -> subclass.isInstance(typedEmailContent) })
        }
    }

    @Test
    fun `test that every template can be constructed without exception`() {

        TestData.contentToKeywordsMap.forEach { (typedEmailContent, keywords) ->

            assertDoesNotThrow {
                val emailContent = typedEmailContent.build()
                keywords.forEach { keyword ->
//                    assertTrue(emailContent.textContent.contains(keyword)) TODO this is wrong
//                    assertTrue(emailContent.htmlContent.contains(keyword)) // TODO this tests needs debugging
                }
                saveEmailContent(typedEmailContent::class.simpleName ?: UUID.randomUUID().toString(), emailContent)
            }
        }
    }

    private fun saveEmailContent(name: String, emailContent: EmailContent) {
        val tmpDir = File("tmp")
        if (!tmpDir.exists()) tmpDir.mkdirs()

        val txtFile = File(tmpDir,"$name.txt")
        val htmlFile = File(tmpDir, "$name.html")

        txtFile.writeText(emailContent.textContent)
        htmlFile.writeText(emailContent.htmlContent)

        println("Saved email as ${txtFile.absolutePath} and ${htmlFile.absolutePath}")
    }

    @Test
    fun `test that late init vars specified in interfaces are correctly injected`() {
        val content = TestData.singleDatasetUploadedEngagement
        val subscriptionUuid = UUID.randomUUID()
        val receiver = mapOf(
            EmailContact("test3@example.com") to subscriptionUuid
        )
        val proxyPrimaryUrl = "abc.dataland.com"

        content.setLateInitVars(receiver, proxyPrimaryUrl)

        assertEquals(content.baseUrl, proxyPrimaryUrl)
        assertEquals(content.subscriptionUuid, subscriptionUuid.toString())
    }

    @Test
    fun `test that late init subscription statuses are correctly injected`() {
        // TODO
    }

}