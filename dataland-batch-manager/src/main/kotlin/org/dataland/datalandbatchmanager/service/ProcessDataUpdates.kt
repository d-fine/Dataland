package org.dataland.datalandbatchmanager.service

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
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi as BackendActuatorApi
import org.dataland.datalandcommunitymanager.openApiClient.api.ActuatorApi as CommunityActuatorApi

/**
 * Class to execute scheduled tasks, like the import of the GLEIF or NorthData golden copy files
 * @param gleifApiAccessor downloads the golden copy files from GLEIF
 * @param gleifGoldenCopyIngestor reads in the csv file from GLEIF and creates GleifCompanyInformation objects
 * @param northDataAccessor downloads the golden copy files from NorthData
 * @param northdataDataIngestor reads in the csv file from NorthData and creates NorthDataCompanyInformation objects
 * @param backendActuatorApi the actuatorApi of the backend
 */
@Suppress("LongParameterList")
@Component
class ProcessDataUpdates(
    @Autowired private val gleifApiAccessor: GleifApiAccessor,
    @Autowired private val gleifGoldenCopyIngestor: GleifGoldenCopyIngestor,
    @Autowired private val northDataAccessor: NorthDataAccessor,
    @Autowired private val northdataDataIngestor: NorthdataDataIngestor,
    @Autowired private val backendActuatorApi: BackendActuatorApi,
    @Autowired private val requestPriorityUpdater: RequestPriorityUpdater,
    @Autowired private val communityActuatorApi: CommunityActuatorApi,
    @Value("\${dataland.dataland-batch-manager.get-all-gleif-companies.force:false}")
    private val allGleifCompaniesForceIngest: Boolean,
    @Value("\${dataland.dataland-batch-manager.get-all-northdata-companies.force:false}")
    private val allNorthDataCompaniesForceIngest: Boolean,
    @Value("\${dataland.dataland-batch-manager.get-all-gleif-companies.flag-file:#{null}}")
    private val allGleifCompaniesIngestFlagFilePath: String?,
    @Value("\${dataland.dataland-batch-manager.get-all-northdata-companies.flag-file:#{null}}")
    private val allNorthDataCompaniesIngestFlagFilePath: String?,
    @Value("\${dataland.dataland-batch-manager.isin-mapping-file}")
    private val savedIsinMappingFile: File,
) {
    companion object {
        const val MS_PER_S = 1000L
        const val MAX_WAITING_TIME_IN_MS = 10L * 60L * MS_PER_S
        const val WAIT_TIME_IN_MS: Long = 5000
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method that listens to a trigger event and calls the methods for GLEIF and NorthData updates
     */
    @EventListener(ApplicationReadyEvent::class)
    fun processExternalCompanyDataIfEnabled() {
        processFullGoldenCopyFileIfEnabled()
        processNorthDataFullGoldenCopyFileIfEnabled()
    }

    /**
     * Downloads the entire GLEIF golden copy file and uploads all included companies to the Dataland Backend.
     * Does so only if the property "dataland.dataland-batch-manager.get-all-gleif-companies" is set.
     */
    fun processFullGoldenCopyFileIfEnabled() {
        val flagFileGleif = allGleifCompaniesIngestFlagFilePath?.let { File(it) }
        if (allGleifCompaniesForceIngest || flagFileGleif?.exists() == true) {
            logger.info("Found flag file or force ingest flag for GLEIF.")
            logFlagFileFoundAndDelete(flagFileGleif)
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
            logger.info("Gleif flag file not present & no force update variable set => Not performing any download")
        }
    }

    private fun logFlagFileFoundAndDelete(flagFile: File?) {
        if (flagFile?.exists() == true) {
            logger.info("Found collect all companies flag. Deleting it.")
            if (!flagFile.delete()) {
                logger.error(
                    "Unable to delete flag file $flagFile. Manually remove it or import will " +
                        "be triggered after service restart again.",
                )
            }
        }
    }

    /**
     * Downloads the entire NorthData golden copy file and uploads all included companies to the Dataland Backend.
     * Does so only if the property "dataland.dataland-batch-manager.get-all-northdata-companies" is set.
     */
    fun processNorthDataFullGoldenCopyFileIfEnabled() {
        val flagFileNorthData = allNorthDataCompaniesIngestFlagFilePath?.let { File(it) }
        if (allNorthDataCompaniesForceIngest || flagFileNorthData?.exists() == true) {
            logger.info("Found flag file or force ingest flag for NorthData.")
            logFlagFileFoundAndDelete(flagFileNorthData)
            waitForBackend()
            logger.info("Retrieving all company data available via NorthData.")
            northdataDataIngestor.processNorthdataFile(northDataAccessor::getFullGoldenCopy)
        } else {
            logger.info("NorthData flag file not present & no force update variable set => Not performing any download")
        }
    }

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 3 * * SUN")
    private fun processUpdates() {
        logger.info("Running scheduled update of GLEIF data.")
        waitForBackend()
        gleifGoldenCopyIngestor.prepareGleifDeltaFile()
        gleifGoldenCopyIngestor.processIsinMappingFile()
        gleifGoldenCopyIngestor.processRelationshipFile(updateAllCompanies = true)
    }

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 5 1-7 1,4,7,10 SUN")
    private fun processNorthDataUpdates() {
        logger.info("Running scheduled update of NorthData data.")
        waitForBackend()
        northdataDataIngestor.processNorthdataFile(northDataAccessor::getFullGoldenCopy)
    }

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 5 * * *")
    private fun processRequestPriorityUpdates() {
        logger.info("Running scheduled update of request priorities.")
        waitForCommunityManager()
        requestPriorityUpdater.processRequestPriorityUpdates()
    }

    /**
     * This method waits for the backend to be ready
     */
    fun waitForBackend() {
        val timeoutTime = Instant.now().toEpochMilli() + MAX_WAITING_TIME_IN_MS
        while (Instant.now().toEpochMilli() <= timeoutTime) {
            try {
                backendActuatorApi.health()
                break
            } catch (exception: ConnectException) {
                logger.info(
                    "Waiting for ${WAIT_TIME_IN_MS / MS_PER_S}s " +
                        "backend to be available. Exception was: ${exception.message}.",
                )
                Thread.sleep(WAIT_TIME_IN_MS)
            }
        }
    }

    /**
     * This method waits for the community manager to be ready
     */
    fun waitForCommunityManager() {
        val timeoutTime = Instant.now().toEpochMilli() + MAX_WAITING_TIME_IN_MS
        while (Instant.now().toEpochMilli() <= timeoutTime) {
            try {
                communityActuatorApi.health()
                break
            } catch (exception: ConnectException) {
                logger.info(
                    "Waiting for ${WAIT_TIME_IN_MS / MS_PER_S}s " +
                        "community manager to be available. Exception was: ${exception.message}.",
                )
                Thread.sleep(WAIT_TIME_IN_MS)
            }
        }
    }
}
