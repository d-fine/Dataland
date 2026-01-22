package org.dataland.datasourcingservice.unitTests

import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.dataland.datasourcingservice.services.ExistingRequestsManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.util.UUID

class DataSourcingManagerTest {
    private val mockDataSourcingValidator = mock<DataSourcingValidator>()
    private val mockDataRevisionRepository = mock<DataRevisionRepository>()
    private val mockDataSourcingRepository = mock<DataSourcingRepository>()
    private val mockExistingRequestsManager = mock<ExistingRequestsManager>()
    private val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()

    private lateinit var dataSourcingManager: DataSourcingManager

    private val companyId = UUID.fromString("00000000-0000-0000-0000-000000000000")

    private val requests =
        List(2) {
            RequestEntity(
                userId = UUID.randomUUID(),
                companyId = companyId,
                dataType = "sfdr",
                reportingPeriod = "2025",
                memberComment = null,
                creationTimestamp = 1000000000,
                requestPriority = RequestPriority.High,
            )
        }

    private val newRequest = requests.first()
    private val existingRequest = requests.last()

    private val newDataSourcingEntity =
        DataSourcingEntity(
            dataSourcingId = UUID.randomUUID(),
            companyId = companyId,
            reportingPeriod = "2025",
            dataType = "sfdr",
            state = DataSourcingState.Initialized,
            associatedRequests = mutableSetOf(newRequest),
        )

    private val existingDataSourcingEntities =
        DataSourcingState.entries.associateWith {
            DataSourcingEntity(
                dataSourcingId = UUID.randomUUID(),
                companyId = companyId,
                reportingPeriod = "2025",
                dataType = "sfdr",
                state = it,
                associatedRequests = mutableSetOf(existingRequest),
            )
        }

    @BeforeEach
    fun setup() {
        reset(
            mockDataSourcingValidator,
            mockDataRevisionRepository,
            mockDataSourcingRepository,
            mockExistingRequestsManager,
            mockCloudEventMessageHandler,
        )

        doReturn(newDataSourcingEntity).whenever(mockDataSourcingRepository).findByIdAndFetchAllStoredFields(any())
        doAnswer { invocation -> invocation.arguments[0] }.whenever(mockDataSourcingRepository).save(any())

        val storedRequestResponse =
            StoredRequest(
                id = newRequest.id.toString(),
                companyId = newRequest.companyId.toString(),
                reportingPeriod = newRequest.reportingPeriod,
                dataType = newRequest.dataType,
                userId = newRequest.userId.toString(),
                creationTimestamp = newRequest.creationTimestamp,
                memberComment = newRequest.memberComment,
                adminComment = newRequest.adminComment,
                lastModifiedDate = newRequest.lastModifiedDate,
                requestPriority = newRequest.requestPriority,
                state = RequestState.Processed,
                dataSourcingEntityId = newDataSourcingEntity.dataSourcingId.toString(),
            )

        doReturn(storedRequestResponse).whenever(mockExistingRequestsManager).patchRequestState(
            any<UUID>(),
            eq(RequestState.Processed),
            anyOrNull(),
        )

        dataSourcingManager =
            DataSourcingManager(
                dataSourcingValidator = mockDataSourcingValidator,
                dataRevisionRepository = mockDataRevisionRepository,
                dataSourcingRepository = mockDataSourcingRepository,
                existingRequestsManager = mockExistingRequestsManager,
                cloudEventMessageHandler = mockCloudEventMessageHandler,
            )
    }

    @ParameterizedTest
    @EnumSource(DataSourcingState::class)
    fun `verify that only marking a sourcing process as Done or NonSourceable will patch requests to Processed`(state: DataSourcingState) {
        val reducedDataSourcing = dataSourcingManager.patchDataSourcingState(newDataSourcingEntity.dataSourcingId, state)

        assertEquals(state, reducedDataSourcing.state)
        if (state == DataSourcingState.Done || state == DataSourcingState.NonSourceable) {
            assertEquals(RequestState.Processed, newRequest.state)
        } else {
            assertNotEquals(RequestState.Processed, newRequest.state)
        }
    }

    @ParameterizedTest
    @EnumSource(DataSourcingState::class)
    fun `verify that useExistingOrCreateDataSourcingAndAddRequest resets an existing data sourcing only reset when in a final state`(
        dataSourcingState: DataSourcingState,
    ) {
        doReturn(existingDataSourcingEntities[dataSourcingState])
            .whenever(mockDataSourcingRepository)
            .findByDataDimensionAndFetchAllStoredFields(
                newRequest.companyId,
                newRequest.dataType,
                newRequest.reportingPeriod,
            )

        val updatedDataSourcing = dataSourcingManager.useExistingOrCreateDataSourcingAndAddRequest(newRequest)

        if (dataSourcingState in setOf(DataSourcingState.Done, DataSourcingState.NonSourceable)) {
            assertEquals(DataSourcingState.Initialized, updatedDataSourcing.state)
        } else {
            assertEquals(dataSourcingState, updatedDataSourcing.state)
        }
    }
}
