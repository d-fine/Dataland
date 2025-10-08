package org.dataland.datasourcingservice.unitTests

import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.dataland.datasourcingservice.services.RequestCreationService
import org.dataland.datasourcingservice.services.SingleRequestManager
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
class SingleRequestManagerTest {
    private val mockDataSourcingValidator = mock<DataSourcingValidator>()
    private val mockRequestRepository = mock<RequestRepository>()
    private val mockDataSourcingManager = mock<DataSourcingManager>()
    private val mockDataRevisionRepository = mock<DataRevisionRepository>()
    private val mockRequestCreationService = mock<RequestCreationService>()

    private val testSingleRequestManager =
        SingleRequestManager(
            mockDataSourcingValidator,
            mockRequestRepository, mockDataSourcingManager, mockDataRevisionRepository, mockRequestCreationService,
        )

    @BeforeEach
    fun setup() {
        reset(mockDataSourcingManager)
    }

    @ParameterizedTest
    @EnumSource(RequestState::class)
    fun `verify that a request is appended to the corresponding sourcing object only when set to Processing`(requestState: RequestState) {
        val newRequest =
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
        whenever(mockRequestRepository.findByIdAndFetchDataSourcingEntity(any())).thenReturn(
            newRequest,
        )
        whenever(mockRequestRepository.save(any())).thenReturn(newRequest)

        testSingleRequestManager.patchRequestState(UUID.randomUUID(), requestState, null)
        if (requestState == RequestState.Processing) {
            verify(mockDataSourcingManager, times(1)).resetOrCreateDataSourcingObjectAndAddRequest(any())
        } else {
            verify(mockDataSourcingManager, never()).resetOrCreateDataSourcingObjectAndAddRequest(any())
        }
    }
}
