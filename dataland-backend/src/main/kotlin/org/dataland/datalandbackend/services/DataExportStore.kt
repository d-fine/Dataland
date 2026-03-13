package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.exceptions.DownloadDataNotFoundApiException
import org.dataland.datalandbackend.exceptions.JOB_NOT_FOUND_SUMMARY
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportJob
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.firstOrNull
import kotlin.collections.removeAll

/**
 * Storage of export jobs for async handling
 */
@Service
class DataExportStore {
    companion object {
        private const val FRONTEND_TIMEOUT_OF_EXPORT_JOB_IN_MIN = 3L
    }

    private val exportJobStorage = mutableMapOf<String, MutableList<ExportJob>>()
    private val jobTimeoutTimers = mutableMapOf<UUID, Timer>()
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Instantiates and saves ExportJobInfo in memory.
     * Also schedules a one-shot timer that logs a warning if the job is still pending after the frontend timeout.
     */
    internal fun createAndSaveExportJob(
        exportJobId: UUID,
        fileType: ExportFileType,
        dataType: String,
    ): ExportJob {
        val newExportJob =
            ExportJob(
                exportJobId,
                null,
                fileType,
                dataType,
                creationTime = Instant.now().toEpochMilli(),
            )
        exportJobStorage
            .getOrPut(DatalandAuthentication.fromContext().userId) { mutableListOf() }
            .add(newExportJob)
        val timer = Timer(true)
        jobTimeoutTimers[exportJobId] = timer
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    handleJobTimeout(exportJobId, newExportJob)
                }
            },
            Duration.ofMinutes(FRONTEND_TIMEOUT_OF_EXPORT_JOB_IN_MIN).toMillis(),
        )
        return newExportJob
    }

    /**
     * Handles the timeout of an export job by removing it from storage if it is still pending.
     */
    internal fun handleJobTimeout(
        exportJobId: UUID,
        exportJob: ExportJob,
    ) {
        if (exportJob.progressState == ExportJobProgressState.Pending) {
            logger.error(
                "export job {} exceeded {} minutes!",
                exportJobId, FRONTEND_TIMEOUT_OF_EXPORT_JOB_IN_MIN,
            )
            exportJobStorage.values.forEach { jobs ->
                jobs.removeAll { it.id == exportJobId }
            }
            exportJobStorage.entries.removeIf { (_, jobs) ->
                jobs.isEmpty()
            }
        }
        jobTimeoutTimers.remove(exportJobId)
    }

    /**
     * Filters exportJob associated to user by id and returns progressState
     */
    fun getExportJobState(exportJobId: UUID): ExportJobProgressState =
        exportJobStorage[DatalandAuthentication.fromContext().userId]
            ?.firstOrNull { it.id == exportJobId }
            ?.progressState
            ?: throw DownloadDataNotFoundApiException(JOB_NOT_FOUND_SUMMARY)

    /**
     * Get an export job from exportJobStorage by its ID.
     */
    fun getExportJob(exportJobId: UUID): ExportJob =
        exportJobStorage[DatalandAuthentication.fromContext().userId]
            ?.firstOrNull { it.id == exportJobId }
            ?: throw DownloadDataNotFoundApiException(JOB_NOT_FOUND_SUMMARY)

    /**
     * Returns the total number of export jobs across all users. For testing purposes only.
     */
    internal fun totalStoredJobCount(): Int = exportJobStorage.values.sumOf { it.size }

    /**
     * Delete an export job from exportJobStorage by its ID.
     */
    fun deleteExportJob(exportJobId: UUID) {
        jobTimeoutTimers.remove(exportJobId)?.cancel()
        val userId = DatalandAuthentication.fromContext().userId
        exportJobStorage[userId]?.removeAll { it.id == exportJobId }
        if (exportJobStorage[userId]?.isEmpty() ?: false) {
            exportJobStorage.remove(userId)
        }
    }
}
