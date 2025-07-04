package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.api.UserService
import org.dataland.userService.openApiClient.model.EnrichedPortfolio
import org.dataland.userService.openApiClient.model.PortfolioMonitoringPatch
import org.dataland.userService.openApiClient.model.PortfolioUpload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
    private val apiAccessor = ApiAccessor()
    private val companyId: String = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

    private fun createDummyPortfolioUpload(companyIds: Set<String>): PortfolioUpload =
        PortfolioUpload(
            portfolioName = "Test Portfolio ${UUID.randomUUID()}",
            companyIds = companyIds,
            isMonitored = false,
            startingMonitoringPeriod = "",
            monitoredFrameworks = emptySet(),
        )

    private fun uploadDummyCompaniesAndDatasets(): List<StoredCompany> {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            val storedCompanyInformation =
                (1..3).map { apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany }

            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[0].companyId, DataTypeEnum.sfdr, "2023")
            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[1].companyId, DataTypeEnum.sfdr, "2023")
            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[1].companyId, DataTypeEnum.lksg, "2022")
            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[0].companyId, DataTypeEnum.sfdr, "2024")

            return storedCompanyInformation
        }
    }

    private fun getAvailableReportingPeriods(
        companyId: String,
        framework: DataTypeEnum,
        enrichedPortfolio: EnrichedPortfolio,
    ): String? {
        val matchingEntries =
            enrichedPortfolio.propertyEntries.filter {
                it.companyId == companyId
            }
        assertEquals(1, matchingEntries.size)
        return matchingEntries.first().availableReportingPeriods[framework.toString()]
    }

    @Test
    fun `test that creating and retrieving a portfolio for an existing company works as expected`() {
        val portfolio = createDummyPortfolioUpload(setOf(companyId))

        GlobalAuth.withTechnicalUser(TechnicalUser.Reader) {
            val timestampBeforeUpload = System.currentTimeMillis()
            ApiAwait.waitForSuccess { UserService.portfolioControllerApi.createPortfolio(portfolio) }
            val portfolioResponse =
                assertDoesNotThrow {
                    UserService.portfolioControllerApi.getAllPortfoliosForCurrentUser().filter {
                        it.creationTimestamp >= timestampBeforeUpload
                    }
                }
            assertEquals(1, portfolioResponse.size)
            assertEquals(portfolio.portfolioName, portfolioResponse.first().portfolioName)
        }
    }

    @Test
    fun `test that enriched portfolios are built as expected`() {
        val storedCompanies = uploadDummyCompaniesAndDatasets()
        val portfolioUpload = createDummyPortfolioUpload(storedCompanies.map { it.companyId }.toSet())

        GlobalAuth.withTechnicalUser(TechnicalUser.Reader) {
            ApiAwait.waitForSuccess { UserService.portfolioControllerApi.createPortfolio(portfolioUpload) }
            val portfolioResponse =
                assertDoesNotThrow { UserService.portfolioControllerApi.getAllPortfoliosForCurrentUser() }
            val enrichedPortfolio =
                UserService.portfolioControllerApi.getEnrichedPortfolio(
                    portfolioResponse.maxBy { it.creationTimestamp }.portfolioId,
                )

            assertEquals(portfolioUpload.portfolioName, enrichedPortfolio.portfolioName)
            assertEquals(3, enrichedPortfolio.propertyEntries.size) // 3 companies with 1 entry each

            val relevantPortfolioFrameworks = listOf(DataTypeEnum.sfdr, DataTypeEnum.lksg)
            storedCompanies.map { it.companyId }.forEach {
                assertEquals(
                    null,
                    getAvailableReportingPeriods(it, DataTypeEnum.vsme, enrichedPortfolio),
                )
            }
            relevantPortfolioFrameworks.forEach {
                assertEquals(
                    null,
                    getAvailableReportingPeriods(storedCompanies[2].companyId, it, enrichedPortfolio),
                )
            }
            assertEquals(
                "2024, 2023",
                getAvailableReportingPeriods(storedCompanies[0].companyId, DataTypeEnum.sfdr, enrichedPortfolio),
            )
            assertEquals(
                "2023",
                getAvailableReportingPeriods(storedCompanies[1].companyId, DataTypeEnum.sfdr, enrichedPortfolio),
            )
            assertEquals(
                null,
                getAvailableReportingPeriods(storedCompanies[0].companyId, DataTypeEnum.lksg, enrichedPortfolio),
            )
            // LkSG is not a major framework, so it does not appear in the enrichedPortfolio although the company has
            // data for it.
            assertEquals(
                null,
                getAvailableReportingPeriods(storedCompanies[1].companyId, DataTypeEnum.lksg, enrichedPortfolio),
            )
        }
    }

    @Test
    fun `portfolio yields expected bulk requests after activating monitoring`() {
        val dummyCompanyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val dummyReportingPeriod = "2024"
        val portfolioUpload =
            PortfolioUpload(
                portfolioName = "Test Portfolio ${UUID.randomUUID()}",
                companyIds = setOf(dummyCompanyId),
                isMonitored = false,
                startingMonitoringPeriod = null,
                monitoredFrameworks = emptySet(),
            )

        val portfolio =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                ApiAwait.waitForData { UserService.portfolioControllerApi.createPortfolio(portfolioUpload) }
            }

        assertEquals(portfolioUpload.portfolioName, portfolio.portfolioName)
        assertEquals(false, portfolio.isMonitored)
        assertEquals(emptySet<String>(), portfolio.monitoredFrameworks)
        assertEquals(null, portfolio.startingMonitoringPeriod)

        val portfolioId = portfolio.portfolioId

        val patchedPortfolio =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                ApiAwait.waitForData {
                    UserService.portfolioControllerApi.patchMonitoring(
                        portfolioId,
                        PortfolioMonitoringPatch(
                            isMonitored = true,
                            startingMonitoringPeriod = dummyReportingPeriod,
                            monitoredFrameworks = setOf("sfdr", "eutaxonomy"),
                        ),
                    )
                }
            }

        assertTrue(patchedPortfolio.isMonitored)
        assertEquals(dummyReportingPeriod, patchedPortfolio.startingMonitoringPeriod)
        assertEquals(setOf("sfdr", "eutaxonomy"), patchedPortfolio.monitoredFrameworks)

        val requests =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                ApiAwait.waitForData {
                    apiAccessor.requestControllerApi.getDataRequests(datalandCompanyId = dummyCompanyId)
                }
            }

        assertEquals(3, requests.size)
        assertTrue(requests.all { it.userId == TechnicalUser.Admin.technicalUserId })
        assertTrue(requests.all { it.reportingPeriod >= dummyReportingPeriod })
    }
}
