package org.dataland.batchmanager.service

import com.fasterxml.jackson.databind.MappingIterator
import org.apache.commons.io.FileUtils
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbatchmanager.model.GleifCompanyInformation
import org.dataland.datalandbatchmanager.service.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.MockedStatic
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.Mockito.spy
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
    private val mockRelationshipExtractor = mock(RelationshipExtractor::class.java)
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
        reset(mockRelationshipExtractor)
    }



    @Test
    fun `test GLEIF LEI file update process`() {
        val (emptyBufferedReader, mockStaticFile) = commonMock()
        `when`(File.createTempFile(anyString(), anyString())).thenReturn(mock(File::class.java))
        companyIngestor.prepareGleifDeltaFile()
        verify(mockGleifCsvParser, times(1)).readDataFromBufferedReader<GleifCompanyInformation>(any() ?: emptyBufferedReader)
        mockStaticFile.close()
    }

    private fun commonMock(): Pair<BufferedReader, MockedStatic<File>> {
        val emptyBufferedReader = BufferedReader(BufferedReader.nullReader())
        `when`(
            mockGleifCsvParser.readDataFromBufferedReader<GleifCompanyInformation>(
                any()
                    ?: emptyBufferedReader,
            ),
        )
            .thenReturn( emptyList())
        companyIngestor = GleifGoldenCopyIngestor(
            mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUploader, mockIsinDeltaBuilder,
            mockRelationshipExtractor,
            oldFile,
        )
        val mockStaticFile = mockStatic(File::class.java)
        return Pair(emptyBufferedReader, mockStaticFile)
    }

    @Test
    fun`test replacement of delta mapping file`() {
        val flagFile = File.createTempFile("test", ".csv")
        flagFile.deleteOnExit()
        val newLines: List<String> = File(newFile.toString()).useLines { lines -> lines.take(5).toList() }
        companyIngestor = GleifGoldenCopyIngestor(
            mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUploader, mockIsinDeltaBuilder,
            mockRelationshipExtractor,
            oldFile,
        )

        assert(newFile.exists())
        assert(oldFile.exists())

        companyIngestor.replaceOldMappingFile(newFile)
        val movedLines: List<String> = File(oldFile.toString()).useLines { lines -> lines.take(5).toList() }

        assert(!newFile.exists())
        assert(oldFile.exists())
        assert(movedLines.hashCode() == newLines.hashCode())
    }

    @Test
    fun`test failing of file deletion`() {
        val flagFile = File.createTempFile("flagFile", ".csv")
        flagFile.deleteOnExit()
        companyIngestor = GleifGoldenCopyIngestor(
            mockGleifApiAccessor, mockGleifCsvParser, mockCompanyUploader, mockIsinDeltaBuilder,
            mockRelationshipExtractor,
            oldFile,
        )

        val newMappingFile = spy(
            File.createTempFile("newMappingFile", ".csv").apply {
                deleteOnExit()
            },
        )
        doReturn(false).`when`(newMappingFile).delete()
        companyIngestor.replaceOldMappingFile(newMappingFile)

        assert(newMappingFile.exists())
    }
}
