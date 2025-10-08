package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.model.BasicDataDimensions
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.dataland.keycloakAdapter.utils.KeycloakAdapterRequestProcessingUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@SpringBootTest(classes = [DatalandDataSourcingService::class])
class RequestCreationServiceTest
    @Autowired
    constructor(
        private val requestRepository: RequestRepository,
    ) : BaseIntegrationTest() {
        private lateinit var mockAuthentication: DatalandAuthentication
        private val mockSecurityContext = mock<SecurityContext>()
        private val premiumUserId = UUID.randomUUID().toString()
        private val mockKeycloakAdapterRequestProcessingUtils = mock<KeycloakAdapterRequestProcessingUtils>()
        private lateinit var requestCreationService: RequestCreationService
        private val testComment = "test comment"

        @BeforeEach
        fun setup() {
            resetSecurityContext(
                premiumUserId,
                setOf(DatalandRealmRole.ROLE_USER),
            )
            doReturn(true).whenever(mockKeycloakAdapterRequestProcessingUtils).userIsPremiumUser(premiumUserId)
            requestCreationService =
                RequestCreationService(
                    requestRepository = requestRepository,
                    keycloakAdapterRequestProcessingUtils = mockKeycloakAdapterRequestProcessingUtils,
                    maxRequestsForUser = 3,
                )
        }

        /**
         * Setting the security context to use the specified userId and set of roles.
         */
        private fun resetSecurityContext(
            userId: String,
            roles: Set<DatalandRealmRole>,
        ) {
            mockAuthentication =
                AuthenticationMock.mockJwtAuthentication(
                    "username",
                    userId,
                    roles,
                )
            doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
            SecurityContextHolder.setContext(mockSecurityContext)
        }

        @Test
        fun `storeRequest stores entity with correct fields`() {
            val userId = UUID.randomUUID()
            val companyId = UUID.randomUUID()
            val basicDataDimensions =
                BasicDataDimensions(
                    companyId = companyId.toString(),
                    dataType = "sfdr",
                    reportingPeriod = "2023",
                )
            val memberComment = testComment
            val requestId = requestCreationService.storeRequest(userId, basicDataDimensions, memberComment)
            val entity = requestRepository.findByIdAndFetchDataSourcingEntity(requestId)
            assertNotNull(entity)
            assertEquals(userId, entity.userId)
            assertEquals(companyId, entity.companyId)
            assertEquals("sfdr", entity.dataType)
            assertEquals("2023", entity.reportingPeriod)
            assertEquals(memberComment, entity.memberComment)
        }

        @Test
        fun `storeRequest throws QuotaExceededException for nonpremium user over quota`() {
            val userId = UUID.randomUUID()
            val companyId = UUID.randomUUID()
            val memberComment = testComment
            for (i in 1..requestCreationService.maxRequestsForUser) {
                val year = 2020 + i
                requestCreationService.storeRequest(
                    userId,
                    BasicDataDimensions(
                        companyId = companyId.toString(),
                        dataType = "sfdr",
                        reportingPeriod = year.toString(),
                    ),
                    memberComment,
                )
            }

            assertThrows<QuotaExceededException> {
                requestCreationService.storeRequest(
                    userId,
                    BasicDataDimensions(
                        companyId = companyId.toString(),
                        dataType = "sfdr",
                        reportingPeriod = "2030",
                    ),
                    memberComment,
                )
            }
        }

        @Test
        fun `storeRequest throws QuotaExceededException for premium user over quota`() {
            val userId = UUID.fromString(premiumUserId)
            val companyId = UUID.randomUUID()
            val memberComment = testComment
            for (i in 1..requestCreationService.maxRequestsForUser) {
                val year = 2020 + i
                requestCreationService.storeRequest(
                    userId,
                    BasicDataDimensions(
                        companyId = companyId.toString(),
                        dataType = "sfdr",
                        reportingPeriod = year.toString(),
                    ),
                    memberComment,
                )
            }

            assertDoesNotThrow {
                requestCreationService.storeRequest(
                    userId,
                    BasicDataDimensions(
                        companyId = companyId.toString(),
                        dataType = "sfdr",
                        reportingPeriod = "2030",
                    ),
                    memberComment,
                )
            }
        }
    }
