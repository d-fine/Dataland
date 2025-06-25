package org.dataland.batchmanager.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.CsvParser
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.dataland.datalandbatchmanager.service.GleifGoldenCopyIngestor
import org.dataland.datalandbatchmanager.service.IsinDeltaBuilder
import org.dataland.datalandbatchmanager.service.NorthDataAccessor
import org.dataland.datalandbatchmanager.service.NorthdataDataIngestor
import org.dataland.datalandbatchmanager.service.ProcessDataUpdates
import org.dataland.datalandbatchmanager.service.RelationshipExtractor
import org.dataland.datalandbatchmanager.service.RequestPriorityUpdater
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.MockedStatic
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.net.ConnectException
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi as BackendActuatorApi
import org.dataland.datalandcommunitymanager.openApiClient.api.ActuatorApi as CommunityActuatorApi

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
    private val mockBackendActuatorApi = mock(BackendActuatorApi::class.java)
    private val mockRequestPriorityUpdater = mock(RequestPriorityUpdater::class.java)
    private val mockCommunityActuatorApi = mock(CommunityActuatorApi::class.java)
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
    fun setupFiles() {
        oldFile = writeTextToTemporaryFile("oldFile.csv", oldContent)
        newFile = writeTextToTemporaryFile("newFile.csv", newContent)
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
        reset(mockBackendActuatorApi)
        reset(mockGleifGoldenCopyIngestorTest)
    }

    private fun writeTextToTemporaryFile(
        fileName: String,
        content: String,
    ): File =
        File(fileName).apply {
            writeText(content.trimIndent())
            deleteOnExit()
        }

    private fun initProcessDataUpdates(
        flagFileGleif: String? = null,
        flagFileGleifUpdate: String? = null,
        flagFileNorthData: String? = null,
        forceIngestGleif: Boolean = false,
        forceIngestNorth: Boolean = false,
        isinFile: File = oldFile,
    ) {
        processDataUpdates =
            ProcessDataUpdates(
                mockGleifApiAccessor,
                mockGleifGoldenCopyIngestorTest,
                mockNorthDataAccessor,
                mockNorthDataIngestorTest,
                mockBackendActuatorApi,
                mockRequestPriorityUpdater,
                mockCommunityActuatorApi,
                forceIngestGleif,
                forceIngestNorth,
                flagFileGleif,
                flagFileGleifUpdate,
                flagFileNorthData,
                isinFile,
            )
    }

    @Test
    fun `test ingestion is not executed if no flag file is provided`() {
        val mockStaticFile = mockStatic(File::class.java)
        initProcessDataUpdates()
        processDataUpdates.processFullGoldenCopyFileIfEnabled()
        mockStaticFile.verify({ File.createTempFile(any(), any()) }, times(0))
        mockStaticFile.close()
    }

    @Test
    fun `test ingestion performs successfully if a file is provided`() {
        val (emptyBufferedReader, mockStaticFile) = mockFileIngestion(oldFile)
        `when`(File.createTempFile(anyString(), anyString())).thenReturn(mock(File::class.java))

        val mockFileUtils = mockStatic(FileUtils::class.java)

        `when`(mockBackendActuatorApi.health()).thenThrow(ConnectException()).thenReturn(true)
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
        val flagFileGleifUpdate = File.createTempFile("test1", ".csv")
        val flagFileNorthdata = File.createTempFile("test2", ".csv")
        val bufferedReader = BufferedReader(BufferedReader.nullReader())

        companyIngestor =
            GleifGoldenCopyIngestor(
                mockGleifApiAccessor, mockCsvParser, mockCompanyUploader, mockIsinDeltaBuilder,
                mockRelationshipExtractor,
                File.createTempFile("tesd", ".csv"),
            )

        companyIngestorNorthData = NorthdataDataIngestor(mockCompanyUploader, mockCsvParser)

        processDataUpdates =
            ProcessDataUpdates(
                mockGleifApiAccessor, companyIngestor, mockNorthDataAccessor,
                companyIngestorNorthData, mockBackendActuatorApi,
                mockRequestPriorityUpdater,
                mockCommunityActuatorApi, allGleifCompaniesForceIngest = false, allNorthDataCompaniesForceIngest = false,
                flagFileGleif.absolutePath, flagFileGleifUpdate.absolutePath, flagFileNorthdata.absolutePath, isinMappingFile,
            )
        return Pair(bufferedReader, mockStatic(File::class.java))
    }

    @Test
    fun `waitForCommunityManager should stop on first successful health check`() {
        whenever(mockCommunityActuatorApi.health()).thenReturn(Any())
        initProcessDataUpdates()
        processDataUpdates.waitForCommunityManager()
        verify(mockCommunityActuatorApi).health()
    }

    @Test
    fun `logFlagFileFoundAndDelete logs success and deletes existing file`() {
        val mockFile = mock(File::class.java)
        whenever(mockFile.exists()).thenReturn(true)
        whenever(mockFile.delete()).thenReturn(true)

        initProcessDataUpdates()

        val method = processDataUpdates.javaClass.getDeclaredMethod("logFlagFileFoundAndDelete", File::class.java)
        method.isAccessible = true
        method.invoke(processDataUpdates, mockFile)

        verify(mockFile).delete()
    }

    @Test
    fun `logFlagFileFoundAndDelete logs error if file cannot be deleted`() {
        val mockFile = mock(File::class.java)
        whenever(mockFile.exists()).thenReturn(true)
        whenever(mockFile.delete()).thenReturn(false)

        // Set up logger and attach our test appender
        val logger = LoggerFactory.getLogger(ProcessDataUpdates::class.java) as Logger
        val appender = TestLogAppender()
        appender.start()
        logger.addAppender(appender)

        initProcessDataUpdates()

        val method = processDataUpdates.javaClass.getDeclaredMethod("logFlagFileFoundAndDelete", File::class.java)
        method.isAccessible = true
        method.invoke(processDataUpdates, mockFile)

        verify(mockFile).delete()
        assert(appender.events.any { it.level == Level.ERROR && it.formattedMessage.contains("Unable to delete flag file") })
    }

    @Test
    fun `processNorthDataFullGoldenCopyFileIfEnabled logs message when no flag or force ingest is set`() {
        // Set up logger and attach test appender
        val logger = LoggerFactory.getLogger(ProcessDataUpdates::class.java) as Logger
        val appender = TestLogAppender()
        initProcessDataUpdates(forceIngestNorth = false, flagFileNorthData = null)
        appender.start()
        logger.addAppender(appender)

        // Run the method
        processDataUpdates.processNorthDataFullGoldenCopyFileIfEnabled()

        assert(
            appender.events.any {
                it.level == Level.INFO && it.formattedMessage.contains("NorthData flag file not present & no force update variable set")
            },
        )
    }

    @Test
    fun `processUpdates does not trigger full update if flag file is missing`() {
        val missingFlag = File("nonexistent.flag")
        val dummyIsin = File.createTempFile("dummy", ".csv")

        initProcessDataUpdates(flagFileGleifUpdate = missingFlag.absolutePath, isinFile = dummyIsin)

        val method = processDataUpdates.javaClass.getDeclaredMethod("processUpdates")
        method.isAccessible = true
        method.invoke(processDataUpdates)

        verify(mockGleifGoldenCopyIngestorTest, never()).prepareGleifDeltaFile(anyBoolean())
        verify(mockGleifGoldenCopyIngestorTest, never()).processIsinMappingFile(anyBoolean())
        verify(mockGleifGoldenCopyIngestorTest, never()).processRelationshipFile(anyBoolean())
    }

    @Test
    fun `processUpdates triggers full update and deletes flag file if present`() {
        val flagFile = File.createTempFile("gleif_manual_update", ".flag")
        flagFile.writeText("trigger")

        assert(flagFile.exists())

        val dummyIsin = File.createTempFile("dummy_isin", ".csv")
        dummyIsin.writeText("LEI,ISIN\n1000,1111")

        initProcessDataUpdates(flagFileGleifUpdate = flagFile.absolutePath, isinFile = dummyIsin)

        val logger = LoggerFactory.getLogger(ProcessDataUpdates::class.java) as Logger
        val appender = TestLogAppender()
        appender.start()
        logger.addAppender(appender)

        val method = processDataUpdates.javaClass.getDeclaredMethod("processUpdates")
        method.isAccessible = true
        method.invoke(processDataUpdates)

        verify(mockGleifGoldenCopyIngestorTest).prepareGleifDeltaFile(true)
        verify(mockGleifGoldenCopyIngestorTest).processIsinMappingFile(true)
        verify(mockGleifGoldenCopyIngestorTest).processRelationshipFile(true)

        assert(!flagFile.exists())

        assert(
            appender.events.any {
                it.level == Level.INFO &&
                    it.formattedMessage.contains("deleted successfully")
            },
        )
    }
}

class TestLogAppender : AppenderBase<ILoggingEvent>() {
    val events = mutableListOf<ILoggingEvent>()

    override fun append(eventObject: ILoggingEvent) {
        events.add(eventObject)
    }
}
