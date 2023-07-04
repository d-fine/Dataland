package org.dataland.batchmanager.service

import com.fasterxml.jackson.databind.MappingIterator
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.dataland.datalandbatchmanager.service.GleifGoldenCopyIngestor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.io.BufferedReader
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GleifGoldenCopyIngestorTest {
    private val mockGleifApiAccessor = mock(GleifApiAccessor::class.java)
    private val mockGleifCsvParser = mock(GleifCsvParser::class.java)
    private val mockCompanyUploader = mock(CompanyUploader::class.java)
    private val mockActuatorApi = mock(ActuatorApi::class.java)
    private lateinit var companyIngestor: GleifGoldenCopyIngestor

    @BeforeEach
    fun setupTest() {
        reset(mockGleifApiAccessor)
        reset(mockGleifCsvParser)
        reset(mockCompanyUploader)
        reset(mockActuatorApi)
    }

    @Test
    fun `test ingestion is not executed if no flag file is provided`() {
        val mockStaticFile = mockStatic(File::class.java)
        companyIngestor = GleifGoldenCopyIngestor(
            mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUploader, mockActuatorApi,
            false, null,
        )
        companyIngestor.processFullGoldenCopyFileIfEnabled()
        mockStaticFile.verify({ File.createTempFile(any(), any()) }, times(0))
        mockStaticFile.close()
    }

    @Test
    fun `test ingestion performs successfully if a file is provided`() {
        val flagFile = File.createTempFile("test", ".csv", File("./"))
        val emptyBufferedReader = BufferedReader(BufferedReader.nullReader())
        `when`(
            mockGleifCsvParser.readGleifDataFromBufferedReader(
                any()
                    ?: emptyBufferedReader,
            ),
        )
            .thenReturn(MappingIterator.emptyIterator())
        companyIngestor = GleifGoldenCopyIngestor(
            mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUploader, mockActuatorApi,
            false, flagFile.absolutePath,
        )
        val mockStaticFile = mockStatic(File::class.java)
        `when`(File.createTempFile(anyString(), anyString())).thenReturn(mock(File::class.java))
        companyIngestor.processFullGoldenCopyFileIfEnabled()
        mockStaticFile.verify({ File.createTempFile(any(), any()) }, times(1))
        verify(mockGleifCsvParser, times(1)).readGleifDataFromBufferedReader(any() ?: emptyBufferedReader)
        mockStaticFile.close()
    }
}
