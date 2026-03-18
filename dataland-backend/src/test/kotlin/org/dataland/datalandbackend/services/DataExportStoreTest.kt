package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.slf4j.Logger
import java.lang.reflect.Field
import java.util.UUID

class DataExportStoreTest {
    companion object {
        private val FILE_TYPE = ExportFileType.EXCEL
        private const val DATA_TYPE = "lksg"
        private const val USER_NAME = "testUser"
        private const val USER_ID = "test-user-id"
    }

    private lateinit var dataExportStore: DataExportStore
    private lateinit var mockLogger: Logger

    @BeforeEach
    fun setup() {
        dataExportStore = DataExportStore()
        mockLogger = mock(Logger::class.java)
        val loggerField: Field = DataExportStore::class.java.getDeclaredField("logger")
        loggerField.isAccessible = true
        loggerField.set(dataExportStore, mockLogger)
        AuthenticationMock.mockSecurityContext(
            USER_NAME,
            USER_ID,
            emptySet<DatalandRealmRole>(),
        )
    }

    @Test
    fun `test createAndSaveExportJob counts total jobs created`() {
        dataExportStore.createAndSaveExportJob(UUID.randomUUID(), FILE_TYPE, DATA_TYPE)

        val totalJobs = dataExportStore.totalStoredJobCount()
        assertTrue(totalJobs == 1, "Expected only 1 job to be created, but found $totalJobs")
    }

    @Test
    fun `test job removed after timeout`() {
        val exportJobId = UUID.randomUUID()

        val exportJob = dataExportStore.createAndSaveExportJob(exportJobId, FILE_TYPE, DATA_TYPE)

        dataExportStore.removeIfJobPendingAfterFrontendTimeout(exportJobId, exportJob)

        val totalJobs = dataExportStore.totalStoredJobCount()
        assertTrue(totalJobs == 0, "Expected jobs to be cleared after timeout, but found $totalJobs")
    }

    @Test
    fun `test job not removed after timeout when not pending`() {
        val exportJobId = UUID.randomUUID()

        val exportJob = dataExportStore.createAndSaveExportJob(exportJobId, FILE_TYPE, DATA_TYPE)

        exportJob.progressState = ExportJobProgressState.Success

        dataExportStore.removeIfJobPendingAfterFrontendTimeout(exportJobId, exportJob)

        val totalJobs = dataExportStore.totalStoredJobCount()
        assertTrue(totalJobs == 1, "Expected job to remain after timeout when not pending, but found $totalJobs")
    }

    @Test
    fun `test warning is logged when job is still pending after frontend timeout`() {
        val exportJobId = UUID.randomUUID()

        val exportJob = dataExportStore.createAndSaveExportJob(exportJobId, FILE_TYPE, DATA_TYPE)

        dataExportStore.warnIfJobPendingAfterFrontendTimeout(exportJobId, exportJob)

        verify(mockLogger).error(
            DataExportStore.EXPORT_JOB_TIMEOUT_LOG_MESSAGE,
            exportJobId,
            FILE_TYPE,
            USER_ID,
            DataExportStore.EXPORT_JOB_TIMEOUT_WARNING_AFTER_MINS,
        )
    }

    @Test
    fun `test no warning is logged when job is not pending after frontend timeout`() {
        val exportJobId = UUID.randomUUID()

        val exportJob = dataExportStore.createAndSaveExportJob(exportJobId, FILE_TYPE, DATA_TYPE)

        exportJob.progressState = ExportJobProgressState.Success

        dataExportStore.warnIfJobPendingAfterFrontendTimeout(exportJobId, exportJob)

        verifyNoInteractions(mockLogger)
    }
}
