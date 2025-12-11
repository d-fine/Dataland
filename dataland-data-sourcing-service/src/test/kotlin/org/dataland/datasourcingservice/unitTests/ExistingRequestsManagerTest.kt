package org.dataland.datasourcingservice.unitTests

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingServiceMessageSender
import org.dataland.datasourcingservice.services.ExistingRequestsManager
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class ExistingRequestsManagerTest {
    private val mockRequestRepository = mock<RequestRepository>()
    private val mockDataSourcingManager = mock<DataSourcingManager>()
    private val mockDataRevisionRepository = mock<DataRevisionRepository>()
    private val mockDataSourcingServiceMessageSender = mock<DataSourcingServiceMessageSender>()
    private val mockRequestQueryManager = mock<RequestQueryManager>()

    private val existingRequestsManager =
        ExistingRequestsManager(
            mockRequestRepository,
            mockDataSourcingManager,
            mockDataRevisionRepository,
            mockDataSourcingServiceMessageSender,
            mockRequestQueryManager,
        )

    private val dataRequestIdForSfdr = UUID.randomUUID()
    private val companyId = UUID.randomUUID()
    private val reportingPeriod = "2025"
    private val dataType = "sfdr"
    private val userId = UUID.randomUUID()

    private val requestEntitySfdr =
        RequestEntity(
            id = dataRequestIdForSfdr,
            companyId = companyId,
            reportingPeriod = reportingPeriod,
            dataType = dataType,
            userId = userId,
            creationTimestamp = 1000000000,
            memberComment = null,
            adminComment = null,
            lastModifiedDate = 1000000000,
            requestPriority = RequestPriority.High,
            state = RequestState.Open,
            dataSourcingEntity = null,
        )

    private val dataSourcingEntity =
        DataSourcingEntity(
            companyId = companyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
        )

    @BeforeEach
    fun setup() {
        reset(
            mockRequestRepository,
            mockDataSourcingManager,
            mockDataRevisionRepository,
            mockDataSourcingServiceMessageSender,
            mockRequestQueryManager,
        )

        doAnswer { invocation ->
            (invocation.arguments[0] as RequestEntity).toExtendedStoredRequest("Company Name", null)
        }.whenever(mockRequestQueryManager).transformRequestEntityToExtendedStoredRequest(any<RequestEntity>())

        doReturn(requestEntitySfdr).whenever(mockRequestRepository).findByIdAndFetchDataSourcingEntity(dataRequestIdForSfdr)

        doAnswer { invocation -> invocation.arguments[0] }.whenever(mockRequestRepository).save(any())

        doReturn(dataSourcingEntity).whenever(mockDataSourcingManager).useExistingOrCreateDataSourcingAndAddRequest(requestEntitySfdr)
    }

    @ParameterizedTest
    @EnumSource(RequestState::class)
    fun `verify that a request is appended to its sourcing object and a message is sent only when request set to Processing`(
        requestState: RequestState,
    ) {
        existingRequestsManager.patchRequestState(dataRequestIdForSfdr, requestState, null)

        if (requestState == RequestState.Processing) {
            verify(mockDataSourcingManager, times(1)).useExistingOrCreateDataSourcingAndAddRequest(any())
            verify(mockDataSourcingServiceMessageSender, times(1))
                .sendMessageToAccountingServiceOnRequestProcessing(
                    dataSourcingEntity = dataSourcingEntity,
                    requestEntity = requestEntitySfdr,
                )
        } else {
            verify(mockDataSourcingManager, never()).useExistingOrCreateDataSourcingAndAddRequest(any())
            verify(mockDataSourcingServiceMessageSender, never())
                .sendMessageToAccountingServiceOnRequestProcessing(
                    dataSourcingEntity = anyOrNull(),
                    requestEntity = anyOrNull(),
                )
        }
    }

    @ParameterizedTest
    @EnumSource(DataTypeEnum::class)
    fun `verify that a message to accounting is sent only for a request not for nuclear and gas`(dataType: DataTypeEnum) {
        val requestIdStateProcessing = UUID.randomUUID()
        val requestEntityVaryingDataType =
            RequestEntity(
                id = requestIdStateProcessing,
                companyId = companyId,
                reportingPeriod = reportingPeriod,
                dataType = dataType.name,
                userId = userId,
                creationTimestamp = 1000000000,
                memberComment = null,
                adminComment = null,
                lastModifiedDate = 1000000000,
                requestPriority = RequestPriority.High,
                state = RequestState.Open,
                dataSourcingEntity = null,
            )
        doReturn(requestEntityVaryingDataType)
            .whenever(mockRequestRepository)
            .findByIdAndFetchDataSourcingEntity(requestIdStateProcessing)
        doReturn(dataSourcingEntity)
            .whenever(mockDataSourcingManager)
            .useExistingOrCreateDataSourcingAndAddRequest(requestEntityVaryingDataType)

        existingRequestsManager.patchRequestState(requestIdStateProcessing, RequestState.Processing, null)

        if (dataType == DataTypeEnum.nuclearMinusAndMinusGas) {
            verify(mockDataSourcingServiceMessageSender, never())
                .sendMessageToAccountingServiceOnRequestProcessing(
                    dataSourcingEntity = anyOrNull(),
                    requestEntity = anyOrNull(),
                )
        } else {
            verify(mockDataSourcingServiceMessageSender, times(1))
                .sendMessageToAccountingServiceOnRequestProcessing(
                    dataSourcingEntity = dataSourcingEntity,
                    requestEntity = requestEntityVaryingDataType,
                )
        }
    }
}
