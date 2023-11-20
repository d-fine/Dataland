package org.dataland.batchmanager.service

import com.fasterxml.jackson.databind.MappingIterator
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.dataland.datalandbatchmanager.service.GleifGoldenCopyIngestor
import org.dataland.datalandbatchmanager.service.IsinDeltaBuilder
import org.junit.jupiter.api.AfterAll
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
import java.io.PrintWriter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GleifGoldenCopyIngestorTest {
    private val mockGleifApiAccessor = mock(GleifApiAccessor::class.java)
    private val mockGleifCsvParser = mock(GleifCsvParser::class.java)
    private val mockCompanyUploader = mock(CompanyUploader::class.java)
    private val mockActuatorApi = mock(ActuatorApi::class.java)
    private val mockIsinDeltaBuilder = mock(IsinDeltaBuilder::class.java)
    private lateinit var companyIngestor: GleifGoldenCopyIngestor

    private lateinit var oldFile: File
    private lateinit var newFile: File

    val oldContent = """
            LEI,ISIN
            1000,1111
            1000,1112
            2000,2222
            3000,3333
            3000,3334
            4000,4444
            6000,6666
            6000,6667
        """

    val newContent = """
            LEI,ISIN
            1000,1111
            1000,1112
            1000,1113
            2000,2222
            3000,3333
            4000, 
            5000,5555
            6000,6666
            6000,6667
        """

    @BeforeEach
    fun setup() {
        oldContent.trimIndent()
        newContent.trimIndent()
//        create file oldFile
        oldFile = File("oldFile.csv")
        var printWriter = PrintWriter(oldFile)
        printWriter.println(oldContent)
        printWriter.close()
//        create file newFile
        newFile = File("newFile.csv")
        printWriter = PrintWriter(newFile)
        printWriter.println(newContent)
        printWriter.close()
    }

    @AfterAll
    fun cleanup() {
        oldFile.deleteOnExit()
        newFile.deleteOnExit()
    }

    @BeforeEach
    fun setupTest() {
        reset(mockGleifApiAccessor)
        reset(mockGleifCsvParser)
        reset(mockCompanyUploader)
        reset(mockActuatorApi)
        reset(mockIsinDeltaBuilder)
    }

    @Test
    fun `test ingestion is not executed if no flag file is provided`() {
        val mockStaticFile = mockStatic(File::class.java)
        companyIngestor = GleifGoldenCopyIngestor(
            mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUploader, mockActuatorApi, mockIsinDeltaBuilder,
            false, null, oldFile,
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
            mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUploader, mockActuatorApi, mockIsinDeltaBuilder,
            false, flagFile.absolutePath, oldFile,
        )
        val mockStaticFile = mockStatic(File::class.java)
        `when`(File.createTempFile(anyString(), anyString())).thenReturn(mock(File::class.java))
        companyIngestor.processFullGoldenCopyFileIfEnabled()
        mockStaticFile.verify({ File.createTempFile(any(), any()) }, times(2))
        verify(mockGleifCsvParser, times(1)).readGleifDataFromBufferedReader(any() ?: emptyBufferedReader)
        mockStaticFile.close()
    }

    @Test
    fun `test GLEIF-LEI file update process`() {
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
            mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUploader, mockActuatorApi, mockIsinDeltaBuilder,
            false, flagFile.absolutePath, oldFile,
        )
        val mockStaticFile = mockStatic(File::class.java)
        `when`(File.createTempFile(anyString(), anyString())).thenReturn(mock(File::class.java))
        companyIngestor.prepareGleifDeltaFile()
        verify(mockGleifCsvParser, times(1)).readGleifDataFromBufferedReader(any() ?: emptyBufferedReader)
        mockStaticFile.close()
    }

//    @Test
//    fun `test ISIN delta map update process`() {
//        val flagFile = File.createTempFile("test", ".csv", File("./"))
//        val emptyBufferedReader = BufferedReader(BufferedReader.nullReader())
//        `when`(
//                mockGleifCsvParser.readGleifDataFromBufferedReader(
//                        any()
//                                ?: emptyBufferedReader,
//                ),
//        )
//                .thenReturn(MappingIterator.emptyIterator())
//        companyIngestor = GleifGoldenCopyIngestor(
//                mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUploader, mockActuatorApi, mockIsinDeltaBuilder,
//                false, flagFile.absolutePath, oldFile,
//        )
//        val mockStaticFile = mockStatic(File::class.java)
//        `when`(File.createTempFile(anyString(), anyString())).thenReturn(mock(File::class.java))
//        companyIngestor.prepareIsinMappingFile()
//        verify(mockGleifCsvParser, times(1)).readGleifDataFromBufferedReader(any() ?: emptyBufferedReader)
//        mockStaticFile.close()
//    }

//    @Test
//    fun `test if new file moves in place of old file`() {
//        val newLines: List<String> = File(newFile.toString()).useLines { lines -> lines.take(5).toList() }
//        val flagFile = File.createTempFile("test", ".csv", File("./"))
//        val emptyBufferedReader = BufferedReader(BufferedReader.nullReader())
//        `when`(
//                mockGleifCsvParser.readGleifDataFromBufferedReader(
//                        any()
//                                ?: emptyBufferedReader,
//                ),
//        )
//                .thenReturn(MappingIterator.emptyIterator())
//        companyIngestor = GleifGoldenCopyIngestor(
//                mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUploader, mockActuatorApi, mockIsinDeltaBuilder,
//                false, flagFile.absolutePath, oldFile,
//        )
//        val mockStaticFile = mockStatic(File::class.java)
//        `when`(File.createTempFile(anyString(), anyString())).thenReturn(mock(File::class.java))
//        companyIngestor.replaceOldMappingFile(newFile)
//        mockStaticFile.close()
//
//        assert(!File("newFile.csv").exists())
//        assert(File("isinMapping.csv").exists())
//
//        val movedLines: List<String> = File(File("isinMapping.csv").toString()).useLines {
//                lines -> lines.take(5).toList()
//        }
//        assert(movedLines.hashCode().equals(newLines.hashCode()))
//    }
}
