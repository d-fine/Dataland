package org.dataland.batchmanager.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.dataland.datalandbackend.openApiClient.api.IsinLeiDataControllerApi
import org.dataland.datalandbatchmanager.service.CompanyInformationParser
import org.dataland.datalandbatchmanager.service.CompanyUploader
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.dataland.datalandbatchmanager.service.GleifGoldenCopyIngestor
import org.dataland.datalandbatchmanager.service.NorthDataAccessor
import org.dataland.datalandbatchmanager.service.NorthdataDataIngestor
import org.dataland.datalandbatchmanager.service.ProcessDataUpdates
import org.dataland.datalandbatchmanager.service.RelationshipExtractor
import org.dataland.datalandbatchmanager.service.RequestPriorityUpdater
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.ConnectException
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi as BackendActuatorApi
import org.dataland.datalandcommunitymanager.openApiClient.api.ActuatorApi as CommunityActuatorApi

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessDataUpdatesTest {
    private val mockIsinLeiDataControllerApi = mock<IsinLeiDataControllerApi>()
    private val mockGleifApiAccessor = mock<GleifApiAccessor>()
    private val mockGleifGoldenCopyIngestorTest = mock<GleifGoldenCopyIngestor>()
    private val mockNorthDataAccessor = mock<NorthDataAccessor>()
    private val mockNorthDataIngestorTest = mock<NorthdataDataIngestor>()
    private val mockCompanyInformationParser = mock<CompanyInformationParser>()
    private val mockCompanyUploader = mock<CompanyUploader>()
    private val mockRelationshipExtractor = mock<RelationshipExtractor>()
    private val mockBackendActuatorApi = mock<BackendActuatorApi>()
    private val mockRequestPriorityUpdater = mock<RequestPriorityUpdater>()
    private val mockCommunityActuatorApi = mock<CommunityActuatorApi>()
    private val mockFile = mock<File>()
    private lateinit var processDataUpdates: ProcessDataUpdates
    private lateinit var companyIngestor: GleifGoldenCopyIngestor
    private lateinit var companyIngestorNorthData: NorthdataDataIngestor

    @BeforeEach
    fun setupTest() {
        reset(mockGleifApiAccessor, mockCompanyInformationParser, mockBackendActuatorApi, mockGleifGoldenCopyIngestorTest)
    }

    private fun initProcessDataUpdates(
        gleifGoldenCopyIngestorTest: GleifGoldenCopyIngestor = mockGleifGoldenCopyIngestorTest,
        nothDataIngestorTest: NorthdataDataIngestor = mockNorthDataIngestorTest,
        flagFileGleif: String? = null,
        flagFileGleifUpdate: String? = null,
        flagFileNorthData: String? = null,
    ) {
        processDataUpdates =
            ProcessDataUpdates(
                mockGleifApiAccessor,
                gleifGoldenCopyIngestorTest,
                mockNorthDataAccessor,
                nothDataIngestorTest,
                mockBackendActuatorApi,
                mockRequestPriorityUpdater,
                mockCommunityActuatorApi,
                allGleifCompaniesForceIngest = false,
                allNorthDataCompaniesForceIngest = false,
                allGleifCompaniesIngestFlagFilePath = flagFileGleif,
                allGleifCompaniesIngestManualUpdateFlagFilePath = flagFileGleifUpdate,
                allNorthDataCompaniesIngestFlagFilePath = flagFileNorthData,
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
        val flagFileGleif = File.createTempFile("test", ".flag")
        val flagFileGleifUpdate = File.createTempFile("test1", ".flag")
        val flagFileNorthdata = File.createTempFile("test2", ".flag")
        val staticMock = mockStatic(File::class.java)
        try {
            staticMock
                .`when`<File> {
                    File.createTempFile(any(), any())
                }.thenReturn(mockFile)

            companyIngestor =
                GleifGoldenCopyIngestor(
                    mockIsinLeiDataControllerApi,
                    mockGleifApiAccessor,
                    mockCompanyInformationParser,
                    mockCompanyUploader,
                    mockRelationshipExtractor,
                )
            val ingestorSpy = spy(companyIngestor)
            doNothing()
                .whenever(
                    ingestorSpy,
                ).processIsinMappingFile()
            companyIngestorNorthData = NorthdataDataIngestor(mockCompanyUploader, mockCompanyInformationParser)

            initProcessDataUpdates(
                gleifGoldenCopyIngestorTest = ingestorSpy,
                nothDataIngestorTest = companyIngestorNorthData,
                flagFileGleif = flagFileGleif.absolutePath,
                flagFileGleifUpdate = flagFileGleifUpdate.absolutePath,
                flagFileNorthData = flagFileNorthdata.absolutePath,
            )

            val bufferedReader = BufferedReader(FileReader("./build/resources/test/GleifTestData.xml"))

            doReturn(bufferedReader).whenever(mockCompanyInformationParser).getXmlStreamFromZip(any())
            doReturn(bufferedReader).whenever(mockCompanyInformationParser).getCsvStreamFromZip(any())
            doReturn(CompanyInformationParser().readGleifCompanyDataFromBufferedReader(bufferedReader))
                .whenever(
                    mockCompanyInformationParser,
                ).readGleifCompanyDataFromBufferedReader(bufferedReader)

            doReturn(bufferedReader).whenever(mockCompanyInformationParser).getCsvStreamFromNorthDataZipFile(any())

            doThrow(ConnectException()).doReturn(true).whenever(mockBackendActuatorApi).health()

            processDataUpdates.processExternalCompanyDataIfEnabled()

            val numberOfDownloadedFiles = 2
            staticMock.verify({ File.createTempFile(any(), any()) }, times(numberOfDownloadedFiles))
            verify(mockCompanyInformationParser, times(1)).readGleifCompanyDataFromBufferedReader(any())
            verify(mockCompanyInformationParser, times(1)).readGleifRelationshipDataFromBufferedReader(any())
            verify(mockCompanyInformationParser, times(1)).readNorthDataFromBufferedReader(any())
        } finally {
            staticMock.close()
        }
    }

    @Test
    fun `waitForCommunityManager should stop on first successful health check`() {
        whenever(mockCommunityActuatorApi.health()).thenReturn(Any())
        initProcessDataUpdates()
        processDataUpdates.waitForCommunityManager()
        verify(mockCommunityActuatorApi).health()
    }

    @Test
    fun `processNorthDataFullGoldenCopyFileIfEnabled logs message when no flag or force ingest is set`() {
        // Set up logger and attach test appender
        val logger = LoggerFactory.getLogger(ProcessDataUpdates::class.java) as Logger
        val appender = TestLogAppender()
        initProcessDataUpdates()
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

        initProcessDataUpdates(flagFileGleifUpdate = missingFlag.absolutePath)

        val method = processDataUpdates.javaClass.getDeclaredMethod("processUpdates")
        method.isAccessible = true
        method.invoke(processDataUpdates)

        verify(mockGleifGoldenCopyIngestorTest, times(1)).prepareGleifDeltaFile(false)
        verify(mockGleifGoldenCopyIngestorTest, times(1)).processIsinMappingFile()
        verify(mockGleifGoldenCopyIngestorTest, times(1)).processRelationshipFile(false)
    }

    @Test
    fun `processUpdates triggers full update and deletes flag file if present`() {
        val flagFile = File.createTempFile("gleif_manual_update", ".flag")
        flagFile.writeText("trigger")

        assert(flagFile.exists())

        initProcessDataUpdates(flagFileGleifUpdate = flagFile.absolutePath)

        val logger = LoggerFactory.getLogger(ProcessDataUpdates::class.java) as Logger
        val appender = TestLogAppender()
        appender.start()
        logger.addAppender(appender)

        val method = processDataUpdates.javaClass.getDeclaredMethod("processUpdates")
        method.isAccessible = true
        method.invoke(processDataUpdates)

        verify(mockGleifGoldenCopyIngestorTest).prepareGleifDeltaFile(true)
        verify(mockGleifGoldenCopyIngestorTest).processIsinMappingFile()
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
