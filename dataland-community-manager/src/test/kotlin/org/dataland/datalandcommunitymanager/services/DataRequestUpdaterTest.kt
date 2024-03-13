package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestedAnsweredEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

class DataRequestUpdaterTest {
    private lateinit var dataRequestUpdater: DataRequestUpdater
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private val dataRequestedAnsweredEmailMessageSender =
        mock(DataRequestedAnsweredEmailMessageSender::class.java)
    private val dataRequestRepository = mock(DataRequestRepository::class.java)
    private val metaDataControllerApi = mock(MetaDataControllerApi::class.java)
    private val objectMapper = mock(ObjectMapper::class.java)
    private val companyDataControllerApi = mock(CompanyDataControllerApi::class.java)
    private val messageUtils = MessageQueueUtils()
    private val jsonString = "jsonString"
    private val correlationId = UUID.randomUUID().toString()
    private val dataRequestEntities: List<DataRequestEntity> = listOf(
        DataRequestEntity(
            userId = "",
            dataType = "",
            reportingPeriod = "",
            creationTimestamp = 0,
            datalandCompanyId = "",
        ),
        DataRequestEntity(
            userId = "dummyId",
            dataType = "dummyDataType",
            reportingPeriod = "dummyPeriod",
            creationTimestamp = 123456,
            datalandCompanyId = "dummyCompanyId",
        ),
    )
    private val companyName = "TestCompany"
    private val metaData = DataMetaInformation(
        dataId = UUID.randomUUID().toString(),
        companyId = "companyId",
        dataType = DataTypeEnum.p2p,
        uploadTime = 0,
        reportingPeriod = "",
        currentlyActive = false,
        qaStatus = org.dataland.datalandbackend.openApiClient.model.QaStatus.accepted,
    )
    private fun mockParameterObjects() {
        `when`(objectMapper.readValue(jsonString, QaCompletedMessage::class.java))
            .thenReturn(QaCompletedMessage(metaData.dataId, QaStatus.Accepted))
        `when`(metaDataControllerApi.getDataMetaInfo(metaData.dataId))
            .thenReturn(metaData)
        `when`(
            dataRequestRepository.searchDataRequestEntity(
                searchFilter = GetDataRequestsSearchFilter(
                    metaData.dataType.value, "", RequestStatus.Open, metaData.reportingPeriod, metaData.companyId,
                ),
            ),
        ).thenReturn(dataRequestEntities)
        doNothing().`when`(dataRequestRepository).updateDataRequestEntitiesFromOpenToAnswered(
            metaData.companyId, metaData.reportingPeriod, metaData.dataType.value,
        )
        dataRequestEntities.forEach {
            doNothing().`when`(dataRequestedAnsweredEmailMessageSender)
                .sendDataRequestedAnsweredEmail(it, correlationId)
        }
        `when`(companyDataControllerApi.getCompanyInfo(metaData.companyId)).thenReturn(
            CompanyInformation(companyName, "", emptyMap(), ""),
        )
    }

    @BeforeEach
    fun setupDataRequestUpdater() {
        mockParameterObjects()
        dataRequestUpdater = DataRequestUpdater(
            messageUtils = messageUtils,
            metaDataControllerApi = metaDataControllerApi,
            objectMapper = objectMapper,
            dataRequestRepository = dataRequestRepository,
            dataRequestedAnsweredEmailMessageSender = dataRequestedAnsweredEmailMessageSender,
        )
    }

    @BeforeEach
    fun setupSecurityMock() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "user@requests.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that an request answered email is send when a request status is updated`() {
        dataRequestUpdater.changeRequestStatusAfterUpload(jsonString, MessageType.QaCompleted, correlationId)
        dataRequestEntities.forEach {
            verify(dataRequestedAnsweredEmailMessageSender)
                .sendDataRequestedAnsweredEmail(it, correlationId)
        }
    }
}
