package org.dataland.batchmanager.service

import org.dataland.datalandbatchmanager.model.NorthDataCompanyInformation
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.CsvParser
import org.dataland.datalandbatchmanager.service.NorthDataAccessor
import org.dataland.datalandbatchmanager.service.NorthdataDataIngestor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatchers.any
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
    private val mockCsvParser = mock(CsvParser::class.java)
    private val mockCompanyUploader = mock(CompanyUploader::class.java)

    private val dummyLei = "dummyLei"
    private val dummyVatId = "dummyVatId"
    private val dummyRegisterId = "dummyRegisterId"

    // Map status codes and identifier values to
    // number of expected invocations of mockCompanyUploader.uploadOrPatchSingleCompany
    private val uploadsForCombinationsOfStatusAndIdentifiers = mapOf(
        arrayOf("active", dummyLei, dummyRegisterId, dummyVatId) to 1,
        arrayOf("terminated", dummyLei, dummyRegisterId, dummyVatId) to 0,
        arrayOf("liquidation", dummyLei, dummyRegisterId, dummyVatId) to 1,
        arrayOf("", dummyLei, dummyRegisterId, dummyVatId) to 1,
        arrayOf("notCovered", dummyLei, dummyRegisterId, dummyVatId) to 0,
        arrayOf("active", "", "", "") to 0,
        arrayOf("liquidation", "", "", "") to 0,
        arrayOf("", "", "", "") to 0,
        arrayOf("active", dummyLei, dummyRegisterId, "") to 1,
        arrayOf("active", dummyLei, "", "") to 1,
        arrayOf("active", "", "", dummyVatId) to 1,
    )

    private val infoIterable = mutableListOf<NorthDataCompanyInformation>()

    @BeforeAll
    fun createFakeIterable() {
        uploadsForCombinationsOfStatusAndIdentifiers.keys.forEach { statusAndIdentifiers ->
            val thisMock = mock(NorthDataCompanyInformation::class.java)
            `when`(thisMock.status).thenReturn(statusAndIdentifiers[0])
            `when`(thisMock.lei).thenReturn(statusAndIdentifiers[1])
            `when`(thisMock.registerId).thenReturn(statusAndIdentifiers[2])
            `when`(thisMock.vatId).thenReturn(statusAndIdentifiers[3])
            infoIterable.add(thisMock)
        }
    }

    @Test
    fun `test NorthData update process`() {
        val mockStaticFile = mockStatic(File::class.java)

        val mockFile = mock(File::class.java)
        val emptyBufferedReader = BufferedReader(BufferedReader.nullReader())
        `when`(mockCsvParser.getCsvStreamFromNorthDataZipFile(any() ?: mockFile)).thenReturn(emptyBufferedReader)
        `when`(mockCsvParser.readNorthDataFromBufferedReader(emptyBufferedReader)).thenReturn(infoIterable)

        val northDataIngestor = NorthdataDataIngestor(mockCompanyUploader, mockCsvParser)
        northDataIngestor.processNorthdataFile(mockNorthDataAccessor::getFullGoldenCopy)

        uploadsForCombinationsOfStatusAndIdentifiers.forEach {
            verify(mockCompanyUploader, times(it.value))
                .uploadOrPatchSingleCompany(
                    infoIterable[uploadsForCombinationsOfStatusAndIdentifiers.keys.indexOf(it.key)],
                )
        }

        mockStaticFile.close()
    }
}
