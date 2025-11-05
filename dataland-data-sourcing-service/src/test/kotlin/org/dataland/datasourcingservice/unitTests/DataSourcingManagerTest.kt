package org.dataland.datasourcingservice.unitTests

import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.util.UUID

class DataSourcingManagerTest {
    private val mockDataSourcingValidator = mock<DataSourcingValidator>()
    private val mockDataRevisionRepository = mock<DataRevisionRepository>()
    private val mockDataSourcingRepository = mock<DataSourcingRepository>()

    private lateinit var dataSourcingManager: DataSourcingManager

    private val newRequest =
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

    private val newDataSourcingEntity =
        DataSourcingEntity(
            dataSourcingId = UUID.randomUUID(),
            companyId = UUID.randomUUID(),
            reportingPeriod = "2025",
            dataType = "sfdr",
            state = DataSourcingState.Initialized,
            associatedRequests = mutableSetOf(newRequest),
        )

    @BeforeEach
    fun setup() {
        reset(
            mockDataSourcingValidator,
            mockDataRevisionRepository,
            mockDataSourcingRepository,
        )

        doReturn(newDataSourcingEntity).whenever(mockDataSourcingRepository).findByIdAndFetchAllStoredFields(any())
        doAnswer { invocation -> invocation.arguments[0] }.whenever(mockDataSourcingRepository).save(any())

        dataSourcingManager =
            DataSourcingManager(
                dataSourcingValidator = mockDataSourcingValidator,
                dataRevisionRepository = mockDataRevisionRepository,
                dataSourcingRepository = mockDataSourcingRepository,
            )
    }

    @ParameterizedTest
    @EnumSource(DataSourcingState::class)
    fun `verify that only marking a sourcing process as Answered or NonSourceable will patch requests to Processed`(
        state: DataSourcingState,
    ) {
        val reducedDataSourcing = dataSourcingManager.patchDataSourcingState(newDataSourcingEntity.dataSourcingId, state)

        Assertions.assertEquals(state, reducedDataSourcing.state)
        if (state == DataSourcingState.Done || state == DataSourcingState.NonSourceable) {
            Assertions.assertEquals(RequestState.Processed, newRequest.state)
        } else {
            Assertions.assertNotEquals(RequestState.Processed, newRequest.state)
        }
    }
}
