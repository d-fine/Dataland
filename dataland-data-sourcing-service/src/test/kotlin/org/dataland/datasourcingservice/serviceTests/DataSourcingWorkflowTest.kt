package org.dataland.datasourcingservice.serviceTests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackendutils.model.InheritedRole
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.controller.DataSourcingController
import org.dataland.datasourcingservice.controller.RequestController
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.services.DataSourcingServiceMessageSender
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID

@SpringBootTest(classes = [DatalandDataSourcingService::class], properties = ["spring.profiles.active=nodb"])
class DataSourcingWorkflowTest
    @Autowired
    constructor(
        private val dataSourcingController: DataSourcingController,
        private val requestController: RequestController,
    ) {
        @MockitoBean
        private lateinit var mockKeycloakUserService: KeycloakUserService

        @MockitoBean
        private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi

        @MockitoBean
        private lateinit var mockDataSourcingServiceMessageSender: DataSourcingServiceMessageSender

        @MockitoBean
        private lateinit var mockInheritedRolesControllerApi: InheritedRolesControllerApi

        private val adminUserId = UUID.randomUUID()

        private val mockSecurityContext = mock<SecurityContext>()
        private val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "data-admin",
                adminUserId.toString(),
                roles = setOf(DatalandRealmRole.ROLE_ADMIN, DatalandRealmRole.ROLE_UPLOADER, DatalandRealmRole.ROLE_USER),
            )

        private val userIds = List(3) { UUID.randomUUID() }

        private val companyName = "New Company"
        private val headquarters = "Location"
        private val countryCode = "DE"

        private val companyInfo =
            CompanyInformation(
                companyName = companyName,
                headquarters = headquarters,
                countryCode = countryCode,
                identifiers = emptyMap(),
            )

        private val companyId = UUID.randomUUID().toString()
        private val memberCompanyId = UUID.randomUUID().toString()
        private val basicCompanyInfo = BasicCompanyInformation(companyId, companyName, headquarters, countryCode)
        private val validationResult = CompanyIdentifierValidationResult("123LEI", basicCompanyInfo)

        private val framework = "sfdr"
        private val reportingPeriod = "2026"

        private val singleRequest = SingleRequest(companyId, framework, reportingPeriod, null)

        private val firstName = "Jane"
        private val lastName = "Doe"

        private fun generateEmail(userId: UUID): String = "$userId@example.com"

        @BeforeEach
        fun setup() {
            reset(
                mockKeycloakUserService,
                mockCompanyDataControllerApi,
                mockDataSourcingServiceMessageSender,
                mockSecurityContext,
            )

            doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
            SecurityContextHolder.setContext(mockSecurityContext)

            userIds.forEach {
                doReturn(
                    KeycloakUserInfo(
                        userId = it.toString(),
                        email = generateEmail(it),
                        firstName = firstName,
                        lastName = lastName,
                    ),
                ).whenever(mockKeycloakUserService).getUser(it.toString())

                doReturn(
                    mapOf(
                        memberCompanyId to listOf(InheritedRole.DatalandMember.name),
                    ),
                ).whenever(mockInheritedRolesControllerApi).getInheritedRoles(it.toString())
            }

            doReturn(listOf(validationResult))
                .whenever(mockCompanyDataControllerApi)
                .postCompanyValidation(listOf(companyId))

            doReturn(companyInfo).whenever(mockCompanyDataControllerApi).getCompanyInfo(companyId)
        }

        @Test
        fun `put three requests to processing then close data sourcing object`() {
            val requestIds =
                userIds.map {
                    requestController.createRequest(singleRequest, it.toString()).body!!.requestId
                }

            requestIds.forEach {
                requestController.patchRequestState(it, RequestState.Processing)
            }

            val dataSourcingId = requestController.getRequest(requestIds[0]).body!!.dataSourcingEntityId
            assertNotNull(dataSourcingId)

            val dataSourcingObject = dataSourcingController.getDataSourcingById(dataSourcingId).body!!
            dataSourcingController.patchDataSourcingState(
                dataSourcingObject.dataSourcingId,
                DataSourcingState.Done,
            )

            val storedRequests = requestIds.map { requestController.getRequest(it).body!! }

            storedRequests.forEach { stored ->
                Assertions.assertEquals(dataSourcingId, stored.dataSourcingEntityId)
                Assertions.assertTrue(stored.id in dataSourcingObject.associatedRequestIds)
                Assertions.assertEquals(RequestState.Processed, stored.state)
            }

            Assertions.assertEquals(3, dataSourcingObject.associatedRequestIds.size)

            val results =
                dataSourcingController
                    .searchDataSourcings(
                        companyId,
                        framework,
                        reportingPeriod,
                        chunkSize = 10,
                        chunkIndex = 0,
                    ).body!!
            Assertions.assertEquals(1, results.size)
        }
    }
