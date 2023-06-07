package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.datalandbatchmanager.gleif.Upload
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File

@Component
class Scheduler(
    @Autowired private val apiAccessor: GleifApiAccessor,
    @Autowired private val gleifParser: GleifCsvParser,
    @Autowired private val keycloakTokenManager: KeycloakTokenManager
) {
    @Scheduled(fixedDelay = 1000000000000, initialDelay = 0)
    private fun logging() {
        val tempFile = File.createTempFile("gleif_update_delta", ".csv")
        try {
            // apiAccessor.getLastMonthGoldenCopyDelta(tempFile)
            val deltaZipFilePath = File("/home/dfine/dataland_project/gleif/20230607-0800-gleif-goldencopy-lei2-last-month.csv.zip")

            val gleifDataStream = gleifParser.getCsvStreamFromZip(deltaZipFilePath)
            val gleifIterator = gleifParser.readGleifDataFromBufferedReader(gleifDataStream)
            ApiClient.accessToken = keycloakTokenManager.getAccessToken()
            Upload().uploadCompanies(gleifIterator.readAll().map { it.toCompanyInformation() })
        } finally {
            tempFile.delete()
        }
    }
}
