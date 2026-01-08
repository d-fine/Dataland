package org.dataland.datasourcingservice.unitTests

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingServiceMessageSender
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.dataland.datasourcingservice.services.ExistingRequestsManager
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class ExistingRequestsManagerTest {
    private val mockRequestRepository = mock<RequestRepository>()
    private val mockDataSourcingRepository = mock<DataSourcingRepository>()
    private val mockDataSourcingManager = mock<DataSourcingManager>()
    private val mockDataSourcingValidator = mock<DataSourcingValidator>()
    private val mockDataRevisionRepository = mock<DataRevisionRepository>()
    private val mockDataSourcingServiceMessageSender = mock<DataSourcingServiceMessageSender>()
    private val mockRequestQueryManager = mock<RequestQueryManager>()
    private val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()

    private lateinit var dataRequestId: UUID
    private lateinit var companyId: UUID
    private var reportingPeriod = "2025"
    private var dataType = "sfdr"
    private lateinit var userId: UUID
    private lateinit var requestEntitySfdr: RequestEntity
    private lateinit var dataSourcingEntity: DataSourcingEntity

    private val existingRequestsManager =
        ExistingRequestsManager(
            mockRequestRepository,
            mockDataSourcingManager,
            mockDataRevisionRepository,
            mockDataSourcingServiceMessageSender,
            mockRequestQueryManager,
        )

    private val testDataSourcingManager =
        DataSourcingManager(
            mockDataSourcingRepository,
            mockDataRevisionRepository,
            mockDataSourcingValidator,
            mockCloudEventMessageHandler,
        )
    private val dataRequestIdForSfdr = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        dataRequestId = UUID.randomUUID()
        companyId = UUID.randomUUID()
        userId = UUID.randomUUID()

        requestEntitySfdr =
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

        dataSourcingEntity =
            DataSourcingEntity(
                companyId = companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
            )

        reset(
            mockRequestRepository,
            mockDataSourcingManager,
            mockDataRevisionRepository,
            mockDataSourcingServiceMessageSender,
            mockRequestQueryManager,
            mockCloudEventMessageHandler,
        )

        doAnswer { invocation ->
            (invocation.arguments[0] as RequestEntity).toExtendedStoredRequest("Company Name", null)
        }.whenever(mockRequestQueryManager).transformRequestEntityToExtendedStoredRequest(any<RequestEntity>())

        doReturn(requestEntitySfdr)
            .whenever(mockRequestRepository)
            .findByIdAndFetchDataSourcingEntity(dataRequestIdForSfdr)

        doAnswer { invocation -> invocation.arguments[0] }.whenever(mockRequestRepository).save(any())

        doReturn(dataSourcingEntity)
            .whenever(mockDataSourcingManager)
            .useExistingOrCreateDataSourcingAndAddRequest(requestEntitySfdr)
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

    @ParameterizedTest
    @EnumSource(DataSourcingState::class)
    fun `verify that only marking a sourcing process as Answered or NonSourceable will patch requests to Processed`(
        state: DataSourcingState,
    ) {
        val newDataSourcingEntity =
            DataSourcingEntity(
                dataSourcingId = UUID.randomUUID(),
                companyId = UUID.randomUUID(),
                reportingPeriod = "2025",
                dataType = "sfdr",
                state = DataSourcingState.Initialized,
                associatedRequests = mutableSetOf(requestEntitySfdr),
            )

        whenever(mockDataSourcingRepository.findByIdAndFetchAllStoredFields(any())).thenReturn(
            newDataSourcingEntity,
        )
        whenever(mockDataSourcingRepository.save(any())).thenAnswer {
            it.arguments[0] as DataSourcingEntity
        }

        val reducedDataSourcing =
            testDataSourcingManager.patchDataSourcingState(newDataSourcingEntity.dataSourcingId, state)
        if (state == DataSourcingState.NonSourceable) {
            verify(
                mockCloudEventMessageHandler,
                times(1),
            ).buildCEMessageAndSendToQueue(
                any(),
                eq(MessageType.DATASOURCING_NONSOURCEABLE),
                any(),
                eq(ExchangeName.DATASOURCING_DATA_NONSOURCEABLE),
                eq(RoutingKeyNames.DATASOURCING_NONSOURCEABLE),
            )
        } else {
            verifyNoInteractions(mockCloudEventMessageHandler)
        }
        Assertions.assertEquals(state, reducedDataSourcing.state)
        if (state == DataSourcingState.Done || state == DataSourcingState.NonSourceable) {
            Assertions.assertEquals(RequestState.Processed, requestEntitySfdr.state)
        } else {
            Assertions.assertNotEquals(RequestState.Processed, requestEntitySfdr.state)
        }
    }

    private fun withdrawRequest(
        initialState: RequestState,
        dataSourcing: DataSourcingEntity?,
    ): RequestEntity {
        val requestId = UUID.randomUUID()
        val requestEntity =
            RequestEntity(
                id = requestId,
                companyId = companyId,
                reportingPeriod = reportingPeriod,
                dataType = dataType,
                userId = userId,
                creationTimestamp = 1000000000,
                memberComment = null,
                adminComment = null,
                lastModifiedDate = 1000000000,
                requestPriority = RequestPriority.High,
                state = initialState,
                dataSourcingEntity = dataSourcing,
            )

        whenever(mockRequestRepository.findByIdAndFetchDataSourcingEntity(requestId))
            .thenReturn(requestEntity)
        whenever(mockRequestRepository.save(any())).thenAnswer { it.arguments[0] }

        existingRequestsManager.patchRequestState(requestId, RequestState.Withdrawn, null)

        Assertions.assertEquals(RequestState.Withdrawn, requestEntity.state)

        verify(mockDataSourcingServiceMessageSender, never())
            .sendMessageToAccountingServiceOnRequestProcessing(
                dataSourcingEntity = anyOrNull(),
                requestEntity = anyOrNull(),
            )
        verify(mockDataSourcingManager, never())
            .useExistingOrCreateDataSourcingAndAddRequest(any())
        verify(mockRequestRepository, times(1)).save(requestEntity)

        return requestEntity
    }

    @ParameterizedTest
    @EnumSource(
        value = RequestState::class,
        names = ["Processing", "Processed"],
        mode = Mode.INCLUDE,
    )
    fun `verify that withdrawal of processed or processing request sends accounting withdrawal message`(initialState: RequestState) {
        val requestEntity = withdrawRequest(initialState, dataSourcingEntity)

        verify(mockDataSourcingServiceMessageSender, times(1))
            .sendMessageToAccountingServiceOnRequestWithdrawn(
                dataSourcingEntity = dataSourcingEntity,
                requestEntity = requestEntity,
            )
    }

    @ParameterizedTest
    @EnumSource(
        value = RequestState::class,
        names = ["Processing", "Processed"],
        mode = Mode.EXCLUDE,
    )
    fun `verify that withdrawal of non processed or processing request does not send accounting withdrawal message`(
        initialState: RequestState,
    ) {
        withdrawRequest(initialState, dataSourcingEntity)

        verify(mockDataSourcingServiceMessageSender, never())
            .sendMessageToAccountingServiceOnRequestWithdrawn(
                dataSourcingEntity = anyOrNull(),
                requestEntity = anyOrNull(),
            )
    }

    @ParameterizedTest
    @EnumSource(
        value = RequestState::class,
        names = ["Processing", "Processed"],
        mode = Mode.INCLUDE,
    )
    fun `verify that withdrawal of processed or processing request without data sourcing does not send accounting withdrawal message`(
        initialState: RequestState,
    ) {
        withdrawRequest(initialState, dataSourcing = null)

        verify(mockDataSourcingServiceMessageSender, never())
            .sendMessageToAccountingServiceOnRequestWithdrawn(
                dataSourcingEntity = anyOrNull(),
                requestEntity = anyOrNull(),
            )
    }
}
