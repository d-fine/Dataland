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
import org.mockito.Mockito
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

class DataRequestUpdaterTest {
    private lateinit var dataRequestUpdater: DataRequestUpdater
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private val dataRequestedAnsweredEmailMessageSender =
        Mockito.mock(DataRequestedAnsweredEmailMessageSender::class.java)
    private val dataRequestRepository = Mockito.mock(DataRequestRepository::class.java)
    private val metaDataControllerApi = Mockito.mock(MetaDataControllerApi::class.java)
    private val objectMapper = Mockito.mock(ObjectMapper::class.java)
    private val companyDataControllerApi = Mockito.mock(CompanyDataControllerApi::class.java)
    private val messageUtils = MessageQueueUtils()
    private val jsonString = "jsonString"
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

    @BeforeEach
    fun setupDataRequestUpdater() {
        Mockito.`when`(objectMapper.readValue(jsonString, QaCompletedMessage::class.java))
            .thenReturn(QaCompletedMessage(metaData.dataId, QaStatus.Accepted))
        Mockito.`when`(metaDataControllerApi.getDataMetaInfo(metaData.dataId))
            .thenReturn(metaData)
        Mockito.`when`(
            dataRequestRepository
                .searchDataRequestEntity(
                    searchFilter = GetDataRequestsSearchFilter(
                        metaData.dataType.value, "", RequestStatus.Open, metaData.reportingPeriod, metaData.companyId,
                    ),
                ),
        ).thenReturn(dataRequestEntities)
        Mockito.doNothing().`when`(dataRequestRepository).updateDataRequestEntitiesFromOpenToAnswered(
            metaData.companyId, metaData.reportingPeriod, metaData.dataType.value,
        )
        dataRequestEntities.forEach {
            Mockito.doNothing().`when`(dataRequestedAnsweredEmailMessageSender)
                .sendDataRequestedAnsweredEmail(it)
        }
        Mockito.`when`(companyDataControllerApi.getCompanyInfo(metaData.companyId)).thenReturn(
            CompanyInformation(companyName, "", emptyMap(), ""),
        )
        dataRequestUpdater = DataRequestUpdater(
            messageUtils = messageUtils,
            metaDataControllerApi = metaDataControllerApi,
            companyDataControllerApi = companyDataControllerApi,
            objectMapper = objectMapper,
            dataRequestRepository = dataRequestRepository,
            dataRequestedAnsweredEmailMessageSender = dataRequestedAnsweredEmailMessageSender,
        )
    }

    @BeforeEach
    fun setupSecurityMock() {
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "user@requests.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        Mockito.`when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that an request answered email is send when a request status is updated`() {
        dataRequestUpdater.changeRequestStatusAfterUpload(jsonString, MessageType.QaCompleted)
        dataRequestEntities.forEach {
            Mockito.verify(dataRequestedAnsweredEmailMessageSender)
                .sendDataRequestedAnsweredEmail(it, companyName)
        }
    }
}
