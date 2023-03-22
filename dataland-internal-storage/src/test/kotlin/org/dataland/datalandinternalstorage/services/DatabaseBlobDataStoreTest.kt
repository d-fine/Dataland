package org.dataland.datalandinternalstorage.services

import org.dataland.datalandinternalstorage.DatalandInternalStorage
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [DatalandInternalStorage::class])
@ActiveProfiles("test")
class DatabaseBlobDataStoreTest(
    @Autowired private val databaseBlobDataStore: DatabaseBlobDataStore,
) {
    @Test
    fun `check that a blob can be stored and then retrieved by its blob id`() {
        val dataToStore = "Dataland is awesome".toByteArray(Charsets.UTF_8)
        val blobId = "b5ebbb0e075e95be1d8e32002a7766deaa1f9c6c075b2d3c9f9822183a4eea27"

        val storedBlob = databaseBlobDataStore.storeBlobToDatabase(blobId, dataToStore)
        assertEquals(blobId, storedBlob.blobId)

        val retrievedData = databaseBlobDataStore.selectBlobById(blobId, "test-correlation-id")
        assertArrayEquals(dataToStore, retrievedData)
    }
}
