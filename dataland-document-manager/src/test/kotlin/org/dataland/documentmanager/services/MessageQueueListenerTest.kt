package org.dataland.documentmanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.documentmanager.DatalandDocumentManager
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.util.Optional

@SpringBootTest(classes = [DatalandDocumentManager::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class MessageQueueListenerTest(
    @Autowired val inMemoryDocumentStore: InMemoryDocumentStore,
    @Autowired private var objectMapper: ObjectMapper,
) {
    lateinit var mockDocumentMetaInfoRepository: DocumentMetaInfoRepository
    lateinit var messageQueueListener: MessageQueueListener

    @BeforeEach
    fun setup() {
        mockDocumentMetaInfoRepository = mock(DocumentMetaInfoRepository::class.java)
        messageQueueListener =
            MessageQueueListener(
                objectMapper = objectMapper,
                documentMetaInfoRepository = mockDocumentMetaInfoRepository,
                inMemoryDocumentStore = inMemoryDocumentStore,
            )
    }

    @Test
    fun `check that an exception is thrown in updating of meta data when documentId is empty`() {
        val messageWithEmptyDocumentID =
            objectMapper.writeValueAsString(
                QaCompletedMessage(
                    identifier = "",
                    validationResult = QaStatus.Accepted,
                    reviewerId = "",
                    message = null,
                ),
            )
        val thrown =
            assertThrows<MessageQueueRejectException> {
                messageQueueListener.updateDocumentMetaData(messageWithEmptyDocumentID, "", MessageType.QA_COMPLETED)
            }
        assertEquals("Message was rejected: Provided document ID is empty", thrown.message)
    }

    @Test
    fun `check that updating meta data after QA works for an existing document`() {
        val documentId = "abc"
        val message =
            objectMapper.writeValueAsString(
                QaCompletedMessage(
                    identifier = documentId,
                    validationResult = QaStatus.Accepted,
                    reviewerId = "",
                    message = null,
                ),
            )

        `when`(mockDocumentMetaInfoRepository.findById(anyString()))
            .thenReturn(
                Optional.of(
                    DocumentMetaInfoEntity(
                        documentType = DocumentType.Pdf,
                        documentId = documentId,
                        uploaderId = "",
                        uploadTime = 0,
                        qaStatus = QaStatus.Pending,
                    ),
                ),
            )

        assertDoesNotThrow { messageQueueListener.updateDocumentMetaData(message, "", MessageType.QA_COMPLETED) }
    }

    @Test
    fun `check that an exception is thrown in removing of stored document if documentId is empty`() {
        val thrown =
            assertThrows<AmqpRejectAndDontRequeueException> {
                messageQueueListener.removeStoredDocumentFromTemporaryStore(
                    "", "",
                    MessageType.DOCUMENT_STORED,
                )
            }
        assertEquals("Message was rejected: Provided document ID is empty", thrown.message)
    }
}
