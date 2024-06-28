package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
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

/**
 * Class to execute scheduled tasks, like the import of the GLEIF golden copy files
 * @param gleifApiAccessor downloads the golden copy files from GLEIF
 * @param gleifParser reads in the csv file from GLEIF and creates GleifCompanyInformation objects
 * @param actuatorApi the actuatorApi of the backend
 */
@Suppress("LongParameterList")
@Component
class ProcessDataUpdates(
    @Autowired private val gleifApiAccessor: GleifApiAccessor,
    @Autowired private val gleifGoldenCopyIngestor: GleifGoldenCopyIngestor,
    @Autowired private val actuatorApi: ActuatorApi,
    @Value("\${dataland.dataland-batch-managet.get-all-gleif-companies.force:false}")
    private val allCompaniesForceIngest: Boolean,
    @Value("\${dataland.dataland-batch-managet.get-all-gleif-companies.flag-file:#{null}}")
    private val allCompaniesIngestFlagFilePath: String?,
    @Value("\${dataland.dataland-batch-manager.isin-mapping-file}")
    private val savedIsinMappingFile: File,
) {

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

            gleifGoldenCopyIngestor.processRelationshipFile(updateAllCompanies = false)
            val tempFile = File.createTempFile("gleif_golden_copy", ".zip")
            gleifGoldenCopyIngestor.processGleifFile(tempFile, gleifApiAccessor::getFullGoldenCopy)
            gleifGoldenCopyIngestor.processIsinMappingFile()
        } else {
            logger.info("Flag file not present & no force update variable set => Not performing any download")
        }
    }

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 3 * * SUN")
    private fun processUpdates() {
        waitForBackend()
        gleifGoldenCopyIngestor.prepareGleifDeltaFile()
        gleifGoldenCopyIngestor.processIsinMappingFile()
        gleifGoldenCopyIngestor.processRelationshipFile(updateAllCompanies = true)
    }

    /**
     * This method waits for the backend to be ready
     */
    fun waitForBackend() {
        val timeoutTime = Instant.now().toEpochMilli() + GleifGoldenCopyIngestor.MAX_WAITING_TIME_IN_MS
        while (Instant.now().toEpochMilli() <= timeoutTime) {
            try {
                actuatorApi.health()
                break
            } catch (exception: ConnectException) {
                logger.info(
                    "Waiting for ${GleifGoldenCopyIngestor.WAIT_TIME_IN_MS / GleifGoldenCopyIngestor.MS_PER_S}s " +
                        "backend to be available. Exception was: ${exception.message}.",
                )
                Thread.sleep(GleifGoldenCopyIngestor.WAIT_TIME_IN_MS)
            }
        }
    }
}
