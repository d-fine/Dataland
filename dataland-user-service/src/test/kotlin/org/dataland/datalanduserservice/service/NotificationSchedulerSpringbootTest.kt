package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalanduserservice.DatalandUserService
import org.dataland.datalanduserservice.entity.NotificationEventEntity
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.enums.NotificationEventType
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.repository.NotificationEventRepository
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.UUID
import java.util.stream.Stream
import kotlin.reflect.KFunction

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [DatalandUserService::class], properties = ["spring.profiles.active=containerized-db"])
class NotificationSchedulerSpringbootTest
    @Autowired
    constructor(
        private val notificationEventRepository: NotificationEventRepository,
        private val portfolioRepository: PortfolioRepository,
    ) : BaseIntegrationTest() {
        private val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()
        private val mockCompanyApi = mock<CompanyDataControllerApi>()
        private val portfolioUpdateSummaryEmailBuilder = PortfolioUpdateSummaryEmailBuilder(mockCloudEventMessageHandler, mockCompanyApi)
        private val notificationScheduler =
            NotificationScheduler(portfolioUpdateSummaryEmailBuilder, portfolioRepository, notificationEventRepository)

        companion object {
            val companyIdsfdrAndEuTaxo = UUID.randomUUID()
            val companyIdOnlySfdr = UUID.randomUUID()
            val companyIdNoNotifications = UUID.randomUUID()
            const val PORTFOLIO_ALL_FRAMEWORKS = "Portfolioname 1"
            const val PORTFOLIO_ONLY_EUTAXO = "Taxonomy only"
            const val PORTFOLIO_ONLY_COMPANY3 = "NoNotificationExpected"
            const val NEW_EUTAXO_FINANCIALS = "EU Taxonomy for financial companies 2026/73"

            val ALL_FRAMEWORKS_EMAIL_KEYWORDS =
                listOf(
                    PORTFOLIO_ALL_FRAMEWORKS,
                    "SFDR",
                    "EU Taxonomy for financial companies",
                    NEW_EUTAXO_FINANCIALS,
                    "2024",
                    "2025",
                    "company1",
                    "company2",
                )
            val ALL_FRAMEWORKS_EMAIL_BLACKLIST =
                listOf(PORTFOLIO_ONLY_EUTAXO, PORTFOLIO_ONLY_COMPANY3, "company3", "\"nonSourceableData\":[]")
            val ONLY_EUTAXO_EMAIL_KEYWORDS =
                listOf(
                    PORTFOLIO_ONLY_EUTAXO,
                    "EU Taxonomy for financial companies",
                    NEW_EUTAXO_FINANCIALS,
                    "2024",
                    "\"nonSourceableData\":[]",
                    "company1",
                )
            val ONLY_EUTAXO_EMAIL_BLACKLIST =
                listOf(PORTFOLIO_ALL_FRAMEWORKS, PORTFOLIO_ONLY_COMPANY3, "SFDR", "2025", "company2", "company3")

            private fun createNotificationFrequencyToPortfolioMap(
                portfolioName: String,
                companyIds: Set<String>,
                frequencies: Set<NotificationFrequency> = NotificationFrequency.entries.toSet(),
                frameworks: Set<String> = setOf("sfdr", "eutaxonomy"),
            ): Map<NotificationFrequency, PortfolioEntity> =
                frequencies.associateWith {
                    PortfolioEntity(
                        UUID.randomUUID(),
                        portfolioName,
                        UUID.randomUUID().toString(),
                        Instant.now().toEpochMilli(),
                        Instant.now().toEpochMilli(),
                        companyIds.toMutableSet(),
                        true,
                        frameworks,
                        notificationFrequency = it,
                        sharedUserIds = emptySet(),
                    )
                }

            val mockPortfolios =
                listOf(
                    createNotificationFrequencyToPortfolioMap(
                        PORTFOLIO_ALL_FRAMEWORKS,
                        setOf(
                            companyIdsfdrAndEuTaxo.toString(),
                            companyIdOnlySfdr
                                .toString(),
                        ),
                    ),
                    createNotificationFrequencyToPortfolioMap(
                        PORTFOLIO_ONLY_EUTAXO,
                        setOf(companyIdsfdrAndEuTaxo.toString(), companyIdOnlySfdr.toString()),
                        frameworks = setOf("eutaxonomy"),
                    ),
                    // This portfolio should not send an email
                    createNotificationFrequencyToPortfolioMap(PORTFOLIO_ONLY_COMPANY3, setOf(companyIdNoNotifications.toString())),
                )

            val mockNotifications =
                listOf(
                    NotificationEventEntity(
                        UUID.randomUUID(),
                        NotificationEventType.AvailableEvent,
                        companyIdsfdrAndEuTaxo,
                        DataTypeEnum.sfdr,
                        "2025",
                    ),
                    NotificationEventEntity(
                        UUID.randomUUID(),
                        NotificationEventType.UpdatedEvent,
                        companyIdsfdrAndEuTaxo,
                        DataTypeEnum.eutaxonomyMinusFinancials,
                        "2024",
                    ),
                    NotificationEventEntity(
                        UUID.randomUUID(),
                        NotificationEventType.AvailableEvent,
                        companyIdsfdrAndEuTaxo,
                        DataTypeEnum.eutaxonomyMinusFinancialsMinus2026Minus73,
                        "2024",
                    ),
                    NotificationEventEntity(
                        UUID.randomUUID(),
                        NotificationEventType.NonSourceableEvent,
                        companyIdOnlySfdr,
                        DataTypeEnum.sfdr,
                        "2025",
                    ),
                )
        }

        data class TestArgument(
            val function: KFunction<Unit>,
            val notificationFrequency: NotificationFrequency,
            val mockPortfolioList: List<PortfolioEntity>,
        )

        @BeforeAll
        fun setUp() {
            notificationEventRepository.saveAll(mockNotifications)
            mockPortfolios.forEach { portfolioRepository.saveAll(it.values) }
        }

        @BeforeEach
        fun setUpBeforeEach() {
            reset(mockCloudEventMessageHandler)
            mockCompany(companyIdsfdrAndEuTaxo, "company1")
            mockCompany(companyIdOnlySfdr, "company2")
            mockCompany(companyIdNoNotifications, "company3")
        }

        fun eMailSchedulerParameters(): Stream<TestArgument> =
            Stream.of(
                TestArgument(
                    NotificationScheduler::scheduledDailyEmailSending,
                    NotificationFrequency.Daily,
                    mockPortfolios.map { it.getValue(NotificationFrequency.Daily) },
                ),
                TestArgument(
                    NotificationScheduler::scheduledWeeklyEmailSending,
                    NotificationFrequency.Weekly,
                    mockPortfolios.map { it.getValue(NotificationFrequency.Weekly) },
                ),
                TestArgument(
                    NotificationScheduler::scheduledMonthlyEmailSending,
                    NotificationFrequency.Monthly,
                    mockPortfolios.map { it.getValue(NotificationFrequency.Monthly) },
                ),
            )

        @ParameterizedTest
        @MethodSource("eMailSchedulerParameters")
        fun `test scheduled email sending creates the expected messages`(ta: TestArgument) {
            ta.function.call(notificationScheduler)

            val bodies = captureSentEmailBodies()
            bodies.forEach {
                assertTrue(
                    it.contains(ta.notificationFrequency.name),
                    "Expected email body to contain notification frequency '${ta.notificationFrequency.name}'. Actual body: $it",
                )
            }

            val (emailWithOnlyEuTaxo, emailWithAllFrameworks) = bodies.sortedBy { it.length }

            assertContainsAll(emailWithAllFrameworks, ALL_FRAMEWORKS_EMAIL_KEYWORDS)
            assertContainsNone(emailWithAllFrameworks, ALL_FRAMEWORKS_EMAIL_BLACKLIST)
            assertContainsAll(emailWithOnlyEuTaxo, ONLY_EUTAXO_EMAIL_KEYWORDS)
            assertContainsNone(emailWithOnlyEuTaxo, ONLY_EUTAXO_EMAIL_BLACKLIST)
        }

        private fun mockCompany(
            id: UUID,
            name: String,
        ) {
            whenever(mockCompanyApi.getCompanyById(eq(id.toString()))).thenReturn(
                StoredCompany("", CompanyInformation(companyName = name, "", mapOf(), ""), listOf()),
            )
        }

        private fun captureSentEmailBodies(): List<String> {
            val bodyCaptor = argumentCaptor<String>()
            verify(mockCloudEventMessageHandler, times(2)).buildCEMessageAndSendToQueue(
                bodyCaptor.capture(),
                any<String>(),
                any<String>(),
                any<String>(),
                any<String>(),
            )
            return bodyCaptor.allValues
        }

        private fun assertContainsAll(
            body: String,
            keywords: List<String>,
        ) = keywords.forEach { assertTrue(it in body, "Expected '$it' to be in '$body'") }

        private fun assertContainsNone(
            body: String,
            keywords: List<String>,
        ) = keywords.forEach { assertTrue(it !in body, "Expected '$it' to not be in '$body'") }
    }
