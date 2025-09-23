package org.dataland.e2etests.tests.dataSourcingService

import org.dataland.dataSourcingService.openApiClient.infrastructure.ClientException
import org.dataland.dataSourcingService.openApiClient.model.DataSourcingState
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.SingleRequest
import org.dataland.dataSourcingService.openApiClient.model.StoredDataSourcing
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.GlobalAuth.jwtHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.lang.Thread.sleep
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataSourcingControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentControllerApiAccessor = DocumentControllerApiAccessor()

    private val testDataType = "sfdr"
    private val testReportingPeriod = "2023"

    private lateinit var storedDataSourcing: StoredDataSourcing

    private fun assertResourceNotFoundException(function: () -> Unit) {
        val exception =
            assertThrows<ClientException> {
                function()
            }
        assertEquals("Client error : 404 ", exception.message)
    }

    private fun createRequest(
        companyId: String,
        dataType: String = testDataType,
        reportingPeriod: String = testReportingPeriod,
        comment: String = "test request",
        user: TechnicalUser = TechnicalUser.Reader,
    ): String {
        val request =
            SingleRequest(companyId, dataType, setOf(reportingPeriod), comment)
        return GlobalAuth.withTechnicalUser(user) {
            apiAccessor.dataSourcingRequestControllerApi
                .createRequest(request)
                .idsOfStoredRequests
                .first()
        }
    }

    private fun uploadDummyDataForDataSourcingObject(): String =
        apiAccessor
            .uploadDummyFrameworkDataset(
                storedDataSourcing.companyId,
                DataTypeEnum.valueOf(storedDataSourcing.dataType),
                storedDataSourcing.reportingPeriod,
            ).dataId

    private fun verifyDataSourcingDocuments(
        dataSourcingObjectId: String,
        expectedDocuments: Set<String>,
    ) {
        val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObjectId)
        assertEquals(expectedDocuments, updatedDataSourcingObject.documentIds)
    }

    private fun changeSourcingStatusAndVerifyRequestsStatuses(
        newSourcingStatus: DataSourcingState,
        expectedRequestState: RequestState,
    ) {
        createRequest(storedDataSourcing.companyId, user = TechnicalUser.PremiumUser)
        createRequest(storedDataSourcing.companyId, user = TechnicalUser.Admin)

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.id, newSourcingStatus)
        }

        val updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(3, updatedSourcingObject.associatedRequestIds?.size ?: 0)
        updatedSourcingObject.associatedRequestIds?.forEach {
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
    fun initializeDataSourcing() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val companyId =
            GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
                apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
            }

        val requestId = createRequest(companyId)
        assertResourceNotFoundException {
            apiAccessor.dataSourcingControllerApi.getDataSourcingByDimensions(companyId, testDataType, testReportingPeriod)
        }

        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Processing)
        storedDataSourcing = apiAccessor.dataSourcingControllerApi.getDataSourcingByDimensions(companyId, testDataType, testReportingPeriod)

        assertEquals(DataSourcingState.Initialized, storedDataSourcing.state)
        assertEquals(setOf(requestId), storedDataSourcing.associatedRequestIds)
    }

    @Test
    fun `verify that a data sourcing object can be retrieved using its ID`() {
        val dataSourcingObjectById = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(storedDataSourcing, dataSourcingObjectById)
    }

    @Test
    fun `verify that a request is appended to the corresponding sourcing object only when set to 'Processing'`() {
        val newRequest =
            GlobalAuth.withTechnicalUser(TechnicalUser.PremiumUser) {
                createRequest(
                    companyId = storedDataSourcing.companyId,
                    dataType = storedDataSourcing.dataType,
                    reportingPeriod = storedDataSourcing.reportingPeriod,
                    comment = "Second request",
                )
            }

        var updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(storedDataSourcing.associatedRequestIds, updatedSourcingObject.associatedRequestIds)

        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(newRequest, RequestState.Processing)
        updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(storedDataSourcing.associatedRequestIds?.plus(newRequest), updatedSourcingObject.associatedRequestIds)
    }

    @Test
    fun `upload a dataset and verify that the corresponding data sourcing object's state is set to 'DataVerification'`() {
        uploadDummyDataForDataSourcingObject()

        ApiAwait.waitForData(
            supplier = { apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id).state },
            condition = { it == DataSourcingState.DataVerification },
        )
    }

    @Test
    fun `accept a dataset and verify that the corresponding data sourcing object's state is set to 'Answered'`() {
        val dataSetId = uploadDummyDataForDataSourcingObject()

        ApiAwait.waitForData { apiAccessor.qaServiceControllerApi.changeQaStatus(dataSetId, QaStatus.Accepted) }
        ApiAwait.waitForData(
            supplier = { apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id).state },
            condition = { it == DataSourcingState.Answered },
        )
    }

    @Test
    fun `verify that a data sourcing object's state 'Answered' can not be changed by data uploads`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.id, DataSourcingState.Answered)
        }
        uploadDummyDataForDataSourcingObject()
        sleep(2000)
        val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(
            DataSourcingState.Answered,
            updatedDataSourcingObject.state,
        )
    }

    @Test
    fun `verify that only marking a sourcing process as 'Answered' or 'NonSourceable' will patch requests to 'Processed'`() {
        for (state in DataSourcingState.entries) {
            changeSourcingStatusAndVerifyRequestsStatuses(
                state,
                if (state == DataSourcingState.Answered || state == DataSourcingState.NonSourceable) {
                    RequestState.Processed
                } else {
                    RequestState.Processing
                },
            )
        }
    }

    @Test
    fun `reopen a processed request and verify that the data sourcing process is started anew`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.id, DataSourcingState.Answered)
        }

        val requestId = storedDataSourcing.associatedRequestIds?.first() ?: ""
        val request = apiAccessor.dataSourcingRequestControllerApi.getRequest(requestId)
        assertEquals(RequestState.Processed, request.state)

        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Open)
        val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(DataSourcingState.Initialized, updatedDataSourcingObject.state)
    }

    @Test
    fun `verify that a data collector can only patch a state from 'DocumentSourcing' to 'DocumentSourcingDone'`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        DataSourcingState.entries.forEach { initialState ->
            DataSourcingState.entries.forEach { finalState ->
                if (initialState == finalState) {
                    return@forEach
                }
                GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                    apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.id, initialState)
                }
                if (initialState == DataSourcingState.DocumentSourcing && finalState == DataSourcingState.DocumentSourcingDone) {
                    apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.id, finalState)
                    val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
                    assertEquals(finalState, updatedDataSourcingObject.state)
                } else {
                    assertThrows<ClientException> {
                        apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.id, finalState)
                    }
                }
            }
        }
    }

    @Test
    fun `verify that historization works for repeated data sourcing workflows for the same data dimensions`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.id, DataSourcingState.Answered)
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.id, DataSourcingState.Initialized)
        }
        apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.id, DataSourcingState.DocumentSourcing)

        val dataSourcingHistory = apiAccessor.dataSourcingControllerApi.getDataSourcingHistoryById(storedDataSourcing.id)
        assertEquals(4, dataSourcingHistory.size)
        assertEquals(DataSourcingState.Initialized, dataSourcingHistory[0].state)
        assertEquals(DataSourcingState.Answered, dataSourcingHistory[1].state)
        assertEquals(DataSourcingState.Initialized, dataSourcingHistory[2].state)
        assertEquals(DataSourcingState.DocumentSourcing, dataSourcingHistory[3].state)
    }

    @Test
    fun `verify that a non-existing document ID can not be added to a data sourcing object`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val nonExistingDocumentId = "nonExistingDocumentId"
        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            storedDataSourcing.id,
            setOf(nonExistingDocumentId),
            true,
        )
    }

    @Test
    fun `verify that documents of an existing data sourcing object can be patched`() {
        val testDocumentIds = documentControllerApiAccessor.uploadAllTestDocumentsAndAssurePersistence()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        verifyDataSourcingDocuments(storedDataSourcing.id, emptySet())
        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            storedDataSourcing.id,
            setOf(testDocumentIds.first()),
            true,
        )
        verifyDataSourcingDocuments(storedDataSourcing.id, setOf(testDocumentIds.first()))

        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            storedDataSourcing.id,
            testDocumentIds.take(3).toSet(),
            true,
        )
        verifyDataSourcingDocuments(storedDataSourcing.id, testDocumentIds.take(3).toSet())

        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            storedDataSourcing.id,
            testDocumentIds.takeLast(2).toSet(),
            false,
        )
        verifyDataSourcingDocuments(storedDataSourcing.id, testDocumentIds.takeLast(2).toSet())
    }

    @Test
    fun `verify that data sourcing attempt dates can be uploaded and lead to a historization`() {
        val firstDate = LocalDate.of(2024, 1, 1)
        val secondDate = LocalDate.of(2024, 6, 1)
        val thirdDate = LocalDate.of(2023, 3, 3)
        val fouthDate = LocalDate.of(2027, 1, 1)

        apiAccessor.dataSourcingControllerApi.patchDateDocumentSourcingAttempt(
            storedDataSourcing.id,
            setOf(firstDate, secondDate),
        )
        val dataSourcingHistory = apiAccessor.dataSourcingControllerApi.getDataSourcingHistoryById(storedDataSourcing.id)
        assertEquals(3, dataSourcingHistory.size)
        assertEquals(null, dataSourcingHistory[0].dateDocumentSourcingAttempt)
        assertEquals(firstDate, dataSourcingHistory[1].dateDocumentSourcingAttempt)
        assertEquals(secondDate, dataSourcingHistory[2].dateDocumentSourcingAttempt)

        apiAccessor.dataSourcingControllerApi.patchDateDocumentSourcingAttempt(
            storedDataSourcing.id,
            setOf(thirdDate, fouthDate),
        )
        assertEquals(5, dataSourcingHistory.size)
        assertEquals(thirdDate, dataSourcingHistory[3].dateDocumentSourcingAttempt)
        assertEquals(fouthDate, dataSourcingHistory[4].dateDocumentSourcingAttempt)
    }
}
