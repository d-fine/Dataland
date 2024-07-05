package org.dataland.batchmanager.service

import org.dataland.datalandbatchmanager.model.NorthDataCompanyInformation
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.dataland.datalandbatchmanager.service.NorthDataAccessor
import org.dataland.datalandbatchmanager.service.NorthdataDataIngestor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.any
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

    private val statusCodes = listOf("active", "terminated", "liquidation", "notCovered")
    private val infoIterable = mutableListOf<NorthDataCompanyInformation>()

    private val emptyNorthDataCompanyInformation = NorthDataCompanyInformation(
        headquarters = "",
        countryCode = "",
        companyName = "",
        headquartersPostalCode = "",
        lei = "",
        registerId = "",
        status = "",
        sector = "",
        street = "",
        vatId = "",
    )

    @BeforeAll
    fun createFakeIterable() {
        statusCodes.forEach { statusCode ->
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

        verify(mockCompanyUploader, times(2))
            .uploadOrPatchSingleCompany(any() ?: emptyNorthDataCompanyInformation)

        verify(mockCompanyUploader, times(1))
            .uploadOrPatchSingleCompany(infoIterable[statusCodes.indexOf("active")])
        verify(mockCompanyUploader, times(1))
            .uploadOrPatchSingleCompany(infoIterable[statusCodes.indexOf("liquidation")])
        verify(mockCompanyUploader, times(0))
            .uploadOrPatchSingleCompany(infoIterable[statusCodes.indexOf("terminated")])

        mockStaticFile.close()
    }
}
