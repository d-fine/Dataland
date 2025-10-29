package org.dataland.datasourcingservice.utils

import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.ExpectedPublicationDateOfDocument
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.mockito.kotlin.mock
import java.time.LocalDate
import java.util.UUID

const val COMPANY_ID_1 = "b1234a45-204a-4ec2-9617-f4646cd44524"
const val COMPANY_ID_2 = "d840fd07-94ea-4128-8ade-35a031b3a31d"
const val DATA_TYPE_1 = "sfdr"
const val DATA_TYPE_2 = "eutaxonomy-financials"
const val REPORTING_PERIOD_1 = "2024"
const val REPORTING_PERIOD_2 = "2025"
const val REQUEST_STATE_1 = "Open"
const val REQUEST_STATE_2 = "Processing"
const val DATA_SOURCING_STATE_1 = "Initialized"
const val DATA_SOURCING_STATE_2 = "DocumentSourcing"
const val TEST_COMPANY_SEARCH_STRING = "TestCompany"
const val TEST_COMPANY_NAME_1 = "TestCompany Name"
const val TEST_COMPANY_NAME_2 = "DummyCompany Name"
const val USER_EMAIL = "testuser@email.com"
const val USER_EMAIL_SEARCH_STRING = "testuser"
const val ADMIN_COMMENT = "This is an admin comment."
const val ADMIN_COMMENT_SEARCH_STRING = "admin comment"

/**
 * Utility class to create and store test data in the database using SpringBootTests.
 */
class DataBaseCreationUtils(
    val requestRepository: RequestRepository = mock<RequestRepository>(),
    val dataSourcingRepository: DataSourcingRepository = mock<DataSourcingRepository>(),
) {
    /**
     * Wrapper function to enable simple request storage in the database with default values,
     * which can be overridden.
     */
    @Suppress("LongParameterList")
    fun storeRequest(
        requestId: UUID = UUID.randomUUID(),
        companyId: UUID = UUID.fromString(COMPANY_ID_1),
        reportingPeriod: String = REPORTING_PERIOD_1,
        dataType: String = DATA_TYPE_1,
        userId: UUID = UUID.randomUUID(),
        creationTimestamp: Long = 0L,
        memberComment: String? = null,
        adminComment: String? = null,
        lastModifiedDate: Long = 0L,
        requestPriority: RequestPriority = RequestPriority.High,
        state: RequestState = RequestState.valueOf(REQUEST_STATE_1),
        dataSourcingEntity: DataSourcingEntity? = null,
    ): RequestEntity =
        requestRepository.saveAndFlush(
            RequestEntity(
                id = requestId,
                companyId = companyId,
                reportingPeriod = reportingPeriod,
                dataType = dataType,
                userId = userId,
                creationTimestamp = creationTimestamp,
                memberComment = memberComment,
                adminComment = adminComment,
                lastModifiedDate = lastModifiedDate,
                requestPriority = requestPriority,
                state = state,
                dataSourcingEntity = dataSourcingEntity,
            ),
        )

    /**
     * Wrapper function to enable simple data sourcing storage in the database with default values,
     * which can be overridden.
     */
    @Suppress("LongParameterList")
    fun storeDataSourcing(
        dataSourcingId: UUID = UUID.randomUUID(),
        companyId: UUID = UUID.fromString(COMPANY_ID_1),
        reportingPeriod: String = REPORTING_PERIOD_1,
        dataType: String = DATA_TYPE_1,
        state: DataSourcingState = DataSourcingState.valueOf(DATA_SOURCING_STATE_1),
        documentIds: Set<String> = emptySet(),
        expectedPublicationDatesOfDocuments: Set<ExpectedPublicationDateOfDocument> = emptySet(),
        dateOfNextDocumentSourcingAttempt: LocalDate? = null,
        documentCollector: UUID? = null,
        dataExtractor: UUID? = null,
        adminComment: String? = null,
        associatedRequests: MutableSet<RequestEntity> = mutableSetOf(),
    ): DataSourcingEntity =
        dataSourcingRepository.saveAndFlush(
            DataSourcingEntity(
                dataSourcingId = dataSourcingId,
                companyId = companyId,
                reportingPeriod = reportingPeriod,
                dataType = dataType,
                state = state,
                documentIds = documentIds,
                expectedPublicationDatesOfDocuments = expectedPublicationDatesOfDocuments,
                dateOfNextDocumentSourcingAttempt = dateOfNextDocumentSourcingAttempt,
                documentCollector = documentCollector,
                dataExtractor = dataExtractor,
                adminComment = adminComment,
                associatedRequests = associatedRequests,
            ),
        )
}
