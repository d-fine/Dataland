package org.dataland.datasourcingservice.unitTests

import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.exceptions.DataSourcingNotFoundApiException
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.dataland.datasourcingservice.services.ExistingRequestsManager
import org.dataland.datasourcingservice.services.RequestDataSourcingAssigner
import org.dataland.datasourcingservice.utils.DerivedRightsUtilsComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DataSourcingManagerTest {
    private val mockDataSourcingValidator = mock<DataSourcingValidator>()
    private val mockDataRevisionRepository = mock<DataRevisionRepository>()
    private val mockDataSourcingRepository = mock<DataSourcingRepository>()
    private val mockExistingRequestsManager = mock<ExistingRequestsManager>()
    private val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()
    private val mockDerivedRightsUtilsComponent = mock<DerivedRightsUtilsComponent>()

    private lateinit var dataSourcingManager: DataSourcingManager
    private lateinit var requestDataSourcingAssigner: RequestDataSourcingAssigner

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

    private lateinit var newDataSourcingEntity: DataSourcingEntity
    private lateinit var existingDataSourcingEntities: Map<DataSourcingState, DataSourcingEntity>

    @BeforeEach
    fun setup() {
        resetMocks()
        createTestEntities()
        setupMocks()
        createManagers()
    }

    private fun resetMocks() {
        reset(
            mockDataSourcingValidator,
            mockDataRevisionRepository,
            mockDataSourcingRepository,
            mockExistingRequestsManager,
            mockCloudEventMessageHandler,
        )
    }

    private fun createTestEntities() {
        newDataSourcingEntity = createDataSourcingEntity(DataSourcingState.Initialized, mutableSetOf(newRequest))
        existingDataSourcingEntities =
            DataSourcingState.entries.associateWith {
                createDataSourcingEntity(it, mutableSetOf(existingRequest))
            }
    }

    private fun setupMocks() {
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

        doAnswer { invocation ->
            val requestId = invocation.arguments[0] as UUID
            val state = invocation.arguments[1] as RequestState
            if (requestId == newRequest.id) {
                newRequest.state = state
            }
            storedRequestResponse
        }.whenever(mockExistingRequestsManager).patchRequestState(
            any<UUID>(),
            eq(RequestState.Processed),
            anyOrNull(),
        )
    }

    private fun createManagers() {
        dataSourcingManager =
            DataSourcingManager(
                dataSourcingValidator = mockDataSourcingValidator,
                dataRevisionRepository = mockDataRevisionRepository,
                dataSourcingRepository = mockDataSourcingRepository,
                existingRequestsManager = mockExistingRequestsManager,
                cloudEventMessageHandler = mockCloudEventMessageHandler,
                derivedRightsUtilsComponent = mockDerivedRightsUtilsComponent,
            )

        requestDataSourcingAssigner =
            RequestDataSourcingAssigner(
                dataSourcingRepository = mockDataSourcingRepository,
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

        val updatedDataSourcing = requestDataSourcingAssigner.useExistingOrCreateDataSourcingAndAddRequest(newRequest)

        if (dataSourcingState in setOf(DataSourcingState.Done, DataSourcingState.NonSourceable)) {
            assertEquals(DataSourcingState.Initialized, updatedDataSourcing.state)
        } else {
            assertEquals(dataSourcingState, updatedDataSourcing.state)
        }
    }

    @Test
    fun `retrieveDataSourcingHistory throws exception when no revisions exist`() {
        val nonExistentId = UUID.randomUUID()
        doReturn(emptyList<Pair<DataSourcingEntity, Long>>())
            .whenever(mockDataRevisionRepository)
            .listDataSourcingRevisionsById(nonExistentId)

        assertThrows<DataSourcingNotFoundApiException> {
            dataSourcingManager.retrieveDataSourcingHistory(nonExistentId)
        }
    }

    @Test
    fun `retrieveDataSourcingHistory returns all revisions when stateChangesOnly is false`() {
        val dataSourcingId = UUID.randomUUID()
        val revisions =
            listOf(
                Pair(createDataSourcingEntity(DataSourcingState.Initialized, dataSourcingId = dataSourcingId), 1000L),
                Pair(createDataSourcingEntity(DataSourcingState.Initialized, dataSourcingId = dataSourcingId), 2000L),
                Pair(createDataSourcingEntity(DataSourcingState.DocumentSourcing, dataSourcingId = dataSourcingId), 3000L),
            )
        doReturn(revisions).whenever(mockDataRevisionRepository).listDataSourcingRevisionsById(dataSourcingId)

        val result = dataSourcingManager.retrieveDataSourcingHistory(dataSourcingId, stateChangesOnly = false)

        assertEquals(3, result.size)
    }

    @Test
    fun `retrieveDataSourcingHistory returns only state changes when stateChangesOnly is true`() {
        val dataSourcingId = UUID.randomUUID()
        val revisions =
            listOf(
                Pair(createDataSourcingEntity(DataSourcingState.Initialized, dataSourcingId = dataSourcingId), 1000L),
                Pair(createDataSourcingEntity(DataSourcingState.Initialized, dataSourcingId = dataSourcingId), 2000L),
                Pair(createDataSourcingEntity(DataSourcingState.DocumentSourcing, dataSourcingId = dataSourcingId), 3000L),
                Pair(createDataSourcingEntity(DataSourcingState.DocumentSourcing, dataSourcingId = dataSourcingId), 4000L),
                Pair(createDataSourcingEntity(DataSourcingState.Done, dataSourcingId = dataSourcingId), 5000L),
            )
        doReturn(revisions).whenever(mockDataRevisionRepository).listDataSourcingRevisionsById(dataSourcingId)

        val result = dataSourcingManager.retrieveDataSourcingHistory(dataSourcingId, stateChangesOnly = true)

        assertEquals(3, result.size)
        assertEquals(DataSourcingState.Initialized, result[0].state)
        assertEquals(DataSourcingState.DocumentSourcing, result[1].state)
        assertEquals(DataSourcingState.Done, result[2].state)
    }

    @Test
    fun `retrieveDataSourcingHistory returns only first revision when no state changes occur`() {
        val dataSourcingId = UUID.randomUUID()
        val revisions =
            listOf(
                Pair(createDataSourcingEntity(DataSourcingState.Initialized, dataSourcingId = dataSourcingId), 1000L),
                Pair(createDataSourcingEntity(DataSourcingState.Initialized, dataSourcingId = dataSourcingId), 2000L),
                Pair(createDataSourcingEntity(DataSourcingState.Initialized, dataSourcingId = dataSourcingId), 3000L),
            )
        doReturn(revisions).whenever(mockDataRevisionRepository).listDataSourcingRevisionsById(dataSourcingId)

        val result = dataSourcingManager.retrieveDataSourcingHistory(dataSourcingId, stateChangesOnly = true)

        assertEquals(1, result.size)
        assertEquals(DataSourcingState.Initialized, result[0].state)
    }

    @ParameterizedTest
    @EnumSource(
        value = DataSourcingState::class,
        names = ["Done", "NonSourceable"],
        mode = EnumSource.Mode.INCLUDE,
    )
    fun `verify that withdrawn requests are not patched when data sourcing enters a final state`(finalState: DataSourcingState) {
        val withdrawnRequest =
            RequestEntity(
                userId = UUID.randomUUID(),
                companyId = companyId,
                dataType = "sfdr",
                reportingPeriod = "2025",
                memberComment = null,
                creationTimestamp = 1000000000,
                requestPriority = RequestPriority.High,
            ).apply {
                state = RequestState.Withdrawn
            }

        val activeRequest =
            RequestEntity(
                userId = UUID.randomUUID(),
                companyId = companyId,
                dataType = "sfdr",
                reportingPeriod = "2025",
                memberComment = null,
                creationTimestamp = 1000000000,
                requestPriority = RequestPriority.High,
            )

        val dataSourcingEntityWithMixedRequests =
            DataSourcingEntity(
                dataSourcingId = UUID.randomUUID(),
                companyId = companyId,
                reportingPeriod = "2025",
                dataType = "sfdr",
                state = DataSourcingState.Initialized,
                associatedRequests = mutableSetOf(withdrawnRequest, activeRequest),
            )

        doReturn(dataSourcingEntityWithMixedRequests)
            .whenever(mockDataSourcingRepository)
            .findByIdAndFetchAllStoredFields(dataSourcingEntityWithMixedRequests.dataSourcingId)

        dataSourcingManager.patchDataSourcingState(
            dataSourcingEntityWithMixedRequests.dataSourcingId,
            finalState,
        )

        verify(mockExistingRequestsManager).patchRequestState(
            eq(activeRequest.id),
            eq(RequestState.Processed),
            anyOrNull(),
        )

        verify(mockExistingRequestsManager, never()).patchRequestState(
            eq(withdrawnRequest.id),
            any(),
            anyOrNull(),
        )
    }

    @ParameterizedTest
    @EnumSource(
        value = DataSourcingState::class,
        names = ["Done", "NonSourceable"],
        mode = EnumSource.Mode.INCLUDE,
    )
    fun `verify that no requests are patched when all associated requests are withdrawn`(finalState: DataSourcingState) {
        val withdrawnRequest1 =
            RequestEntity(
                userId = UUID.randomUUID(),
                companyId = companyId,
                dataType = "sfdr",
                reportingPeriod = "2025",
                memberComment = null,
                creationTimestamp = 1000000000,
                requestPriority = RequestPriority.High,
            ).apply {
                state = RequestState.Withdrawn
            }

        val withdrawnRequest2 =
            RequestEntity(
                userId = UUID.randomUUID(),
                companyId = companyId,
                dataType = "sfdr",
                reportingPeriod = "2025",
                memberComment = null,
                creationTimestamp = 1000000000,
                requestPriority = RequestPriority.High,
            ).apply {
                state = RequestState.Withdrawn
            }

        val dataSourcingEntityWithOnlyWithdrawn =
            DataSourcingEntity(
                dataSourcingId = UUID.randomUUID(),
                companyId = companyId,
                reportingPeriod = "2025",
                dataType = "sfdr",
                state = DataSourcingState.Initialized,
                associatedRequests = mutableSetOf(withdrawnRequest1, withdrawnRequest2),
            )

        doReturn(dataSourcingEntityWithOnlyWithdrawn)
            .whenever(mockDataSourcingRepository)
            .findByIdAndFetchAllStoredFields(dataSourcingEntityWithOnlyWithdrawn.dataSourcingId)

        dataSourcingManager.patchDataSourcingState(
            dataSourcingEntityWithOnlyWithdrawn.dataSourcingId,
            finalState,
        )

        verify(mockExistingRequestsManager, never()).patchRequestState(
            any(),
            any(),
            anyOrNull(),
        )
    }

    private fun createDataSourcingEntity(
        state: DataSourcingState,
        associatedRequests: MutableSet<RequestEntity> = mutableSetOf(),
        dataSourcingId: UUID = UUID.randomUUID(),
    ): DataSourcingEntity =
        DataSourcingEntity(
            dataSourcingId = dataSourcingId,
            companyId = companyId,
            reportingPeriod = "2025",
            dataType = "sfdr",
            state = state,
            associatedRequests = associatedRequests,
        )
}
