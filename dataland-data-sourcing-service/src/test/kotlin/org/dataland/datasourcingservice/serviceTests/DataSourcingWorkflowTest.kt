package org.dataland.datasourcingservice.serviceTests

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackendutils.model.InheritedRole
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.messages.NonSourceabilityCreatedEventPayload
import org.dataland.datalandmessagequeueutils.messages.QaNonSourceabilityAcceptedEventPayload
import org.dataland.datalandmessagequeueutils.messages.QaNonSourceabilityRejectedEventPayload
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.controller.DataSourcingController
import org.dataland.datasourcingservice.controller.RequestController
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.services.DataSourcingServiceListener
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
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

@SpringBootTest(classes = [DatalandDataSourcingService::class], properties = ["spring.profiles.active=nodb"])
class DataSourcingWorkflowTest
    @Autowired
    constructor(
        private val dataSourcingController: DataSourcingController,
        private val requestController: RequestController,
        private val dataSourcingServiceListener: DataSourcingServiceListener,
        private val objectMapper: ObjectMapper,
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

        @Test
        fun `verify created-event transition moves data sourcing to NonSourceableVerification`() {
            val requestId = requestController.createRequest(singleRequest, userIds.first().toString()).body!!.requestId
            requestController.patchRequestState(requestId, RequestState.Processing)

            val dataSourcingId = requestController.getRequest(requestId).body!!.dataSourcingEntityId
            assertNotNull(dataSourcingId)

            dataSourcingController.patchDataSourcingState(dataSourcingId, DataSourcingState.NonSourceableVerification)

            val updatedDataSourcing = dataSourcingController.getDataSourcingById(dataSourcingId).body!!
            Assertions.assertEquals(DataSourcingState.NonSourceableVerification, updatedDataSourcing.state)
        }

        @Test
        fun `verify auto-accepted transition moves data sourcing to NonSourceable`() {
            val requestId = requestController.createRequest(singleRequest, userIds.first().toString()).body!!.requestId
            requestController.patchRequestState(requestId, RequestState.Processing)

            val dataSourcingId = requestController.getRequest(requestId).body!!.dataSourcingEntityId
            assertNotNull(dataSourcingId)

            dataSourcingController.patchDataSourcingState(dataSourcingId, DataSourcingState.NonSourceable)

            val updatedDataSourcing = dataSourcingController.getDataSourcingById(dataSourcingId).body!!
            val updatedRequest = requestController.getRequest(requestId).body!!

            Assertions.assertEquals(DataSourcingState.NonSourceable, updatedDataSourcing.state)
            Assertions.assertEquals(RequestState.Processed, updatedRequest.state)
        }

        @Test
        fun `verify replay coverage for created-event transition`() {
            val requestId = requestController.createRequest(singleRequest, userIds.first().toString()).body!!.requestId
            requestController.patchRequestState(requestId, RequestState.Processing)

            val dataSourcingId = requestController.getRequest(requestId).body!!.dataSourcingEntityId
            assertNotNull(dataSourcingId)

            dataSourcingController.patchDataSourcingState(dataSourcingId, DataSourcingState.NonSourceableVerification)
            dataSourcingController.patchDataSourcingState(dataSourcingId, DataSourcingState.NonSourceableVerification)

            val updatedDataSourcing = dataSourcingController.getDataSourcingById(dataSourcingId).body!!
            Assertions.assertEquals(DataSourcingState.NonSourceableVerification, updatedDataSourcing.state)
        }

        @Test
        fun `verify replay coverage for auto-accepted transition`() {
            val requestId = requestController.createRequest(singleRequest, userIds.first().toString()).body!!.requestId
            requestController.patchRequestState(requestId, RequestState.Processing)

            val dataSourcingId = requestController.getRequest(requestId).body!!.dataSourcingEntityId
            assertNotNull(dataSourcingId)

            dataSourcingController.patchDataSourcingState(dataSourcingId, DataSourcingState.NonSourceable)
            dataSourcingController.patchDataSourcingState(dataSourcingId, DataSourcingState.NonSourceable)

            val updatedDataSourcing = dataSourcingController.getDataSourcingById(dataSourcingId).body!!
            val updatedRequest = requestController.getRequest(requestId).body!!

            Assertions.assertEquals(DataSourcingState.NonSourceable, updatedDataSourcing.state)
            Assertions.assertEquals(RequestState.Processed, updatedRequest.state)
        }

        @Test
        fun `verify qa accepted decision event promotes verification state to NonSourceable`() {
            val requestId = requestController.createRequest(singleRequest, userIds.first().toString()).body!!.requestId
            requestController.patchRequestState(requestId, RequestState.Processing)

            val dataSourcingId = requestController.getRequest(requestId).body!!.dataSourcingEntityId
            assertNotNull(dataSourcingId)
            val nonSourceabilityId = UUID.randomUUID()

            val createdPayload =
                objectMapper.writeValueAsString(
                    NonSourceabilityCreatedEventPayload(
                        eventId = UUID.randomUUID(),
                        nonSourceabilityId = nonSourceabilityId,
                        companyId = UUID.fromString(companyId),
                        dataType = framework,
                        reportingPeriod = reportingPeriod,
                        reason = "no source",
                        uploaderUserId = userIds.first().toString(),
                        uploadTime = ZonedDateTime.now(ZoneOffset.UTC),
                        eventPublishedTime = ZonedDateTime.now(ZoneOffset.UTC),
                    ),
                )

            dataSourcingServiceListener.processBackendNonSourceabilityEvents(
                payload = createdPayload,
                correlationId = "corr-created",
                type = MessageType.NON_SOURCEABILITY_CREATED,
            )

            val acceptedPayload =
                objectMapper.writeValueAsString(
                    QaNonSourceabilityAcceptedEventPayload(
                        eventId = UUID.randomUUID(),
                        nonSourceabilityId = nonSourceabilityId,
                        reviewerUserId = userIds.last().toString(),
                        qaComment = "accepted",
                        decisionTime = ZonedDateTime.now(ZoneOffset.UTC),
                        eventPublishedTime = ZonedDateTime.now(ZoneOffset.UTC),
                    ),
                )

            dataSourcingServiceListener.processQaNonSourceabilityDecisionEvents(
                payload = acceptedPayload,
                correlationId = "corr-accepted",
                type = MessageType.QA_NON_SOURCEABILITY_ACCEPTED,
            )

            val updatedDataSourcing = dataSourcingController.getDataSourcingById(dataSourcingId).body!!
            val updatedRequest = requestController.getRequest(requestId).body!!

            Assertions.assertEquals(DataSourcingState.NonSourceable, updatedDataSourcing.state)
            Assertions.assertEquals(RequestState.Processed, updatedRequest.state)
        }

        @Test
        fun `verify qa rejected decision event retains verification state in NonSourceableVerification`() {
            val requestId = requestController.createRequest(singleRequest, userIds.first().toString()).body!!.requestId
            requestController.patchRequestState(requestId, RequestState.Processing)

            val dataSourcingId = requestController.getRequest(requestId).body!!.dataSourcingEntityId
            assertNotNull(dataSourcingId)
            val nonSourceabilityId = UUID.randomUUID()

            val createdPayload =
                objectMapper.writeValueAsString(
                    NonSourceabilityCreatedEventPayload(
                        eventId = UUID.randomUUID(),
                        nonSourceabilityId = nonSourceabilityId,
                        companyId = UUID.fromString(companyId),
                        dataType = framework,
                        reportingPeriod = reportingPeriod,
                        reason = "no source",
                        uploaderUserId = userIds.first().toString(),
                        uploadTime = ZonedDateTime.now(ZoneOffset.UTC),
                        eventPublishedTime = ZonedDateTime.now(ZoneOffset.UTC),
                    ),
                )

            dataSourcingServiceListener.processBackendNonSourceabilityEvents(
                payload = createdPayload,
                correlationId = "corr-created",
                type = MessageType.NON_SOURCEABILITY_CREATED,
            )

            val rejectedPayload =
                objectMapper.writeValueAsString(
                    QaNonSourceabilityRejectedEventPayload(
                        eventId = UUID.randomUUID(),
                        nonSourceabilityId = nonSourceabilityId,
                        reviewerUserId = userIds.last().toString(),
                        qaComment = "not credible",
                        decisionTime = ZonedDateTime.now(ZoneOffset.UTC),
                        eventPublishedTime = ZonedDateTime.now(ZoneOffset.UTC),
                    ),
                )

            dataSourcingServiceListener.processQaNonSourceabilityDecisionEvents(
                payload = rejectedPayload,
                correlationId = "corr-rejected",
                type = MessageType.QA_NON_SOURCEABILITY_REJECTED,
            )

            val updatedDataSourcing = dataSourcingController.getDataSourcingById(dataSourcingId).body!!
            val updatedRequest = requestController.getRequest(requestId).body!!

            Assertions.assertEquals(DataSourcingState.NonSourceableVerification, updatedDataSourcing.state)
            Assertions.assertNotEquals(RequestState.Processed, updatedRequest.state)
        }
    }
