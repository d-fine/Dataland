package org.dataland.datasourcingservice.serviceTests

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.ExtendedStoredRequest
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.ExistingRequestsManager
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID

@SpringBootTest(classes = [DatalandDataSourcingService::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureMockMvc
class ExistingRequestsManagerTest
    @Autowired
    constructor(
        private val existingRequestsManager: ExistingRequestsManager,
    ) {
        @MockitoBean
        private lateinit var mockDataRevisionRepository: DataRevisionRepository

        @MockitoBean
        private lateinit var mockDataSourcingManager: DataSourcingManager

        @MockitoBean
        private lateinit var mockRequestRepository: RequestRepository

        @MockitoBean
        private lateinit var mockRequestQueryManager: RequestQueryManager

        private val requestId = UUID.randomUUID()
        private val dummyUserId = UUID.randomUUID()
        private val dummyCompanyId = UUID.randomUUID()
        private val dataSourcingID = UUID.randomUUID()

        private val lastModifiedDateFirstRequest = 1000L

        private val dummyRequestStateHistory: List<Pair<RequestEntity, Long>> =
            listOf(
                Pair(
                    RequestEntity(
                        id = requestId,
                        companyId = dummyCompanyId,
                        reportingPeriod = "2025",
                        dataType = "dummyDataType",
                        userId = dummyUserId,
                        creationTimestamp = lastModifiedDateFirstRequest,
                        memberComment = null,
                        lastModifiedDate = lastModifiedDateFirstRequest,
                        requestPriority = RequestPriority.Low,
                        state = RequestState.Open,
                    ),
                    lastModifiedDateFirstRequest,
                ),
                Pair(
                    RequestEntity(
                        id = requestId,
                        companyId = dummyCompanyId,
                        reportingPeriod = "2025",
                        dataType = "dummyDataType",
                        userId = dummyUserId,
                        creationTimestamp = lastModifiedDateFirstRequest + 500L,
                        memberComment = null,
                        lastModifiedDate = lastModifiedDateFirstRequest + 500L,
                        requestPriority = RequestPriority.Low,
                        state = RequestState.Processing,
                    ),
                    lastModifiedDateFirstRequest + 500L,
                ),
            )

        private val dummyDataSourcingStatHistory: List<DataSourcingWithoutReferences> =
            listOf(
                DataSourcingWithoutReferences(
                    dataSourcingId = dataSourcingID.toString(),
                    companyId = dummyCompanyId.toString(),
                    reportingPeriod = "2025",
                    dataType = "dummyDataType",
                    state = DataSourcingState.Initialized,
                    dateOfNextDocumentSourcingAttempt = null,
                    documentCollector = null,
                    dataExtractor = null,
                    adminComment = null,
                    lastModifiedDate = lastModifiedDateFirstRequest + 500L,
                ),
            )

        private val dummyRequestEntity =
            RequestEntity(
                id = requestId,
                companyId = dummyCompanyId,
                reportingPeriod = "2025",
                dataType = "dummyDataType",
                userId = dummyUserId,
                creationTimestamp = lastModifiedDateFirstRequest,
                memberComment = null,
                lastModifiedDate = lastModifiedDateFirstRequest,
                requestPriority = RequestPriority.Low,
                state = RequestState.Open,
            )

        private val dummyExtendedStoredRequest: ExtendedStoredRequest =
            ExtendedStoredRequest(
                id = requestId.toString(),
                companyId = dummyCompanyId.toString(),
                reportingPeriod = "2025",
                dataType = "dummyDataType",
                userId = dummyUserId.toString(),
                creationTimestamp = lastModifiedDateFirstRequest,
                memberComment = null,
                adminComment = null,
                lastModifiedDate = lastModifiedDateFirstRequest,
                requestPriority = RequestPriority.Low,
                state = RequestState.Open,
                dataSourcingEntityId = dataSourcingID.toString(),
                companyName = "Dummy Company",
                userEmailAddress = "",
            )

        @BeforeEach
        fun setup() {
            reset(
                mockDataRevisionRepository,
                mockDataSourcingManager,
                mockRequestRepository,
                mockRequestQueryManager,
            )
            doReturn(dummyRequestStateHistory).whenever(mockDataRevisionRepository).listDataRequestRevisionsById(requestId)

            doReturn(dummyRequestEntity).whenever(mockRequestRepository).findByIdAndFetchDataSourcingEntity(requestId)

            doReturn(dummyExtendedStoredRequest)
                .whenever(mockRequestQueryManager)
                .transformRequestEntityToExtendedStoredRequest(dummyRequestEntity)

            doReturn(dummyDataSourcingStatHistory)
                .whenever(mockDataSourcingManager)
                .retrieveDataSourcingHistory(
                    ValidationUtils.convertToUUID(
                        dataSourcingID.toString(),
                    ),
                    true,
                )
        }

        @Test
        fun `request history is sorted by timestamps`() {
            val requestHistory = existingRequestsManager.retrieveRequestHistory(requestId)

            Assertions.assertEquals(1000, requestHistory[0].modificationDate)
            Assertions.assertEquals(1500, requestHistory[1].modificationDate)

            val extendRequestHistory = existingRequestsManager.retrieveExtendedRequestHistory(requestId)

            Assertions.assertEquals(1000, extendRequestHistory[0].modificationDate)
            Assertions.assertEquals(1500, extendRequestHistory[1].modificationDate)
        }

        @Test
        fun `Check that consecutive rows with same displayed status are only shown once in not-extended request history`() {
            val requestStateHistory: List<Pair<RequestEntity, Long>> =
                dummyRequestStateHistory +
                    Pair(
                        RequestEntity(
                            id = requestId,
                            companyId = dummyCompanyId,
                            reportingPeriod = "2025",
                            dataType = "dummyDataType",
                            userId = dummyUserId,
                            creationTimestamp = lastModifiedDateFirstRequest + 1000L,
                            memberComment = null,
                            lastModifiedDate = lastModifiedDateFirstRequest + 1000L,
                            requestPriority = RequestPriority.Low,
                            state = RequestState.Withdrawn,
                        ),
                        lastModifiedDateFirstRequest + 1000L,
                    )

            val dataSourcingStatHistory: List<DataSourcingWithoutReferences> =
                dummyDataSourcingStatHistory +
                    DataSourcingWithoutReferences(
                        dataSourcingId = dataSourcingID.toString(),
                        companyId = dummyCompanyId.toString(),
                        reportingPeriod = "2025",
                        dataType = "dummyDataType",
                        state = DataSourcingState.DataExtraction,
                        dateOfNextDocumentSourcingAttempt = null,
                        documentCollector = null,
                        dataExtractor = null,
                        adminComment = null,
                        lastModifiedDate = lastModifiedDateFirstRequest + 1500L,
                    )

            doReturn(requestStateHistory).whenever(mockDataRevisionRepository).listDataRequestRevisionsById(requestId)

            doReturn(dataSourcingStatHistory)
                .whenever(mockDataSourcingManager)
                .retrieveDataSourcingHistory(
                    ValidationUtils.convertToUUID(
                        dataSourcingID.toString(),
                    ),
                    true,
                )

            val requestHistory = existingRequestsManager.retrieveRequestHistory(requestId)

            Assertions.assertEquals(3, requestHistory.size)
            requestHistory.zipWithNext { a, b ->
                Assertions.assertNotEquals(a.displayedState, b.displayedState)
            }
        }
    }
