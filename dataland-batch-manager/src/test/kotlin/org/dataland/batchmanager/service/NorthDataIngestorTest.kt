package org.dataland.batchmanager.service

import org.dataland.datalandbatchmanager.model.NorthDataCompanyInformation
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.dataland.datalandbatchmanager.service.NorthDataAccessor
import org.dataland.datalandbatchmanager.service.NorthdataDataIngestor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.io.BufferedReader
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NorthDataIngestorTest {
    private val mockNorthDataAccessor = mock(NorthDataAccessor::class.java)
    private val mockCsvParser = mock(GleifCsvParser::class.java)
    private val mockCompanyUploader = mock(CompanyUploader::class.java)

    // Map status codes to number of expected invocations of mockCompanyUploader.uploadOrPatchSingleCompany
    private val statusCodes = mapOf(
        "active" to 1,
        "terminated" to 0,
        "liquidation" to 1,
        "" to 1,
        "notCovered" to 0,
    )

    private val infoIterable = mutableListOf<NorthDataCompanyInformation>()

    @BeforeAll
    fun createFakeIterable() {
        statusCodes.keys.forEach { statusCode ->
            val thisMock = mock(NorthDataCompanyInformation::class.java)
            `when`(thisMock.status).thenReturn(statusCode)
            infoIterable.add(thisMock)
        }
    }

    @Test
    fun `test NorthData update process`() {
        val mockStaticFile = mockStatic(File::class.java)

        val mockFile = mock(File::class.java)
        `when`(File.createTempFile(anyString(), anyString())).thenReturn(mockFile)
        val emptyBufferedReader = BufferedReader(BufferedReader.nullReader())
        `when`(mockCsvParser.getCsvStreamFromNorthDataZipFile(mockFile)).thenReturn(emptyBufferedReader)
        `when`(mockCsvParser.readNorthDataFromBufferedReader(emptyBufferedReader)).thenReturn(infoIterable)

        val northDataIngestor = NorthdataDataIngestor(mockCompanyUploader, mockCsvParser)
        northDataIngestor.processNorthdataFile(mockNorthDataAccessor::getFullGoldenCopy)

        statusCodes.forEach {
            verify(mockCompanyUploader, times(it.value))
                .uploadOrPatchSingleCompany(infoIterable[statusCodes.keys.indexOf(it.key)])
        }

        mockStaticFile.close()
    }
}
