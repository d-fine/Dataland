package org.dataland.datalandbatchmanager.service

import org.apache.commons.io.FileUtils
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbatchmanager.model.GleifCompanyCombinedInformation
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
import kotlin.time.Duration
import kotlin.time.measureTime

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
    @Autowired private val relationShipExtractor: RelationshipExtractor,
    @Value("\${dataland.dataland-batch-managet.get-all-gleif-companies.force:false}")
    private val allCompaniesForceIngest: Boolean,
    @Value("\${dataland.dataland-batch-managet.get-all-gleif-companies.flag-file:#{null}}")
    private val allCompaniesIngestFlagFilePath: String?,
    @Value("\${dataland.dataland-batch-manager.isin-mapping-file}")
    private val savedIsinMappingFile: File,
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

            if (savedIsinMappingFile.exists() && (!savedIsinMappingFile.delete())) {
                throw FileSystemException(
                    file = savedIsinMappingFile,
                    reason = "Unable to delete ISIN mapping file $savedIsinMappingFile",
                )
            }

            waitForBackend()
            logger.info("Retrieving all company data available via GLEIF.")

            // Process relationship file before LEI file to have info available upon company upload
            processRelationshipFile(updateAllCompanies = false)
            val tempFile = File.createTempFile("gleif_golden_copy", ".zip")
            processGleifFile(tempFile, gleifApiAccessor::getFullGoldenCopy)
            processIsinMappingFile()
        } else {
            logger.info("Flag file not present & no force update variable set => Not performing any download")
        }
    }

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 3 * * SUN")
    private fun processUpdates() {
        waitForBackend()
        prepareGleifDeltaFile()
        processIsinMappingFile()
        processRelationshipFile(updateAllCompanies = true)
    }

    /**
     * Starting point for GLEIF delta file handling
     */
    fun prepareGleifDeltaFile() {
        logger.info("Starting Gleif company update cycle for latest delta file.")
        val tempFile = File.createTempFile("gleif_update_delta", ".zip")
        processGleifFile(tempFile, gleifApiAccessor::getLastMonthGoldenCopyDelta)
    }

    @Synchronized
    private fun processGleifFile(zipFile: File, downloadFile: (file: File) -> Unit) {
        val duration = measureTime {
            try {
                downloadFile(zipFile)
                uploadCompanies(zipFile)
            } finally {
                if (!zipFile.delete()) {
                    logger.error("Unable to delete temporary file $zipFile")
                }
            }
        }
        logger.info("Finished processing of GLEIF file $zipFile in ${formatExecutionTime(duration)}.")
    }

    @Synchronized
    private fun processRelationshipFile(updateAllCompanies: Boolean = false) {
        logger.info("Starting parent mapping update cycle for latest file.")
        val newRelationshipFile = File.createTempFile("gleif_relationship_golden_copy", ".zip")
        val duration = measureTime {
            gleifApiAccessor.getFullGoldenCopyRR(newRelationshipFile)
            val gleifDataStream = gleifParser.getCsvStreamFromZip(newRelationshipFile)
            val gleifCsvParser = gleifParser.readGleifRelationshipDataFromBufferedReader(gleifDataStream)
            relationShipExtractor.prepareFinalParentMapping(gleifCsvParser)
            if (updateAllCompanies) companyUploader.updateRelationships(relationShipExtractor.finalParentMapping)
        }
        logger.info("Finished processing of GLEIF RR file $newRelationshipFile in ${formatExecutionTime(duration)}.")
    }

    /**
     * Starting point for ISIN mapping file handling
     */
    @Synchronized
    private fun processIsinMappingFile() {
        logger.info("Starting LEI-ISIN mapping update cycle for latest file.")
        val newMappingFile = File.createTempFile("gleif_mapping_update", ".csv")
        val duration = measureTime {
            gleifApiAccessor.getFullIsinMappingFile(newMappingFile)
            val deltaMapping: Map<String, Set<String>> =
                if (!savedIsinMappingFile.exists() || savedIsinMappingFile.length() == 0L) {
                    isinDeltaBuilder.createDeltaOfMappingFile(newMappingFile, null)
                } else {
                    isinDeltaBuilder.createDeltaOfMappingFile(newMappingFile, savedIsinMappingFile)
                }
            val newPersistentFile = File("${savedIsinMappingFile.parent}/newIsinMapping.csv")
            FileUtils.copyFile(newMappingFile, newPersistentFile)
            if (!newMappingFile.delete()) {
                logger.error("failed to delete temporary mapping file $newMappingFile")
            }
            companyUploader.updateIsins(deltaMapping)
            replaceOldMappingFile(File("${savedIsinMappingFile.parent}/newIsinMapping.csv"))
        }
        logger.info("Finished processing of file $newMappingFile in ${formatExecutionTime(duration)}.")
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
                    .forEach {
                        companyUploader.uploadOrPatchSingleCompany(
                            GleifCompanyCombinedInformation(
                                it,
                                relationShipExtractor.finalParentMapping.getOrDefault(it.lei, null),
                            ),
                        )
                    }
            }.get()
        } finally {
            uploadThreadPool.shutdown()
        }
    }

    private fun formatExecutionTime(duration: Duration): String {
        return duration
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
            newMappingFile.copyTo(savedIsinMappingFile, true)
            if (!newMappingFile.delete()) {
                logger.error("failed to delete file $newMappingFile")
            }
        } catch (e: FileSystemException) {
            logger.error("Error while replacing the old mapping file: ${e.message}")
        }
    }
}
