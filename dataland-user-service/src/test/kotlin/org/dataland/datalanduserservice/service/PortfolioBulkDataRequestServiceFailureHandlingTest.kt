package org.dataland.datalanduserservice.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.dataland.dataSourcingService.openApiClient.model.BulkDataRequest
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.exceptions.BulkDataRequestJobFailedException
import org.dataland.datalanduserservice.model.PortfolioRequestFailure
import org.dataland.datalanduserservice.model.SectorType
import org.dataland.datalanduserservice.model.TimeWindowThreshold
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import java.util.UUID

class PortfolioBulkDataRequestServiceFailureHandlingTest : AbstractPortfolioBulkDataRequestServiceTest() {
    private val serviceLogger = LoggerFactory.getLogger(PortfolioBulkDataRequestService::class.java) as Logger
    private val logAppender = ListAppender<ILoggingEvent>()

    @BeforeEach
    fun attachLogAppender() {
        logAppender.start()
        serviceLogger.addAppender(logAppender)
    }

    @AfterEach
    fun detachLogAppender() {
        serviceLogger.detachAppender(logAppender)
        logAppender.list.clear()
    }

    @Test
    fun `failure of one portfolio does not stop the remaining portfolios`() {
        val first = monitoredSfdrPortfolio("first")
        val failing = monitoredSfdrPortfolio("failing")
        val last = monitoredSfdrPortfolio("last")
        val inNextTimeWindowGroup = monitoredSfdrPortfolio("extended", TimeWindowThreshold.Extended)
        stubPortfolioRepo(listOf(first, failing, last, inNextTimeWindowGroup))
        failRequestPostingFor(failing)

        val exception = runJobExpectingFailure()

        assertRequestsPostedFor(first, last, inNextTimeWindowGroup)
        assertFailures(exception, failing to PortfolioRequestFailure.Stage.POST_REQUESTS)
    }

    @Test
    fun `failure in updateCompanies skips only the affected time window group`() {
        val standard = monitoredSfdrPortfolio("standard")
        val extended = monitoredSfdrPortfolio("extended", TimeWindowThreshold.Extended)
        stubPortfolioRepo(listOf(standard, extended))
        failCompanyLookupFor(standard)

        val exception = runJobExpectingFailure()

        assertRequestsPostedFor(extended)
        assertNoRequestsPostedFor(standard)
        assertFailures(exception, standard to PortfolioRequestFailure.Stage.UPDATE_COMPANIES)
    }

    @Test
    fun `logs an error summary and throws only after all portfolios were attempted`() {
        val failing = monitoredSfdrPortfolio("failing")
        val later = monitoredSfdrPortfolio("later")
        stubPortfolioRepo(listOf(failing, later))
        failRequestPostingFor(failing)

        runJobExpectingFailure()

        assertRequestsPostedFor(later)
        val errorSummary = logAppender.list.last { it.level == Level.ERROR }.formattedMessage
        assertTrue(errorSummary.contains("1 failure(s)"), errorSummary)
        assertTrue(errorSummary.contains(failing.portfolioId.toString()), errorSummary)
        assertTrue(logAppender.list.none { it.formattedMessage.contains("job completed") })
    }

    @Test
    fun `failing eutaxonomy request does not prevent sfdr request of same portfolio and is logged with context`() {
        stubCompanyApi("both-company", 0, SectorType.FINANCIALS.name)
        val portfolio =
            buildMonitoredPortfolioEntity(
                companyIds = setOf("both-company"),
                frameworks = setOf(EUTAXONOMY, SFDR),
                portfolioName = "both",
                portfolioId = UUID.randomUUID(),
                userId = "both-user",
            )
        stubPortfolioRepo(listOf(portfolio))
        whenever(
            mockRequestApi.postBulkDataRequest(
                argThat<BulkDataRequest> { dataTypes.contains(EUTAXONOMY_FINANCIALS_2026_73) },
                any(),
            ),
        ).thenThrow(IllegalStateException("data sourcing down"))

        val exception = runJobExpectingFailure()

        verify(mockRequestApi).postBulkDataRequest(argThat<BulkDataRequest> { dataTypes == setOf(SFDR) }, any())
        assertFailures(exception, portfolio to PortfolioRequestFailure.Stage.POST_REQUESTS)
        val detail = logAppender.list.first { it.level == Level.ERROR }.formattedMessage
        assertTrue(detail.contains("eutaxonomy") && detail.contains("reportingPeriod="), detail)
    }

    @Test
    fun `does not throw or log an error if all portfolios are processed successfully`() {
        stubPortfolioRepo(listOf(monitoredSfdrPortfolio("only")))

        service.createBulkDataRequestsForAllMonitoredPortfolios()

        assertTrue(logAppender.list.none { it.level == Level.ERROR })
    }

    private fun companyIdOf(portfolio: PortfolioEntity) = portfolio.companyIds.single()

    /** Creates a monitored sfdr portfolio with one financial company; ids are derived from [name]. */
    private fun monitoredSfdrPortfolio(
        name: String,
        threshold: TimeWindowThreshold = TimeWindowThreshold.Standard,
    ): PortfolioEntity {
        stubCompanyApi("$name-company", 0, SectorType.FINANCIALS.name)
        return buildMonitoredPortfolioEntity(
            companyIds = setOf("$name-company"),
            frameworks = setOf(SFDR),
            portfolioName = name,
            portfolioId = UUID.randomUUID(),
            userId = "$name-user",
            timeWindowThreshold = threshold,
        )
    }

    private fun failRequestPostingFor(portfolio: PortfolioEntity) {
        whenever(
            mockRequestApi.postBulkDataRequest(
                argThat<BulkDataRequest> { companyIdentifiers.contains(companyIdOf(portfolio)) },
                any(),
            ),
        ).thenThrow(IllegalStateException("data sourcing down"))
    }

    private fun failCompanyLookupFor(portfolio: PortfolioEntity) {
        whenever(mockCompanyDataApi.getCompanyById(companyIdOf(portfolio))).thenThrow(IllegalStateException("backend down"))
    }

    private fun runJobExpectingFailure() =
        assertThrows<BulkDataRequestJobFailedException> { service.createBulkDataRequestsForAllMonitoredPortfolios() }

    /** At least once, because a portfolio in the extended time window posts one request per reporting period. */
    private fun assertRequestsPostedFor(vararg portfolios: PortfolioEntity) =
        portfolios.forEach {
            verify(mockRequestApi, atLeastOnce()).postBulkDataRequest(
                argThat<BulkDataRequest> { companyIdentifiers == it.companyIds.toSet() && dataTypes == setOf(SFDR) },
                eq(it.userId),
            )
        }

    private fun assertNoRequestsPostedFor(portfolio: PortfolioEntity) =
        verify(mockRequestApi, never()).postBulkDataRequest(any(), eq(portfolio.userId))

    private fun assertFailures(
        exception: BulkDataRequestJobFailedException,
        vararg expected: Pair<PortfolioEntity, PortfolioRequestFailure.Stage>,
    ) = assertEquals(
        expected.map { (portfolio, stage) -> portfolio.portfolioId.toString() to stage },
        exception.failures.map { it.portfolioId to it.stage },
    )
}
