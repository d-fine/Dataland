package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class DataExportStoreTest {
    companion object {
        private val FILE_TYPE = ExportFileType.EXCEL
        private const val DATA_TYPE = "lksg"
        private const val USER_NAME = "testUser"
        private const val USER_ID = "test-user-id"
    }

    private lateinit var dataExportStore: DataExportStore

    @BeforeEach
    fun setup() {
        dataExportStore = DataExportStore()
    }

    @Test
    fun `test createAndSaveExportJob counts total jobs created`() {
        AuthenticationMock.withAuthenticationMock(USER_NAME, USER_ID, emptySet<DatalandRealmRole>()) {
            dataExportStore.createAndSaveExportJob(UUID.randomUUID(), FILE_TYPE, DATA_TYPE)
        }

        val totalJobs = dataExportStore.totalStoredJobCount()
        assertTrue(totalJobs == 1, "Expected only 1 job to be created, but found $totalJobs")
    }

    @Test
    fun `test job removed after timeout`() {
        val exportJobId = UUID.randomUUID()

        val exportJob =
            AuthenticationMock.withAuthenticationMock(USER_NAME, USER_ID, emptySet<DatalandRealmRole>()) {
                dataExportStore.createAndSaveExportJob(exportJobId, FILE_TYPE, DATA_TYPE)
            }

        dataExportStore.handleJobTimeout(exportJobId, exportJob)

        val totalJobs = dataExportStore.totalStoredJobCount()
        assertTrue(totalJobs == 0, "Expected jobs to be cleared after timeout, but found $totalJobs")
    }

    @Test
    fun `test job not removed after timeout when not pending`() {
        val exportJobId = UUID.randomUUID()

        val exportJob =
            AuthenticationMock.withAuthenticationMock(USER_NAME, USER_ID, emptySet<DatalandRealmRole>()) {
                dataExportStore.createAndSaveExportJob(exportJobId, FILE_TYPE, DATA_TYPE)
            }

        exportJob.progressState = ExportJobProgressState.Success

        dataExportStore.handleJobTimeout(exportJobId, exportJob)

        val totalJobs = dataExportStore.totalStoredJobCount()
        assertTrue(totalJobs == 1, "Expected job to remain after timeout when not pending, but found $totalJobs")
    }
}
