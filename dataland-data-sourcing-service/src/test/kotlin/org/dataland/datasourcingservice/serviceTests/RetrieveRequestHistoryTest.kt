package org.dataland.datasourcingservice.serviceTests

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.ExistingRequestsManager
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID

@SpringBootTest(classes = [DatalandDataSourcingService::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureMockMvc
class RetrieveRequestHistoryTest
    @Autowired
    constructor(
        private val existingRequestsManager: ExistingRequestsManager,
    ) {
        @MockitoBean
        private lateinit var mockDataRevisionRepository: DataRevisionRepository

        @MockitoBean
        private lateinit var mockDataSourcingManager: DataSourcingManager

        @MockitoBean
        private lateinit var mockExistingRequestsManager: ExistingRequestsManager

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

        @Test
        fun `request history is sorted by timestamps`() {
            doReturn(dummyRequestStateHistory).whenever(mockDataRevisionRepository).listDataRequestRevisionsById(requestId)

            doReturn(dataSourcingID.toString()).whenever(mockExistingRequestsManager).getRequest(requestId).dataSourcingEntityId

            doReturn(dummyDataSourcingStatHistory)
                .whenever(mockDataSourcingManager)
                .retrieveDataSourcingHistory(
                    ValidationUtils.convertToUUID(
                        dataSourcingID.toString(),
                    ),
                    true,
                )

            val requestHistory = existingRequestsManager.retrieveRequestHistory(requestId)
            print(requestHistory)
        }
    }
