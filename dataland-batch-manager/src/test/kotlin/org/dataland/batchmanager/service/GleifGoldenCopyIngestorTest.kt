package org.dataland.batchmanager.service

import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbackend.openApiClient.api.IsinLeiDataControllerApi
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.CsvParser
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.dataland.datalandbatchmanager.service.GleifGoldenCopyIngestor
import org.dataland.datalandbatchmanager.service.RelationshipExtractor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GleifGoldenCopyIngestorTest {
    private val mockIsinLeiDataControllerApi = mock<IsinLeiDataControllerApi>()
    private val mockGleifApiAccessor = mock<GleifApiAccessor>()
    private val mockCsvParser = mock<CsvParser>()
    private val mockCompanyUploader = mock<CompanyUploader>()
    private val mockActuatorApi = mock<ActuatorApi>()
    private val mockRelationshipExtractor = mock<RelationshipExtractor>()
    private val mockFile = mock<File>()
    private lateinit var gleifGoldenCopyIngestor: GleifGoldenCopyIngestor
    private val isinLeiContent =
        """
        LEI,ISIN
        1000,1111
        2000,2222
        """.trimIndent()

    @BeforeEach
    fun setupTest() {
        reset(mockGleifApiAccessor, mockCsvParser, mockCompanyUploader, mockActuatorApi, mockRelationshipExtractor)
        gleifGoldenCopyIngestor =
            GleifGoldenCopyIngestor(
                mockIsinLeiDataControllerApi, mockGleifApiAccessor, mockCsvParser, mockCompanyUploader, mockRelationshipExtractor,
            )
    }

    @Test
    fun `test GLEIF LEI file update process`() {
        val bufferedReader = BufferedReader(FileReader("./build/resources/test/GleifTestData.csv"))
        val staticMock = mockStatic(File::class.java)
        staticMock
            .`when`<File> {
                File.createTempFile(any(), any())
            }.thenReturn(mockFile)

        doReturn(bufferedReader).whenever(mockCsvParser).getCsvStreamFromZip(mockFile)
        doReturn(CsvParser().readGleifCompanyDataFromBufferedReader(bufferedReader))
            .whenever(
                mockCsvParser,
            ).readGleifCompanyDataFromBufferedReader(bufferedReader)

        gleifGoldenCopyIngestor.prepareGleifDeltaFile()

        verify(mockGleifApiAccessor, times(1)).getLastMonthGoldenCopyDelta(mockFile)
        verify(mockCompanyUploader, times(1))
            .uploadOrPatchSingleCompany(any())

        staticMock.close()
    }

    @Test
    fun `extractIsinLeiMapping parses CSV correctly`() {
        val tempFile = File.createTempFile("test_isin_lei", ".csv")
        tempFile.writeText(isinLeiContent.trimIndent())
        val result = gleifGoldenCopyIngestor.extractIsinLeiMapping(tempFile)
        assertEquals(2, result.size)
        assertEquals("1111", result[0].isin)
        assertEquals("1000", result[0].lei)
        assertEquals("2222", result[1].isin)
        assertEquals("2000", result[1].lei)
        tempFile.deleteOnExit()
    }
}
