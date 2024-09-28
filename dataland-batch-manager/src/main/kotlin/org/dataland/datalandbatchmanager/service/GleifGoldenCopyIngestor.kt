package org.dataland.datalandbatchmanager.service

import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.model.GleifCompanyCombinedInformation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.util.Locale
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
    @Autowired private val gleifParser: CsvParser,
    @Autowired private val companyUploader: CompanyUploader,
    @Autowired private val isinDeltaBuilder: IsinDeltaBuilder,
    @Autowired private val relationshipExtractor: RelationshipExtractor,
    @Value("\${dataland.dataland-batch-manager.isin-mapping-file}")
    private val savedIsinMappingFile: File,
) {
    companion object {
        const val UPLOAD_THREAD_POOL_SIZE = 32
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Starting point for GLEIF delta file handling
     */
    fun prepareGleifDeltaFile() {
        logger.info("Starting Gleif company update cycle for latest delta file.")
        val tempFile = File.createTempFile("gleif_update_delta", ".zip")
        processGleifFile(tempFile, gleifApiAccessor::getLastMonthGoldenCopyDelta)
    }

    /**
     * This method processes the downloaded gleif zip file
     * @param zipFile the file into which the gleif data should be saved
     * @param downloadFile the method which is executed to retrieve the external data
     */
    @Synchronized
    fun processGleifFile(
        zipFile: File,
        downloadFile: (file: File) -> Unit,
    ) {
        val duration =
            measureTime {
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

    /**
     * This method processes the gleif relationship file
     * @param updateAllCompanies boolean to control whether all companies should be updated or not
     */
    @Synchronized
    fun processRelationshipFile(updateAllCompanies: Boolean = false) {
        logger.info("Starting parent mapping update cycle for latest file.")
        val newRelationshipFile = File.createTempFile("gleif_relationship_golden_copy", ".zip")
        val duration =
            measureTime {
                gleifApiAccessor.getFullGoldenCopyOfRelationships(newRelationshipFile)
                val gleifDataStream = gleifParser.getCsvStreamFromZip(newRelationshipFile)
                val gleifCsvParser = gleifParser.readGleifRelationshipDataFromBufferedReader(gleifDataStream)
                relationshipExtractor.prepareFinalParentMapping(gleifCsvParser)
                if (updateAllCompanies) companyUploader.updateRelationships(relationshipExtractor.finalParentMapping)
            }
        logger.info("Finished processing of GLEIF RR file $newRelationshipFile in ${formatExecutionTime(duration)}.")
    }

    /**
     * Starting point for ISIN mapping file handling
     */
    @Synchronized
    fun processIsinMappingFile() {
        logger.info("Starting LEI-ISIN mapping update cycle for latest file.")
        val newMappingFile = File.createTempFile("gleif_mapping_update", ".csv")
        val duration =
            measureTime {
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

    private fun uploadCompanies(zipFile: File) {
        val gleifDataStream = gleifParser.getCsvStreamFromZip(zipFile)
        val gleifIterable = gleifParser.readGleifCompanyDataFromBufferedReader(gleifDataStream)

        val uploadThreadPool = ForkJoinPool(UPLOAD_THREAD_POOL_SIZE)
        try {
            uploadThreadPool
                .submit {
                    StreamSupport
                        .stream(gleifIterable.spliterator(), true)
                        .forEach {
                            companyUploader.uploadOrPatchSingleCompany(
                                GleifCompanyCombinedInformation(
                                    it,
                                    relationshipExtractor.finalParentMapping.getOrDefault(it.lei, null),
                                ),
                            )
                        }
                }.get()
        } finally {
            uploadThreadPool.shutdown()
        }
    }

    private fun formatExecutionTime(duration: Duration): String =
        duration
            .toComponents { hours, minutes, seconds, _ ->
                String.format(
                    Locale.getDefault(), "%02dh %02dm %02ds", hours, minutes, seconds,
                )
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
