package org.dataland.e2etests.tests

import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.documentmanager.openApiClient.model.DocumentQAStatus
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.junit.jupiter.api.Test
import org.dataland.e2etests.BASE_PATH_TO_DOCUMENT_MANAGER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File

class DocumentControllerTest {
    private val documentControllerClient = DocumentControllerApi(BASE_PATH_TO_DOCUMENT_MANAGER)

    private val document: File = File.createTempFile("test", ".pdf")

    @Test
    fun `test that a dummy document can be uploaded and retrieved after successfull QA`() {
        document.writeText("this is content")
        val expectedHash = document.readBytes().sha256()
        val metaInfo = documentControllerClient.postDocument(document)
        assertEquals(expectedHash, metaInfo.documentId)
        assertEquals(DocumentQAStatus.pending, metaInfo.qaStatus)
        assertTrue(documentControllerClient.checkDocument(metaInfo.documentId!!).documentExists)
        val downloadedFile = documentControllerClient.getDocument(metaInfo.documentId!!)
        assertEquals(expectedHash, downloadedFile.readBytes().sha256())
        assertEquals(document.name, downloadedFile.name)
    }
}