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
import org.dataland.datasourcingservice.services.RequestHistoryService
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
class RequestHistoryServiceTest
    @Autowired
    constructor(
        private val requestHistoryService: RequestHistoryService,
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

        private val processingComment = "Request Processing"

        private val dummyRequestStateHistory: List<RequestEntity> =
            listOf(
                RequestEntity(
                    id = requestId,
                    companyId = dummyCompanyId,
                    reportingPeriod = "2025",
                    dataType = "dummyDataType",
                    userId = dummyUserId,
                    creationTimestamp = lastModifiedDateFirstRequest,
                    adminComment = "Request Opened",
                    memberComment = null,
                    lastModifiedDate = lastModifiedDateFirstRequest,
                    requestPriority = RequestPriority.Low,
                    state = RequestState.Open,
                ),
                RequestEntity(
                    id = requestId,
                    companyId = dummyCompanyId,
                    reportingPeriod = "2025",
                    dataType = "dummyDataType",
                    userId = dummyUserId,
                    adminComment = processingComment,
                    creationTimestamp = lastModifiedDateFirstRequest + 600000L,
                    memberComment = null,
                    lastModifiedDate = lastModifiedDateFirstRequest + 600000L,
                    requestPriority = RequestPriority.Low,
                    state = RequestState.Processing,
                ),
                RequestEntity(
                    id = requestId,
                    companyId = dummyCompanyId,
                    reportingPeriod = "2025",
                    dataType = "dummyDataType",
                    userId = dummyUserId,
                    creationTimestamp = lastModifiedDateFirstRequest + 2 * 600000L,
                    memberComment = null,
                    lastModifiedDate = lastModifiedDateFirstRequest + 2 * 600000L,
                    requestPriority = RequestPriority.Low,
                    state = RequestState.Withdrawn,
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
                    lastModifiedDate = lastModifiedDateFirstRequest + 600000L + 50L,
                ),
                DataSourcingWithoutReferences(
                    dataSourcingId = dataSourcingID.toString(),
                    companyId = dummyCompanyId.toString(),
                    reportingPeriod = "2025",
                    dataType = "dummyDataType",
                    state = DataSourcingState.DocumentSourcing,
                    dateOfNextDocumentSourcingAttempt = null,
                    documentCollector = null,
                    dataExtractor = null,
                    adminComment = null,
                    lastModifiedDate = lastModifiedDateFirstRequest + (1.5 * 600000).toLong(),
                ),
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
                    lastModifiedDate = lastModifiedDateFirstRequest + 3 * 600000,
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

            doReturn(dummyRequestEntity).whenever(mockRequestRepository).findByIdAndFetchDataSourcingEntity(requestId)

            doReturn(dummyExtendedStoredRequest)
                .whenever(mockRequestQueryManager)
                .transformRequestEntityToExtendedStoredRequest(dummyRequestEntity)
        }

        @Test
        fun `request history is sorted by timestamps`() {
            doReturn(dummyRequestStateHistory).whenever(mockDataRevisionRepository).listDataRequestRevisionsById(requestId)

            doReturn(dummyDataSourcingStatHistory)
                .whenever(mockDataSourcingManager)
                .retrieveDataSourcingHistory(
                    ValidationUtils.convertToUUID(
                        dataSourcingID.toString(),
                    ),
                    true,
                )

            val requestHistory = requestHistoryService.retrieveRequestHistory(requestId)

            requestHistory.zipWithNext { a, b ->
                Assertions.assertTrue(a.modificationDate < b.modificationDate)
            }

            val extendRequestHistory = requestHistoryService.retrieveExtendedRequestHistory(requestId)

            extendRequestHistory.zipWithNext { a, b ->
                Assertions.assertTrue(a.modificationDate < b.modificationDate)
            }
        }

        @Test
        fun `single history entry if state changes in request and data sourcing are within 1000 ms`() {
            doReturn(dummyRequestStateHistory.subList(0, 2)).whenever(mockDataRevisionRepository).listDataRequestRevisionsById(requestId)

            doReturn(dummyDataSourcingStatHistory.subList(0, 1))
                .whenever(mockDataSourcingManager)
                .retrieveDataSourcingHistory(
                    ValidationUtils.convertToUUID(
                        dataSourcingID.toString(),
                    ),
                    true,
                )

            val requestHistory = requestHistoryService.retrieveRequestHistory(requestId)

            Assertions.assertEquals(2, requestHistory.size)

            val extendRequestHistory = requestHistoryService.retrieveExtendedRequestHistory(requestId)

            Assertions.assertEquals(2, extendRequestHistory.size)
        }

        @Test
        fun `same admin comment is visible for equal request states`() {
            doReturn(dummyRequestStateHistory.subList(0, 2)).whenever(mockDataRevisionRepository).listDataRequestRevisionsById(requestId)

            doReturn(dummyDataSourcingStatHistory.subList(0, 2))
                .whenever(mockDataSourcingManager)
                .retrieveDataSourcingHistory(
                    ValidationUtils.convertToUUID(
                        dataSourcingID.toString(),
                    ),
                    true,
                )

            val extendRequestHistory = requestHistoryService.retrieveExtendedRequestHistory(requestId)

            Assertions.assertEquals(processingComment, extendRequestHistory[1].adminComment)
            Assertions.assertEquals(processingComment, extendRequestHistory[2].adminComment)
        }

        @Test
        fun `check that consecutive rows with same displayed status are only shown once in not extended request history`() {
            doReturn(dummyRequestStateHistory).whenever(mockDataRevisionRepository).listDataRequestRevisionsById(requestId)

            doReturn(dummyDataSourcingStatHistory)
                .whenever(mockDataSourcingManager)
                .retrieveDataSourcingHistory(
                    ValidationUtils.convertToUUID(
                        dataSourcingID.toString(),
                    ),
                    true,
                )

            val requestHistory = requestHistoryService.retrieveRequestHistory(requestId)

            Assertions.assertEquals(4, requestHistory.size)
            requestHistory.zipWithNext { a, b ->
                Assertions.assertNotEquals(a.displayedState, b.displayedState)
            }

            val extendedRequestHistory = requestHistoryService.retrieveExtendedRequestHistory(requestId)
            Assertions.assertEquals(5, extendedRequestHistory.size)
        }
    }
