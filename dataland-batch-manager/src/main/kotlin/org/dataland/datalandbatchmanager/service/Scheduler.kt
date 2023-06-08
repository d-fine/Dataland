package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.datalandbatchmanager.gleif.Upload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Component
class Scheduler(
    @Autowired private val apiAccessor: GleifApiAccessor,
    @Autowired private val gleifParser: GleifCsvParser,
    @Autowired private val keycloakTokenManager: KeycloakTokenManager
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000000000000, initialDelay = 0)
    private fun processDeltaFile() {
        logger.info("Starting update cycle for latest delta file.")
        val start = System.nanoTime()
        val tempFile = File.createTempFile("gleif_update_delta", ".csv")
        try {
            apiAccessor.getLastMonthGoldenCopyDelta(tempFile)

            val gleifDataStream = gleifParser.getCsvStreamFromZip(tempFile)
            //val gleifDataStream = gleifParser.getCsvStreamFromZip(deltaZipFilePath)
            val gleifIterator = gleifParser.readGleifDataFromBufferedReader(gleifDataStream)
            //gleifIterator.readAll().map { println(it) }
            ApiClient.accessToken = keycloakTokenManager.getAccessToken()
            var counter = 0
            val uploads = 100
            while (gleifIterator.hasNextValue()) {
                if (counter >= uploads) {
                    ApiClient.accessToken = keycloakTokenManager.getAccessToken()
                    counter = 0
                }
                Upload().uploadCompanies(listOf(gleifIterator.next().toCompanyInformation()))
                counter++
            }
            //Upload().uploadCompanies(gleifIterator.readAll().map { it.toCompanyInformation() })
        } finally {
            tempFile.delete()
        }

        val executionTime = (System.nanoTime() - start)
            .toDuration(DurationUnit.NANOSECONDS)
            .toComponents { hours, minutes, seconds, _ ->  String.format("%02dh %02dm %02ds", hours, minutes, seconds ) }
        logger.info("Finished update cycle for latest delta file in $executionTime.")
    }
}
