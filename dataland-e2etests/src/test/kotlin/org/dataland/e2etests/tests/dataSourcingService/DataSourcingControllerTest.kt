package org.dataland.e2etests.tests.dataSourcingService

import org.dataland.dataSourcingService.openApiClient.infrastructure.ClientException
import org.dataland.dataSourcingService.openApiClient.model.DataSourcingState
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.StoredDataSourcing
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataSourcingControllerTest : DataSourcingTest() {
    private val documentControllerApiAccessor = DocumentControllerApiAccessor()

    private fun assertForbiddenException(function: () -> Unit) {
        val exception =
            assertThrows<ClientException> {
                function()
            }
        assertEquals(403, exception.statusCode)
    }

    private fun verifyDataSourcingDocuments(
        dataSourcingObjectId: String,
        expectedDocuments: Set<String>,
    ) {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObjectId)
            assertEquals(expectedDocuments, updatedDataSourcingObject.documentIds)
        }
    }

    private fun changeSourcingStatusAndVerifyRequestsStatuses(
        newSourcingStatus: DataSourcingState,
        expectedRequestState: RequestState,
    ) {
        val requestId1 = createRequest(storedDataSourcing.companyId, user = TechnicalUser.PremiumUser)
        val requestId2 = createRequest(storedDataSourcing.companyId, user = TechnicalUser.Admin)

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            // requests are only linked to data sourcing objects after having been validated by the QARG Team and set to Processing
            apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId1, RequestState.Processing)
            apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId2, RequestState.Processing)
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.dataSourcingId, newSourcingStatus)
        }

        val updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.dataSourcingId)
        assertEquals(2, updatedSourcingObject.associatedRequestIds.size)
        updatedSourcingObject.associatedRequestIds.forEach {
            val request = apiAccessor.dataSourcingRequestControllerApi.getRequest(it)
            assertEquals(expectedRequestState, request.state)
        }
    }

    /**
     * Initialize a data sourcing object by creating a request and setting its state to 'Processing'.
     *
     * The object is stored in the class variable 'dataSourcingObject' for use in the tests.
     */
    @BeforeEach
    fun setup() {
        super.initializeDataSourcing()
    }

    @Test
    fun `verify that data sourcing objects behave as they should during the early stages of their lifecycle`() {
        val (companyId, requestId) = createNewCompanyAndRequestAndReturnTheirIds()

        GlobalAuth.withTechnicalUser(TechnicalUser.Reader) {
            assertForbiddenException {
                apiAccessor.dataSourcingControllerApi.searchDataSourcings(
                    companyId = companyId,
                    dataType = testDataType,
                    reportingPeriod = testReportingPeriod,
                )
            }
        }

        assert(
            apiAccessor.dataSourcingControllerApi
                .searchDataSourcings(
                    companyId = companyId,
                    dataType = testDataType,
                    reportingPeriod = testReportingPeriod,
                ).isEmpty(),
        )

        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Processing)

        val dataSourcing =
            apiAccessor.dataSourcingControllerApi
                .searchDataSourcings(
                    companyId = companyId,
                    dataType = testDataType,
                    reportingPeriod = testReportingPeriod,
                ).first()

        assertEquals(DataSourcingState.Initialized, dataSourcing.state)
        assertEquals(setOf(requestId), dataSourcing.associatedRequestIds)
    }

    @Test
    fun `verify that a data sourcing object can be retrieved using its ID`() {
        val dataSourcingObjectById =
            apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.dataSourcingId)
        assertEquals(storedDataSourcing, dataSourcingObjectById)
    }

    @Test
    fun `verify that a request is appended to the corresponding sourcing object only when set to Processing`() {
        val newRequest =
            createRequest(
                companyId = storedDataSourcing.companyId,
                dataType = storedDataSourcing.dataType,
                reportingPeriod = storedDataSourcing.reportingPeriod,
                comment = "Second request",
                user = TechnicalUser.PremiumUser,
            )

        var updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.dataSourcingId)
        assertEquals(storedDataSourcing.associatedRequestIds, updatedSourcingObject.associatedRequestIds)

        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(newRequest, RequestState.Processing)
        updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.dataSourcingId)
        assertEquals(
            storedDataSourcing.associatedRequestIds.plus(newRequest),
            updatedSourcingObject.associatedRequestIds,
        )
    }

    @ParameterizedTest
    @EnumSource(DataSourcingState::class)
    fun `verify that only marking a sourcing process as Answered or NonSourceable will patch requests to Processed`(
        state: DataSourcingState,
    ) {
        val expectedStatus =
            if (state == DataSourcingState.Done || state == DataSourcingState.NonSourceable) {
                RequestState.Processed
            } else {
                RequestState.Processing
            }

        changeSourcingStatusAndVerifyRequestsStatuses(state, expectedStatus)
    }

    @Test
    fun `reopen a processed request and verify that the data sourcing process is started anew`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcing.dataSourcingId,
                DataSourcingState.Done,
            )
        }

        val requestId = storedDataSourcing.associatedRequestIds.first()
        val request = apiAccessor.dataSourcingRequestControllerApi.getRequest(requestId)
        assertEquals(RequestState.Processed, request.state)

        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Open)
        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Processing)
        val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.dataSourcingId)
        assertEquals(DataSourcingState.Initialized, updatedDataSourcingObject.state)
    }

    @Test
    fun `verify that an admin can assign a document collector and a data extractor`() {
        val companyIdCollector =
            createNewCompanyAndReturnId()
        val companyIdExtractor =
            createNewCompanyAndReturnId()
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcing.dataSourcingId,
                DataSourcingState.DocumentSourcing,
            )
            apiAccessor.dataSourcingControllerApi.patchProviderAndAdminComment(
                storedDataSourcing.dataSourcingId,
                companyIdCollector,
            )
        }
        var updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.dataSourcingId)
        assertEquals(companyIdCollector, updatedDataSourcingObject.documentCollector)
        assertEquals(null, updatedDataSourcingObject.dataExtractor)
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcing.dataSourcingId,
                DataSourcingState.DataExtraction,
            )
            updatedDataSourcingObject =
                apiAccessor.dataSourcingControllerApi.patchProviderAndAdminComment(
                    storedDataSourcing.dataSourcingId,
                    null,
                    companyIdExtractor,
                )
        }
        assertEquals(companyIdCollector, updatedDataSourcingObject.documentCollector)
        assertEquals(companyIdExtractor, updatedDataSourcingObject.dataExtractor)
    }

    private fun patchOpenRequestsToProcessingAndReturnInitializedStoredDataSourcings(
        requestIds: List<String>,
        companyIds: List<String>,
    ): List<StoredDataSourcing> {
        require(requestIds.size == companyIds.size) {
            "requestIds and companyIds are index-linked lists."
        }
        requestIds.forEach {
            apiAccessor.dataSourcingRequestControllerApi.patchRequestState(it, RequestState.Processing)
        }
        return companyIds.map {
            apiAccessor.dataSourcingControllerApi
                .searchDataSourcings(
                    companyId = it,
                    dataType = testDataType,
                    reportingPeriod = testReportingPeriod,
                ).first()
        }
    }

    @Test
    fun `verify that a document collector or data extractor can see all data sourcing objects assigned to them`() {
        val companyIdDocumentCollectorOrDataExtractor =
            createNewCompanyAndReturnId()
        val companyId1 =
            createNewCompanyAndReturnId()
        val companyId2 =
            createNewCompanyAndReturnId()

        val requestId1 = createRequest(companyId1)
        val requestId2 = createRequest(companyId2)

        lateinit var storedDataSourcings: List<StoredDataSourcing>

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            storedDataSourcings =
                patchOpenRequestsToProcessingAndReturnInitializedStoredDataSourcings(
                    listOf(requestId1, requestId2),
                    listOf(companyId1, companyId2),
                )
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcings[0].dataSourcingId,
                DataSourcingState.DocumentSourcing,
            )
            apiAccessor.dataSourcingControllerApi.patchProviderAndAdminComment(
                storedDataSourcings[0].dataSourcingId,
                companyIdDocumentCollectorOrDataExtractor,
            )
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcings[1].dataSourcingId,
                DataSourcingState.DataExtraction,
            )
            apiAccessor.dataSourcingControllerApi.patchProviderAndAdminComment(
                storedDataSourcings[1].dataSourcingId,
                null,
                companyIdDocumentCollectorOrDataExtractor,
            )
        }

        val updatedDataSourcingObjects =
            apiAccessor.dataSourcingControllerApi
                .getDataSourcingForCompanyId(companyIdDocumentCollectorOrDataExtractor)
        assertEquals(2, updatedDataSourcingObjects.size)
        assertEquals(
            storedDataSourcings.map { it.dataSourcingId }.toSet(),
            updatedDataSourcingObjects.map { it.dataSourcingId }.toSet(),
        )
    }

    @Test
    fun `verify that historization works for repeated data sourcing workflows for the same data dimensions`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcing.dataSourcingId,
                DataSourcingState.Done,
            )
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcing.dataSourcingId,
                DataSourcingState.Initialized,
            )
        }
        apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
            storedDataSourcing.dataSourcingId,
            DataSourcingState.DocumentSourcing,
        )

        val dataSourcingHistory =
            apiAccessor.dataSourcingControllerApi.getDataSourcingHistoryById(storedDataSourcing.dataSourcingId)
        assertEquals(4, dataSourcingHistory.size)
        assertEquals(DataSourcingState.Initialized, dataSourcingHistory[0].state)
        assertEquals(DataSourcingState.Done, dataSourcingHistory[1].state)
        assertEquals(DataSourcingState.Initialized, dataSourcingHistory[2].state)
        assertEquals(DataSourcingState.DocumentSourcing, dataSourcingHistory[3].state)
    }

    @Test
    fun `verify that a nonexisting document ID can not be added to a data sourcing object`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val nonExistingDocumentId = "nonExistingDocumentId"
        val exception =
            assertThrows<ClientException> {
                apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
                    storedDataSourcing.dataSourcingId,
                    setOf(nonExistingDocumentId),
                    true,
                )
            }
        assertEquals(404, exception.statusCode)
    }

    @Test
    fun `verify that documents of an existing data sourcing object can be patched`() {
        val testDocumentIds = documentControllerApiAccessor.uploadAllTestDocumentsAndAssurePersistence()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        verifyDataSourcingDocuments(storedDataSourcing.dataSourcingId, emptySet())
        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            storedDataSourcing.dataSourcingId,
            setOf(testDocumentIds.first()),
            true,
        )
        verifyDataSourcingDocuments(storedDataSourcing.dataSourcingId, setOf(testDocumentIds.first()))

        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            storedDataSourcing.dataSourcingId,
            testDocumentIds.take(3).toSet(),
            true,
        )
        verifyDataSourcingDocuments(storedDataSourcing.dataSourcingId, testDocumentIds.take(3).toSet())

        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            storedDataSourcing.dataSourcingId,
            testDocumentIds.takeLast(2).toSet(),
            false,
        )
        verifyDataSourcingDocuments(storedDataSourcing.dataSourcingId, testDocumentIds.takeLast(2).toSet())
    }

    @Test
    fun `verify that data sourcing attempt dates can be uploaded and lead to a historization`() {
        val firstDate = LocalDate.now()
        val secondDate = firstDate.plusMonths(6)
        val thirdDate = secondDate.minusMonths(2)
        val fourthDate = thirdDate.plusYears(4)

        for (date in listOf(firstDate, secondDate)) {
            apiAccessor.dataSourcingControllerApi.patchDateOfNextDocumentSourcingAttempt(storedDataSourcing.dataSourcingId, date)
        }
        apiAccessor.dataSourcingControllerApi.getDataSourcingHistoryById(storedDataSourcing.dataSourcingId).let {
            assertEquals(3, it.size)
            assertEquals(null, it[0].dateOfNextDocumentSourcingAttempt)
            assertEquals(firstDate, it[1].dateOfNextDocumentSourcingAttempt)
            assertEquals(secondDate, it[2].dateOfNextDocumentSourcingAttempt)
        }

        for (date in listOf(thirdDate, fourthDate)) {
            apiAccessor.dataSourcingControllerApi.patchDateOfNextDocumentSourcingAttempt(storedDataSourcing.dataSourcingId, date)
        }
        apiAccessor.dataSourcingControllerApi.getDataSourcingHistoryById(storedDataSourcing.dataSourcingId).let {
            assertEquals(5, it.size)
            assertEquals(thirdDate, it[3].dateOfNextDocumentSourcingAttempt)
            assertEquals(fourthDate, it[4].dateOfNextDocumentSourcingAttempt)
        }
    }
}
