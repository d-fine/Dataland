package org.dataland.e2etests.tests.dataSourcingService

import org.dataland.dataSourcingService.openApiClient.infrastructure.ClientException
import org.dataland.dataSourcingService.openApiClient.model.DataSourcingState
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.SingleRequest
import org.dataland.dataSourcingService.openApiClient.model.StoredDataSourcing
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.lang.Thread.sleep
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataSourcingControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentControllerApiAccessor = DocumentControllerApiAccessor()

    private val testDataType = "sfdr"
    private val testReportingPeriod = "2023"

    private lateinit var storedDataSourcing: StoredDataSourcing

    private fun assertForbiddenException(function: () -> Unit) {
        val exception =
            assertThrows<ClientException> {
                function()
            }
        assertEquals("Client error : 403 ", exception.message)
    }

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
        val request = SingleRequest(companyId, dataType, reportingPeriod, comment)
        return GlobalAuth.withTechnicalUser(user) {
            apiAccessor.dataSourcingRequestControllerApi.createRequest(request).id
        }
    }

    private fun uploadDummyDataForDataSourcingObject(): String =
        apiAccessor
            .uploadDummyFrameworkDataset(
                storedDataSourcing.companyId,
                DataTypeEnum.valueOf(storedDataSourcing.dataType),
                storedDataSourcing.reportingPeriod,
                false,
            ).dataId

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
            // requests are only linked to data sourcing objects after having been validated by the QUARG Team and set to Processing
            apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId1, RequestState.Processing)
            apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId2, RequestState.Processing)
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(storedDataSourcing.id, newSourcingStatus)
        }

        val updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(3, updatedSourcingObject.associatedRequestIds.size)
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
    fun initializeDataSourcing() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val companyId =
            createNewCompanyAndReturnId()

        val requestId = createRequest(companyId)

        GlobalAuth.withTechnicalUser(TechnicalUser.Reader) {
            assertForbiddenException {
                apiAccessor.dataSourcingControllerApi.getDataSourcingByDimensions(
                    companyId,
                    testDataType,
                    testReportingPeriod,
                )
            }
        }

        assertResourceNotFoundException {
            apiAccessor.dataSourcingControllerApi.getDataSourcingByDimensions(
                companyId,
                testDataType,
                testReportingPeriod,
            )
        }
        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Processing)
        storedDataSourcing =
            apiAccessor.dataSourcingControllerApi.getDataSourcingByDimensions(
                companyId,
                testDataType,
                testReportingPeriod,
            )

        assertEquals(DataSourcingState.Initialized, storedDataSourcing.state)
        assertEquals(setOf(requestId), storedDataSourcing.associatedRequestIds)
    }

    @Test
    fun `verify that a data sourcing object can be retrieved using its ID`() {
        val dataSourcingObjectById =
            apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
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

        var updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(storedDataSourcing.associatedRequestIds, updatedSourcingObject.associatedRequestIds)

        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(newRequest, RequestState.Processing)
        updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(
            storedDataSourcing.associatedRequestIds.plus(newRequest),
            updatedSourcingObject.associatedRequestIds,
        )
    }

    @Test
    fun `upload a dataset and verify that the state of the corresponding data sourcing object is set to DataVerification`() {
        uploadDummyDataForDataSourcingObject()

        ApiAwait.waitForData(
            supplier = { apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id).state },
            condition = { it == DataSourcingState.DataVerification },
        )
    }

    @Test
    fun `accept a dataset and verify that the state of the corresponding data sourcing object is set to Answered`() {
        val dataSetId = uploadDummyDataForDataSourcingObject()

        ApiAwait.waitForData { apiAccessor.qaServiceControllerApi.changeQaStatus(dataSetId, QaStatus.Accepted) }
        ApiAwait.waitForData(
            timeoutInSeconds = 10,
            supplier = { apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id).state },
            condition = { it == DataSourcingState.Answered },
        )
    }

    @Test
    fun `verify that the state Answered of a data sourcing object cannot be changed by data uploads`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcing.id,
                DataSourcingState.Answered,
            )
        }
        uploadDummyDataForDataSourcingObject()
        sleep(2000) // make sure events are processed throughout the system before proceeding
        val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(
            DataSourcingState.Answered,
            updatedDataSourcingObject.state,
        )
    }

    @ParameterizedTest
    @EnumSource(DataSourcingState::class)
    fun `verify that only marking a sourcing process as Answered or NonSourceable will patch requests to Processed`(
        state: DataSourcingState,
    ) {
        val expectedStatus =
            if (state == DataSourcingState.Answered || state == DataSourcingState.NonSourceable) {
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
                storedDataSourcing.id,
                DataSourcingState.Answered,
            )
        }

        val requestId = storedDataSourcing.associatedRequestIds.first()
        val request = apiAccessor.dataSourcingRequestControllerApi.getRequest(requestId)
        assertEquals(RequestState.Processed, request.state)

        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Open)
        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Processing)
        val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
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
                storedDataSourcing.id,
                DataSourcingState.DocumentSourcing,
            )
            apiAccessor.dataSourcingControllerApi.patchDocumentCollectorAndDataExtractor(
                storedDataSourcing.id,
                companyIdCollector,
            )
        }
        var updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.id)
        assertEquals(companyIdCollector, updatedDataSourcingObject.documentCollector)
        assertEquals(null, updatedDataSourcingObject.dataExtractor)
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcing.id,
                DataSourcingState.DataExtraction,
            )
            updatedDataSourcingObject =
                apiAccessor.dataSourcingControllerApi.patchDocumentCollectorAndDataExtractor(
                    storedDataSourcing.id,
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
            apiAccessor.dataSourcingControllerApi.getDataSourcingByDimensions(
                it,
                testDataType,
                testReportingPeriod,
            )
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
                storedDataSourcings[0].id,
                DataSourcingState.DocumentSourcing,
            )
            apiAccessor.dataSourcingControllerApi.patchDocumentCollectorAndDataExtractor(
                storedDataSourcings[0].id,
                companyIdDocumentCollectorOrDataExtractor,
            )
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcings[1].id,
                DataSourcingState.DataExtraction,
            )
            apiAccessor.dataSourcingControllerApi.patchDocumentCollectorAndDataExtractor(
                storedDataSourcings[1].id,
                null,
                companyIdDocumentCollectorOrDataExtractor,
            )
        }

        val updatedDataSourcingObjects =
            apiAccessor.dataSourcingControllerApi
                .getDataSourcingForCompanyId(companyIdDocumentCollectorOrDataExtractor)
        assertEquals(2, updatedDataSourcingObjects.size)
        assertEquals(
            storedDataSourcings.map { it.id }.toSet(),
            updatedDataSourcingObjects.map { it.id }.toSet(),
        )
    }

    @Test
    fun `verify that historization works for repeated data sourcing workflows for the same data dimensions`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcing.id,
                DataSourcingState.Answered,
            )
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcing.id,
                DataSourcingState.Initialized,
            )
        }
        apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
            storedDataSourcing.id,
            DataSourcingState.DocumentSourcing,
        )

        val dataSourcingHistory =
            apiAccessor.dataSourcingControllerApi.getDataSourcingHistoryById(storedDataSourcing.id)
        assertEquals(4, dataSourcingHistory.size)
        assertEquals(DataSourcingState.Initialized, dataSourcingHistory[0].state)
        assertEquals(DataSourcingState.Answered, dataSourcingHistory[1].state)
        assertEquals(DataSourcingState.Initialized, dataSourcingHistory[2].state)
        assertEquals(DataSourcingState.DocumentSourcing, dataSourcingHistory[3].state)
    }

    @Test
    fun `verify that a nonexisting document ID can not be added to a data sourcing object`() {
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
        val firstDate = LocalDate.now()
        val secondDate = firstDate.plusMonths(6)
        val thirdDate = secondDate.minusMonths(2)
        val fourthDate = thirdDate.plusYears(4)

        for (date in listOf(firstDate, secondDate)) {
            apiAccessor.dataSourcingControllerApi.patchDateOfNextDocumentSourcingAttempt(storedDataSourcing.id, date)
        }
        apiAccessor.dataSourcingControllerApi.getDataSourcingHistoryById(storedDataSourcing.id).let {
            assertEquals(3, it.size)
            assertEquals(null, it[0].dateOfNextDocumentSourcingAttempt)
            assertEquals(firstDate, it[1].dateOfNextDocumentSourcingAttempt)
            assertEquals(secondDate, it[2].dateOfNextDocumentSourcingAttempt)
        }

        for (date in listOf(thirdDate, fourthDate)) {
            apiAccessor.dataSourcingControllerApi.patchDateOfNextDocumentSourcingAttempt(storedDataSourcing.id, date)
        }
        apiAccessor.dataSourcingControllerApi.getDataSourcingHistoryById(storedDataSourcing.id).let {
            assertEquals(5, it.size)
            assertEquals(thirdDate, it[3].dateOfNextDocumentSourcingAttempt)
            assertEquals(fourthDate, it[4].dateOfNextDocumentSourcingAttempt)
        }
    }

    /* We do not think that we want to prohibit adding past dates in case the data provider forgot entering one.
    @Test
    fun `verify that data sourcing attempts cannot be specified in the past`() {
        assertThrows<ClientException> {
            apiAccessor.dataSourcingControllerApi.patchDateDocumentSourcingAttempt(
                storedDataSourcing.id,
                LocalDate.now().minusDays(1),
            )
        }
    }
     */

    private fun createNewCompanyAndReturnId(): String =
        GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        }
}
