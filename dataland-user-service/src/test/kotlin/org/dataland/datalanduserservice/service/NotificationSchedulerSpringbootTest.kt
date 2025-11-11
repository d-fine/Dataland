package org.dataland.datalanduserservice.service

import jakarta.persistence.EntityManager
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [DatalandUserService::class], properties = ["spring.profiles.active=nodb"])
class NotificationSchedulerSpringbootTest
    @Autowired
    constructor(
        private val notificationEventRepository: NotificationEventRepository,
        private val entityManager: EntityManager,
        private val portfolioRepository: PortfolioRepository,
    ) {
        companion object {
            val companyId1 = UUID.randomUUID()
            val companyId2 = UUID.randomUUID()
            val companyId3 = UUID.randomUUID()
            val mockPortfolioMap1 =
                NotificationFrequency.entries.associateWith {
                    PortfolioEntity(
                        UUID.randomUUID(),
                        "portfolio1",
                        UUID.randomUUID().toString(),
                        Instant.now().toEpochMilli(),
                        Instant.now().toEpochMilli(),
                        mutableSetOf(companyId1.toString(), companyId2.toString()),
                        true,
                        setOf("sfdr", "eutaxonomy-financials"),
                        notificationFrequency = it,
                    )
                }
            val mockPortfolioMap2 =
                NotificationFrequency.entries.associateWith {
                    PortfolioEntity(
                        UUID.randomUUID(),
                        "portfolio2",
                        UUID.randomUUID().toString(),
                        Instant.now().toEpochMilli(),
                        Instant.now().toEpochMilli(),
                        mutableSetOf(companyId1.toString(), companyId2.toString()),
                        true,
                        setOf("eutaxonomy-financials"),
                        notificationFrequency = it,
                    )
                }

            // This portfolio should not send an email
            val mockPortfolioMap3 =
                NotificationFrequency.entries.associateWith {
                    PortfolioEntity(
                        UUID.randomUUID(),
                        "portfolio3",
                        UUID.randomUUID().toString(),
                        Instant.now().toEpochMilli(),
                        Instant.now().toEpochMilli(),
                        mutableSetOf(companyId3.toString()),
                        true,
                        setOf("sfdr", "eutaxonomy-financials"),
                        notificationFrequency = it,
                    )
                }

            val mockNotifications =
                listOf(
                    NotificationEventEntity(
                        UUID.randomUUID(),
                        NotificationEventType.AvailableEvent,
                        companyId1,
                        DataTypeEnum.sfdr,
                        "2025",
                    ),
                    NotificationEventEntity(
                        UUID.randomUUID(),
                        NotificationEventType.UpdatedEvent,
                        companyId1,
                        DataTypeEnum.eutaxonomyMinusFinancials,
                        "2024",
                    ),
                    NotificationEventEntity(
                        UUID.randomUUID(),
                        NotificationEventType.NonSourceableEvent,
                        companyId2,
                        DataTypeEnum.sfdr,
                        "2025",
                    ),
                )
        }

        private val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()
        private val mockCompanyApi = mock<CompanyDataControllerApi>()
        private val dataRequestSummaryEmailBuilder = DataRequestSummaryEmailBuilder(mockCloudEventMessageHandler, mockCompanyApi)
        private val notificationScheduler =
            NotificationScheduler(dataRequestSummaryEmailBuilder, portfolioRepository, entityManager)

        data class TestArgument(
            val function: () -> Unit,
            val notificationFrequency: NotificationFrequency,
            val mockPortfolioList: List<PortfolioEntity>,
            val expectedNumberOfCalls: Int,
        )

        @BeforeAll
        fun setUp() {
            notificationEventRepository.saveAll(mockNotifications)
            portfolioRepository.saveAll(mockPortfolioMap1.values)
            portfolioRepository.saveAll(mockPortfolioMap2.values)
            portfolioRepository.saveAll(mockPortfolioMap3.values)
        }

        @BeforeEach
        fun setUpBeforeEach() {
            reset(mockCloudEventMessageHandler)
        }

        fun eMailSchedulerTwoDimensionalParameters(): Stream<TestArgument> =
            Stream.of(
                TestArgument(
                    { notificationScheduler.scheduledDailyEmailSending() },
                    NotificationFrequency.Daily,
                    listOf(
                        mockPortfolioMap1[NotificationFrequency.Daily]!!,
                        mockPortfolioMap2[NotificationFrequency.Daily]!!,
                        mockPortfolioMap3[NotificationFrequency.Daily]!!,
                    ),
                    3,
                ),
                TestArgument(
                    { notificationScheduler.scheduledWeeklyEmailSending() },
                    NotificationFrequency.Weekly,
                    listOf(
                        mockPortfolioMap1[NotificationFrequency.Weekly]!!,
                        mockPortfolioMap2[NotificationFrequency.Weekly]!!,
                        mockPortfolioMap3[NotificationFrequency.Weekly]!!,
                    ),
                    1,
                ),
                TestArgument(
                    { notificationScheduler.scheduledMonthlyEmailSending() },
                    NotificationFrequency.Monthly,
                    listOf(
                        mockPortfolioMap1[NotificationFrequency.Monthly]!!,
                        mockPortfolioMap2[NotificationFrequency.Monthly]!!,
                        mockPortfolioMap3[NotificationFrequency.Monthly]!!,
                    ),
                    1,
                ),
            )

        @ParameterizedTest
        @MethodSource("eMailSchedulerTwoDimensionalParameters")
        fun `test scheduledWeeklyEmailSending does create the expected messages`(ta: TestArgument) {
            val mockCompanyInformation1 = CompanyInformation(companyName = "company1", "", mapOf(), "")
            val mockStoredCompany1 = StoredCompany("", mockCompanyInformation1, listOf())
            val mockCompanyInformation2 = CompanyInformation(companyName = "company2", "", mapOf(), "")
            val mockStoredCompany2 = StoredCompany("", mockCompanyInformation2, listOf())
            val mockCompanyInformation3 = CompanyInformation(companyName = "company3", "", mapOf(), "")
            val mockStoredCompany3 = StoredCompany("", mockCompanyInformation3, listOf())
            whenever(mockCompanyApi.getCompanyById(eq(companyId1.toString()))).thenReturn(mockStoredCompany1)
            whenever(mockCompanyApi.getCompanyById(eq(companyId2.toString()))).thenReturn(mockStoredCompany2)
            whenever(mockCompanyApi.getCompanyById(eq(companyId3.toString()))).thenReturn(mockStoredCompany3)

            ta.function()

            val bodyCaptor = argumentCaptor<String>()
            verify(mockCloudEventMessageHandler, times(3)).buildCEMessageAndSendToQueue(
                bodyCaptor.capture(),
                any<String>(),
                any<String>(),
                any<String>(),
                any<String>(),
            )

            val capturedBodies = bodyCaptor.allValues
            capturedBodies.forEach { assert(ta.notificationFrequency.name in it) }

            val (email1, email2) =
                if (capturedBodies[0].length > capturedBodies[1].length) {
                    capturedBodies[0] to capturedBodies[1]
                } else {
                    capturedBodies[1] to capturedBodies[0]
                }

            val emailKeywords1 = listOf("portfolio1", "SFDR", "EU Taxonomy for financial companies", "2024", "company1", "company2")
            val emailKeywordsBlacklist1 = listOf("portfolio2", "portfolio3", "company3", "\"nonSourceableData\":[]")
            val emailKeywords2 =
                listOf("portfolio2", "EU Taxonomy for financial companies", "2024", "\"nonSourceableData\":[]", "company1")
            val emailKeywordsBlacklist2 = listOf("portfolio1", "portfolio3", "SFDR", "company2", "company3")

            emailKeywords1.forEach { assertTrue(it in email1, "Expected '$it' to be in '$email1'") }
            emailKeywordsBlacklist1.forEach { assertTrue(it !in email1, "Expected '$it' to not be in '$email1'") }
            emailKeywords2.forEach { assertTrue(it in email2, "Expected '$it' to be in '$email2'") }
            emailKeywordsBlacklist2.forEach { assertTrue(it !in email2, "Expected '$it' to not be in '$email1'") }
        }
    }
