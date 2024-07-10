package org.dataland.batchmanager.service

import org.apache.commons.io.FileUtils
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.CsvParser
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.dataland.datalandbatchmanager.service.GleifGoldenCopyIngestor
import org.dataland.datalandbatchmanager.service.IsinDeltaBuilder
import org.dataland.datalandbatchmanager.service.NorthDataAccessor
import org.dataland.datalandbatchmanager.service.NorthdataDataIngestor
import org.dataland.datalandbatchmanager.service.ProcessDataUpdates
import org.dataland.datalandbatchmanager.service.RelationshipExtractor
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.MockedStatic
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
import java.net.ConnectException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessDataUpdatesTest {
    private val mockGleifApiAccessor = mock(GleifApiAccessor::class.java)
    private val mockGleifGoldenCopyIngestorTest = mock(GleifGoldenCopyIngestor::class.java)
    private val mockNorthDataAccessor = mock(NorthDataAccessor::class.java)
    private val mockNorthDataIngestorTest = mock(NorthdataDataIngestor::class.java)
    private val mockCsvParser = mock(CsvParser::class.java)
    private val mockCompanyUploader = mock(CompanyUploader::class.java)
    private val mockIsinDeltaBuilder = mock(IsinDeltaBuilder::class.java)
    private val mockRelationshipExtractor = mock(RelationshipExtractor::class.java)
    private val mockActuatorApi = mock(ActuatorApi::class.java)
    private lateinit var processDataUpdates: ProcessDataUpdates
    private lateinit var companyIngestor: GleifGoldenCopyIngestor
    private lateinit var companyIngestorNorthData: NorthdataDataIngestor

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
        reset(mockCsvParser)
        reset(mockActuatorApi)
        reset(mockGleifGoldenCopyIngestorTest)
    }

    @Test
    fun `test ingestion is not executed if no flag file is provided`() {
        val mockStaticFile = mockStatic(File::class.java)
        processDataUpdates = ProcessDataUpdates(
            mockGleifApiAccessor, mockGleifGoldenCopyIngestorTest, mockNorthDataAccessor, mockNorthDataIngestorTest,
            mockActuatorApi,
            false, false,
            null, null, oldFile,
        )
        processDataUpdates.processFullGoldenCopyFileIfEnabled()
        mockStaticFile.verify({ File.createTempFile(any(), any()) }, times(0))
        mockStaticFile.close()
    }

    @Test
    fun `test ingestion performs successfully if a file is provided`() {
        val (emptyBufferedReader, mockStaticFile) = mockFileIngestion(oldFile)
        `when`(File.createTempFile(anyString(), anyString())).thenReturn(mock(File::class.java))

        val mockFileUtils = mockStatic(FileUtils::class.java)

        `when`(mockActuatorApi.health()).thenThrow(ConnectException()).thenReturn(true)
        mock(Thread::class.java)

        processDataUpdates.processExternalCompanyDataIfEnabled()

        val numberOfDownloadedFiles = 3
        mockStaticFile.verify({ File.createTempFile(any(), any()) }, times(numberOfDownloadedFiles))
        verify(mockCsvParser, times(1)).readGleifCompanyDataFromBufferedReader((any() ?: emptyBufferedReader))
        verify(mockCsvParser, times(1)).readGleifRelationshipDataFromBufferedReader((any() ?: emptyBufferedReader))
        verify(mockCsvParser, times(1)).readNorthDataFromBufferedReader((any() ?: emptyBufferedReader))
        mockStaticFile.close()
        mockFileUtils.close()
    }

    @Test
    fun `test error when mapping file still exists`() {
        val mockOldIsinFile = mock(File::class.java)
        mockFileIngestion(mockOldIsinFile)
        `when`(mockOldIsinFile.exists()).thenReturn(true)
        `when`(mockOldIsinFile.delete()).thenReturn(false)
        assertThrows<FileSystemException> { processDataUpdates.processFullGoldenCopyFileIfEnabled() }
    }

    private fun mockFileIngestion(isinMappingFile: File): Pair<BufferedReader, MockedStatic<File>> {
        val flagFileGleif = File.createTempFile("test", ".csv")
        val flagFileNorthdata = File.createTempFile("test2", ".csv")
        val bufferedReader = BufferedReader(BufferedReader.nullReader())

        companyIngestor = GleifGoldenCopyIngestor(
            mockGleifApiAccessor, mockCsvParser, mockCompanyUploader, mockIsinDeltaBuilder,
            mockRelationshipExtractor,
            File.createTempFile("tesd", ".csv"),
        )

        companyIngestorNorthData = NorthdataDataIngestor(mockCompanyUploader, mockCsvParser)

        processDataUpdates = ProcessDataUpdates(
            mockGleifApiAccessor, companyIngestor, mockNorthDataAccessor,
            companyIngestorNorthData, mockActuatorApi,
            false, false,
            flagFileGleif.absolutePath, flagFileNorthdata.absolutePath, isinMappingFile,
        )
        val mockStaticFile = mockStatic(File::class.java)
        return Pair(bufferedReader, mockStaticFile)
    }
}
