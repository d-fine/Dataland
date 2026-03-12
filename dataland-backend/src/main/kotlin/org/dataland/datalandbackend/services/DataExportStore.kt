package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.exceptions.DownloadDataNotFoundApiException
import org.dataland.datalandbackend.exceptions.JOB_NOT_FOUND_SUMMARY
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportJob
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.firstOrNull

/**
 * Storage of export jobs for async handling
 */
@Service
class DataExportStore {
    companion object {
        private const val MAX_AGE_OF_EXPORT_JOB_IN_MIN = 10L
        private const val FRONTEND_TIMEOUT_OF_EXPORT_JOB_IN_MIN = 2L
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
                    if (newExportJob.progressState == ExportJobProgressState.Pending) {
                        logger.error(
                            "error: export job {} exceeded {} minutes!",
                            exportJobId, FRONTEND_TIMEOUT_OF_EXPORT_JOB_IN_MIN,
                        )
                    }
                    jobTimeoutTimers.remove(exportJobId)
                }
            },
            Duration.ofMinutes(FRONTEND_TIMEOUT_OF_EXPORT_JOB_IN_MIN).toMillis(),
        )
        return newExportJob
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

    @Suppress("UnusedPrivateMember")
    @Scheduled(cron = "0 */10 * * * *")
    private fun regularExportJobCleanup() {
        val cutoff = Instant.now().minus(Duration.ofMinutes(MAX_AGE_OF_EXPORT_JOB_IN_MIN)).toEpochMilli()

        exportJobStorage.values.forEach { jobs ->
            jobs.removeAll { job ->
                job.creationTime < cutoff
            }
        }
        exportJobStorage.entries.removeIf { (_, jobs) ->
            jobs.isEmpty()
        }
    }
}
