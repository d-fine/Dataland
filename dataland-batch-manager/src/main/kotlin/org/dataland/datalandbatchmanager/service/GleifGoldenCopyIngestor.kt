package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbatchmanager.model.GleifCompanyInformation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.net.ConnectException
import java.time.Instant
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.stream.StreamSupport
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Class to execute scheduled tasks, like the import of the GLEIF golden copy files
 * @param gleifApiAccessor downloads the golden copy files from GLEIF
 * @param gleifParser reads in the csv file from GLEIF and creates GleifCompanyInformation objects
 */
@Suppress("LongParameterList")
@Component
class GleifGoldenCopyIngestor(
    @Autowired private val gleifApiAccessor: GleifApiAccessor,
    @Autowired private val gleifParser: GleifCsvParser,
    @Autowired private val companyUploader: CompanyUploader,
    @Autowired private val actuatorApi: ActuatorApi,
    @Autowired private val isinDeltaBuilder: IsinDeltaBuilder,
    @Value("\${dataland.dataland-batch-managet.get-all-gleif-companies.force:false}")
    private val allCompaniesForceIngest: Boolean,
    @Value("\${dataland.dataland-batch-managet.get-all-gleif-companies.flag-file:#{null}}")
    private val allCompaniesIngestFlagFilePath: String?,
    @Value("\${dataland.dataland-batch-manager.isin-mapping-file}")
    private val isinMappingFile: File,
) {
    companion object {
        const val MS_PER_S = 1000L
        const val MAX_WAITING_TIME_IN_MS = 10L * 60L * MS_PER_S
        const val WAIT_TIME_IN_MS: Long = 5000
        const val UPLOAD_THREAT_POOL_SIZE = 32
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Downloads the entire GLEIF golden copy file and uploads all included companies to the Dataland Backend.
     * Does so only if the property "dataland.dataland-batch-managet.get-all-gleif-companies" is set.
     */
    @EventListener(ApplicationReadyEvent::class)
    fun processFullGoldenCopyFileIfEnabled() {
        val flagFile = allCompaniesIngestFlagFilePath?.let { File(it) }
        if (allCompaniesForceIngest || flagFile?.exists() == true) {
            if (flagFile?.exists() == true) {
                logger.info("Found collect all companies flag. Deleting it.")
                if (!flagFile.delete()) {
                    logger.error(
                        "Unable to delete flag file $flagFile. Manually remove it or import will " +
                            "be triggered after service restart again.",
                    )
                }
            }

            if (isinMappingFile.exists() && (!isinMappingFile.delete())) {
                throw FileSystemException(
                    file = isinMappingFile,
                    reason = "Unable to delete ISIN mapping file $isinMappingFile",
                )
            }

            logger.info("Retrieving all company data available via GLEIF.")
            val tempFile = File.createTempFile("gleif_golden_copy", ".zip")
            processGleifDeltaFile(tempFile, gleifApiAccessor::getFullGoldenCopy)
            prepareIsinMappingFile()
        } else {
            logger.info("Flag file not present & no force update variable set => Not performing any download")
        }
    }

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 3 * * SUN")
    private fun processUpdates() {
        prepareGleifDeltaFile()
        prepareIsinMappingFile()
    }

    /**
     * Starting point for GLEIF delta file handling
     */
    fun prepareGleifDeltaFile() {
        logger.info("Starting Gleif company update cycle for latest delta file.")
        val tempFile = File.createTempFile("gleif_update_delta", ".zip")
        processGleifDeltaFile(tempFile, gleifApiAccessor::getLastMonthGoldenCopyDelta)
    }

    @Synchronized
    private fun processGleifDeltaFile(zipFile: File, downloadFile: (file: File) -> Unit) {
        waitForBackend()
        val start = System.nanoTime()
        try {
            downloadFile(zipFile)
            uploadCompanies(zipFile)
        } finally {
            if (!zipFile.delete()) {
                logger.error("Unable to delete temporary file $zipFile")
            }
        }
        logger.info("Finished processing of file $zipFile in ${getExecutionTime(start)}.")
    }

    /**
     * Starting point for ISIN mapping file handling
     */
    fun prepareIsinMappingFile() {
        logger.info("Starting LEI-ISIN mapping update cycle for latest file.")
        val tempFile = File.createTempFile("gleif_mapping_update", ".csv")
        processIsinMappingFile(tempFile, gleifApiAccessor::getFullIsinMappingFile)
    }

    @Synchronized
    private fun processIsinMappingFile(newMappingFile: File, downloadFile: (file: File) -> Unit) {
        waitForBackend()
        val start = System.nanoTime()
        downloadFile(newMappingFile)
        val deltaMapping: Map<String, Set<String>> =
            if (!isinMappingFile.exists() || isinMappingFile.length() == 0L) {
                isinDeltaBuilder.createDeltaOfMappingFile(newMappingFile, null)
            } else {
                isinDeltaBuilder.createDeltaOfMappingFile(newMappingFile, isinMappingFile)
            }
        replaceOldMappingFile(newMappingFile)
        companyUploader.updateIsins(deltaMapping)

        logger.info("Finished processing of file $newMappingFile in ${getExecutionTime(start)}.")
    }

    private fun waitForBackend() {
        val timeoutTime = Instant.now().toEpochMilli() + MAX_WAITING_TIME_IN_MS
        while (Instant.now().toEpochMilli() <= timeoutTime) {
            try {
                actuatorApi.health()
                break
            } catch (exception: ConnectException) {
                logger.info(
                    "Waiting for ${WAIT_TIME_IN_MS / MS_PER_S}s backend to be available." +
                        " Exception was: ${exception.message}.",
                )
                Thread.sleep(WAIT_TIME_IN_MS)
            }
        }
    }

    private fun uploadCompanies(zipFile: File) {
        val gleifDataStream = gleifParser.getCsvStreamFromZip(zipFile)
        val gleifIterator = gleifParser.readGleifDataFromBufferedReader(gleifDataStream)
        val gleifIterable = Iterable<GleifCompanyInformation> { gleifIterator }

        val uploadThreadPool = ForkJoinPool(UPLOAD_THREAT_POOL_SIZE)
        try {
            uploadThreadPool.submit {
                StreamSupport.stream(gleifIterable.spliterator(), true)
                    .forEach { companyUploader.uploadOrPatchSingleCompany(it) }
            }.get()
        } finally {
            uploadThreadPool.shutdown()
        }
    }

    private fun getExecutionTime(startTime: Long): String {
        return (System.nanoTime() - startTime)
            .toDuration(DurationUnit.NANOSECONDS)
            .toComponents { hours, minutes, seconds, _ ->
                String.format(
                    Locale.getDefault(), "%02dh %02dm %02ds", hours, minutes, seconds,
                )
            }
    }

    /**
     * Replaces the locally saved old mapping file with the recently downloaded one after creating delta is done
     * @param newMappingFile latest version of the LEI-ISIN mapping file
     */
    fun replaceOldMappingFile(newMappingFile: File) {
        try {
            newMappingFile.copyTo(isinMappingFile, true)
            if (!newMappingFile.delete()) {
                logger.error("failed to delete file $newMappingFile")
            }
        } catch (e: FileSystemException) {
            logger.error("Error while replacing the old mapping file: ${e.message}")
        }
    }
}
