package org.dataland.documentmanager.services

import org.dataland.datalandbackendutils.utils.sha256
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InMemoryDocumentStoreTest {
    private val inMemoryStore = InMemoryDocumentStore()

    @Test
    fun `check that data can be stored and retrieved in the inmemory store`() {
        val dataToStore = "Dataland is awesome".toByteArray(Charsets.UTF_8)
        val expectedHash = "b5ebbb0e075e95be1d8e32002a7766deaa1f9c6c075b2d3c9f9822183a4eea27"

        val documentId = dataToStore.sha256()
        inMemoryStore.storeDataInMemory(documentId, dataToStore)
        assertEquals(expectedHash, documentId)

        val retrievedDataset = inMemoryStore.retrieveDataFromMemoryStore(expectedHash)
        assertArrayEquals(dataToStore, retrievedDataset)

        inMemoryStore.deleteFromInMemoryStore(expectedHash)

        assertEquals(inMemoryStore.retrieveDataFromMemoryStore(expectedHash), null)
    }
}
