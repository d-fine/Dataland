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
        private const val FRONTEND_TIMEOUT_CHECKER_FREQUENCY_IN_SEC = 15L
    }

    private val exportJobStorage = mutableMapOf<String, MutableList<ExportJob>>()
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Instantiates and saves ExportJobInfo in memory.
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
            .getOrPut(DatalandAuthentication.fromContext().userId) { mutableListOf(newExportJob) }
            .add(newExportJob)
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

    @Suppress("UnusedPrivateMember")
    @Scheduled(cron = "*/15 * * * * *")
    private fun frontendExportJobTimeoutAlert() {
        val frontendTimeout = Instant.now().minus(Duration.ofMinutes(FRONTEND_TIMEOUT_OF_EXPORT_JOB_IN_MIN)).toEpochMilli()
        val frontendTimeoutPlusCronInterval =
            Instant
                .now()
                .minus(Duration.ofMinutes(FRONTEND_TIMEOUT_OF_EXPORT_JOB_IN_MIN).plusSeconds(FRONTEND_TIMEOUT_CHECKER_FREQUENCY_IN_SEC))
                .toEpochMilli()

        exportJobStorage.values.forEach { jobs ->
            jobs
                .filter { job ->
                    job.creationTime in (frontendTimeoutPlusCronInterval)..<frontendTimeout &&
                        job.progressState == ExportJobProgressState.Pending
                }.forEach { job ->
                    logger.info(
                        "error: Export job {} exceeded {} minutes!",
                        job.id, FRONTEND_TIMEOUT_OF_EXPORT_JOB_IN_MIN,
                    )
                }
        }
    }
}
