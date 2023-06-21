package org.dataland.batchmanager.service

import com.fasterxml.jackson.databind.MappingIterator
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbatchmanager.service.CompanyUpload
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.dataland.datalandbatchmanager.service.GleifGoldenCopyIngestor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import java.io.BufferedReader
import java.io.File

class GleifGoldenCopyIngestorTest {
    private val mockGleifApiAccessor = mock(GleifApiAccessor::class.java)
    private val mockGleifCsvParser = mock(GleifCsvParser::class.java)
    private val mockCompanyUpload = mock(CompanyUpload::class.java)
    private val mockActuatorApi = mock(ActuatorApi::class.java)
    private lateinit var companyIngestor: GleifGoldenCopyIngestor

    @BeforeEach
    fun setupTest() {
        reset(mockGleifApiAccessor)
        reset(mockGleifCsvParser)
        reset(mockCompanyUpload)
        reset(mockActuatorApi)
    }

    @Test
    fun `test failing ingestion`() {
        companyIngestor = GleifGoldenCopyIngestor(
            mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUpload, mockActuatorApi,
            false, null,
        )
        companyIngestor.processFullGoldenCopyFileIfEnabled()
    }

    @Test
    fun `test successful ingestion`() {
        val flagFile = File.createTempFile("test", ".csv", File("./"))
        `when`(
            mockGleifCsvParser.readGleifDataFromBufferedReader(
                any()
                    ?: BufferedReader(BufferedReader.nullReader()),
            ),
        )
            .thenReturn(MappingIterator.emptyIterator())
        companyIngestor = GleifGoldenCopyIngestor(
            mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUpload, mockActuatorApi,
            false, flagFile.absolutePath,
        )
        companyIngestor.processFullGoldenCopyFileIfEnabled()
    }
}
