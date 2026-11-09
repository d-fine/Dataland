package org.dataland.e2etests.tests

import org.dataland.dataSourcingService.openApiClient.model.RequestSearchFilterString
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.api.UserService
import org.dataland.userService.openApiClient.model.EnrichedPortfolio
import org.dataland.userService.openApiClient.model.NotificationFrequency
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
            identifiers = companyIds,
            isMonitored = false,
            monitoredFrameworks = emptySet(),
            notificationFrequency = NotificationFrequency.NoNotifications,
            sharedUserIds = emptySet(),
            timeWindowThreshold = null,
        )

    private fun uploadDummyCompaniesAndDatasets(): List<StoredCompany> {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            val storedCompanyInformation =
                (1..4).map { apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany }

            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[0].companyId, DataTypeEnum.sfdr, "2023")
            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[0].companyId, DataTypeEnum.sfdr, "2024")
            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[1].companyId, DataTypeEnum.sfdr, "2022")
            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[1].companyId, DataTypeEnum.lksg, "2023")
            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[1].companyId, DataTypeEnum.pcaf, "2024")
            apiAccessor
                .uploadDummyFrameworkDataset(storedCompanyInformation[2].companyId, DataTypeEnum.eutaxonomyMinusNonMinusFinancials, "2022")

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
        return matchingEntries.first().availableReportingPeriods[framework.value]
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
            assertEquals(4, enrichedPortfolio.propertyEntries.size)

            val relevantPortfolioFrameworks =
                listOf(DataTypeEnum.sfdr, DataTypeEnum.lksg, DataTypeEnum.pcaf, DataTypeEnum.eutaxonomyMinusNonMinusFinancials)
            relevantPortfolioFrameworks.forEach {
                assertEquals(
                    null,
                    getAvailableReportingPeriods(storedCompanies[3].companyId, it, enrichedPortfolio),
                )
            }
            listOf(
                Triple(0, DataTypeEnum.sfdr, "2024, 2023"),
                // SFDR and PCAF share data points, so if SFDR was uploaded, PCAF should be available for that year too and vice versa
                Triple(0, DataTypeEnum.pcaf, "2024, 2023"),
                Triple(0, DataTypeEnum.lksg, null),
                Triple(1, DataTypeEnum.sfdr, "2024, 2022"),
                Triple(1, DataTypeEnum.pcaf, "2024, 2022"),
                // LkSG is not a major framework, so it does not appear in the enrichedPortfolio although the company has
                // data for it.
                Triple(1, DataTypeEnum.lksg, null),
                Triple(2, DataTypeEnum.eutaxonomyMinusNonMinusFinancials, "2022"),
                Triple(2, DataTypeEnum.pcaf, null),
            ).forEach { (companyIndex, framework, expectedPeriods) ->
                assertEquals(
                    expectedPeriods,
                    getAvailableReportingPeriods(storedCompanies[companyIndex].companyId, framework, enrichedPortfolio),
                )
            }
        }
    }

    @Test
    fun `portfolio yields no requests after activating monitoring`() {
        val dummyCompanyId = apiAccessor.uploadOneCompanyWithRandomIdentifierFYEAndReportingShift().actualStoredCompany.companyId
        val portfolioUpload =
            PortfolioUpload(
                portfolioName = "Test Portfolio ${UUID.randomUUID()}",
                identifiers = setOf(dummyCompanyId),
                isMonitored = false,
                monitoredFrameworks = emptySet(),
                notificationFrequency = NotificationFrequency.NoNotifications,
                sharedUserIds = emptySet(),
                timeWindowThreshold = null,
            )

        val portfolio =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                ApiAwait.waitForData { UserService.portfolioControllerApi.createPortfolio(portfolioUpload) }
            }

        assertEquals(portfolioUpload.portfolioName, portfolio.portfolioName)
        assertEquals(false, portfolio.isMonitored)
        assertEquals(emptySet<String>(), portfolio.monitoredFrameworks)

        val portfolioId = portfolio.portfolioId

        val patchedPortfolio =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                ApiAwait.waitForData {
                    UserService.portfolioControllerApi.patchMonitoring(
                        portfolioId,
                        PortfolioMonitoringPatch(
                            isMonitored = true,
                            monitoredFrameworks = setOf("sfdr", "eutaxonomy"),
                            notificationFrequency = NotificationFrequency.NoNotifications,
                            timeWindowThreshold = PortfolioMonitoringPatch.TimeWindowThreshold.Standard,
                        ),
                    )
                }
            }

        assertTrue(patchedPortfolio.isMonitored)
        assertEquals(setOf("sfdr", "eutaxonomy"), patchedPortfolio.monitoredFrameworks)

        val requests =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                ApiAwait.waitForData {
                    apiAccessor.enhancedRequestControllerApi.postRequestSearch(
                        RequestSearchFilterString(
                            companyId = dummyCompanyId,
                        ),
                    )
                }
            }

        assertEquals(0, requests.size)
        assertTrue(requests.all { it.userId == TechnicalUser.Admin.technicalUserId })
    }
}
