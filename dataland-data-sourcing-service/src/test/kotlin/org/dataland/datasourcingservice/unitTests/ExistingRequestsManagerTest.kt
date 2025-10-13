package org.dataland.datasourcingservice.unitTests

import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.dataland.datasourcingservice.services.ExistingRequestsManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExistingRequestsManagerTest {
    private val mockDataSourcingValidator = mock<DataSourcingValidator>()
    private val mockRequestRepository = mock<RequestRepository>()
    private val mockDataSourcingManager = mock<DataSourcingManager>()
    private val mockDataRevisionRepository = mock<DataRevisionRepository>()
    private val mockDataSourcingRepository = mock<DataSourcingRepository>()

    private val testExistingRequestsManager =
        ExistingRequestsManager(mockRequestRepository, mockDataSourcingManager, mockDataRevisionRepository)

    private val testDataSourcingManager =
        DataSourcingManager(mockDataSourcingRepository, mockDataRevisionRepository, mockDataSourcingValidator)

    private lateinit var newRequest: RequestEntity

    @BeforeEach
    fun setup() {
        newRequest =
            RequestEntity(
                id = UUID.randomUUID(),
                companyId = UUID.randomUUID(),
                reportingPeriod = "2025",
                dataType = "sfdr",
                userId = UUID.randomUUID(),
                creationTimestamp = 1000000000,
                memberComment = null,
                adminComment = null,
                lastModifiedDate = 1000000000,
                requestPriority = RequestPriority.High,
                state = RequestState.Open,
                dataSourcingEntity = null,
            )
        reset(mockDataSourcingManager)
    }

    @ParameterizedTest
    @EnumSource(RequestState::class)
    fun `verify that a request is appended to the corresponding sourcing object only when set to Processing`(requestState: RequestState) {
        whenever(mockRequestRepository.findByIdAndFetchDataSourcingEntity(any())).thenReturn(
            newRequest,
        )
        whenever(mockRequestRepository.save(any())).thenReturn(newRequest)

        testExistingRequestsManager.patchRequestState(UUID.randomUUID(), requestState, null)
        if (requestState == RequestState.Processing) {
            verify(mockDataSourcingManager, times(1)).resetOrCreateDataSourcingObjectAndAddRequest(any())
        } else {
            verify(mockDataSourcingManager, never()).resetOrCreateDataSourcingObjectAndAddRequest(any())
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
                associatedRequests = mutableSetOf(newRequest),
            )

        whenever(mockDataSourcingRepository.findByIdAndFetchAllStoredFields(any())).thenReturn(
            newDataSourcingEntity,
        )
        whenever(mockDataSourcingRepository.save(any())).thenAnswer {
            it.arguments[0] as DataSourcingEntity
        }

        val reducedDataSourcing = testDataSourcingManager.patchDataSourcingState(newDataSourcingEntity.dataSourcingId, state)

        Assertions.assertEquals(state, reducedDataSourcing.state)
        if (state == DataSourcingState.Done || state == DataSourcingState.NonSourceable) {
            Assertions.assertEquals(RequestState.Processed, newRequest.state)
        } else {
            Assertions.assertNotEquals(RequestState.Processed, newRequest.state)
        }
    }
}
