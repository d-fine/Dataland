package org.dataland.datalandbackend.services

import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class DataExportStoreTest {
    private lateinit var dataExportStore: DataExportStore

    @BeforeEach
    fun setup() {
        dataExportStore = DataExportStore()
    }

    @Test
    fun `test createAndSaveExportJob counts total jobs created`() {
        val fileType = ExportFileType.EXCEL
        val dataType = "lksg"

        AuthenticationMock.withAuthenticationMock("testUser", "test-user-id", emptySet<DatalandRealmRole>()) {
            dataExportStore.createAndSaveExportJob(UUID.randomUUID(), fileType, dataType)
        }

        val field = DataExportStore::class.java.getDeclaredField("exportJobStorage")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val storage = field.get(dataExportStore) as MutableMap<String, MutableList<*>>
        val totalJobs = storage.values.flatten().size
        assertTrue(totalJobs == 1, "Expected only 1 job to be created, but found $totalJobs")
    }

    @Test
    fun `test removing jobs after timeout`() {
        val fileType = ExportFileType.EXCEL
        val dataType = "lksg"
        val exportJobId = UUID.randomUUID()

        val exportJob =
            AuthenticationMock.withAuthenticationMock("testUser", "test-user-id", emptySet<DatalandRealmRole>()) {
                dataExportStore.createAndSaveExportJob(exportJobId, fileType, dataType)
            }

        dataExportStore.handleJobTimeout(exportJobId, exportJob)

        val field = DataExportStore::class.java.getDeclaredField("exportJobStorage")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val storage = field.get(dataExportStore) as MutableMap<String, MutableList<*>>
        val totalJobs = storage.values.flatten().size
        assertTrue(totalJobs == 0, "Expected jobs to be cleared after timeout, but found $totalJobs")
    }
}
