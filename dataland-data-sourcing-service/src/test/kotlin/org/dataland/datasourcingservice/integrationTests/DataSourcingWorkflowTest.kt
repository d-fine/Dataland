package org.dataland.datasourcingservice.integrationTests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.controller.DataSourcingController
import org.dataland.datasourcingservice.controller.RequestController
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.kotlin.any
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
        private lateinit var mockRequestQueryManager: RequestQueryManager

        private val mockSecurityContext = mock<SecurityContext>()
        private lateinit var mockAuthentication: DatalandAuthentication

        @Test
        fun `put three requests to processing then close data sourcing object`() {
            reset(mockKeycloakUserService)
            mockAuthentication =
                AuthenticationMock.mockJwtAuthentication(
                    "data-admin",
                    "user-id",
                    roles = setOf(DatalandRealmRole.ROLE_ADMIN, DatalandRealmRole.ROLE_UPLOADER),
                )
            doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
            SecurityContextHolder.setContext(mockSecurityContext)

            val companyId = UUID.randomUUID().toString()
            val companyInfo = BasicCompanyInformation(companyId, "New Company", "Location", "DE")
            val validationResult = CompanyIdentifierValidationResult("123LEI", companyInfo)
            whenever(mockCompanyDataControllerApi.postCompanyValidation(any()))
                .thenReturn(listOf(validationResult))
            whenever(mockRequestQueryManager.transformRequestEntityToExtendedStoredRequest(any<RequestEntity>())).thenAnswer { invocation ->
                (invocation.arguments[0] as RequestEntity).toExtendedStoredRequest("New Company", null)
            }

            val requests = List(3) { SingleRequest(companyId, "sfdr", "2026", null) }
            val requestIds =
                requests.map {
                    requestController.createRequest(it, UUID.randomUUID().toString()).body!!.requestId
                }

            requestIds.forEach {
                requestController.patchRequestState(it, RequestState.Processing)
            }

            val dataSourcingId = requestController.getRequest(requestIds[0]).body!!.dataSourcingEntityId
            assertNotNull(dataSourcingId)

            val dataSourcingObject = dataSourcingController.getDataSourcingById(dataSourcingId).body!!
            dataSourcingController.patchDataSourcingState(dataSourcingObject.dataSourcingId, DataSourcingState.Done)

            val storedRequests = requestIds.map { requestController.getRequest(it).body!! }

            storedRequests.forEach { stored ->
                Assertions.assertEquals(dataSourcingId, stored.dataSourcingEntityId)
                Assertions.assertTrue(stored.id in dataSourcingObject.associatedRequestIds)
                Assertions.assertEquals(RequestState.Processed, stored.state)
            }

            Assertions.assertEquals(3, dataSourcingObject.associatedRequestIds.size)

            val results = dataSourcingController.searchDataSourcings(companyId, "sfdr", "2026", chunkSize = 10, chunkIndex = 0).body!!
            Assertions.assertEquals(1, results.size)
        }
    }
