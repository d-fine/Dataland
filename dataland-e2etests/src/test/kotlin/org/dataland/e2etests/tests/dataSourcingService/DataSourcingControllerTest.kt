package org.dataland.e2etests.tests.dataSourcingService

import org.dataland.dataSourcingService.openApiClient.infrastructure.ClientException
import org.dataland.dataSourcingService.openApiClient.model.DataSourcingResponse
import org.dataland.dataSourcingService.openApiClient.model.DataSourcingState
import org.dataland.dataSourcingService.openApiClient.model.RequestState
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataSourcingControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentControllerApiAccessor = DocumentControllerApiAccessor()

    private val testDataType = "sfdr"
    private val testReportingPeriod = "2023"

    private lateinit var dataSourcingObject: DataSourcingResponse

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
            org.dataland.dataSourcingService.openApiClient.model
                .DataRequest(companyId, dataType, reportingPeriod, comment)
        return GlobalAuth.withTechnicalUser(user) {
            apiAccessor.dataSourcingRequestControllerApi.createRequest(request).id
        }
    }

    private fun uploadDummyDataForDataSourcingObject(): String =
        apiAccessor
            .uploadDummyFrameworkDataset(
                dataSourcingObject.companyId,
                DataTypeEnum.valueOf(dataSourcingObject.dataType),
                dataSourcingObject.reportingPeriod,
            ).dataId

    private fun verifyDataSourcingDocuments(
        dataSourcingObjectId: String,
        expectedDocuments: Set<String>,
    ) {
        val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObjectId)
        assertEquals(expectedDocuments, updatedDataSourcingObject.documentIds)
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

        apiAccessor.dataSourcingRequestControllerApi.patchDataRequestState(requestId, RequestState.Processing)
        dataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingByDimensions(companyId, testDataType, testReportingPeriod)

        assertEquals(DataSourcingState.Initialized, DataSourcingState.valueOf(dataSourcingObject.state))
        assertEquals(setOf(requestId), dataSourcingObject.associatedRequestIds)
    }

    @Test
    fun `verify that a data sourcing object can be retrieved using its ID`() {
        val dataSourcingObjectById = apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObject.id)
        assertEquals(dataSourcingObject, dataSourcingObjectById)
    }

    @Test
    fun `post a request for data dimensions with existing sourcing object and verify the request is appended to the sourcing object`() {
        val newRequest =
            GlobalAuth.withTechnicalUser(TechnicalUser.PremiumUser) {
                createRequest(
                    companyId = dataSourcingObject.companyId,
                    dataType = dataSourcingObject.dataType,
                    reportingPeriod = dataSourcingObject.reportingPeriod,
                    comment = "Second request",
                )
            }

        val updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObject.id)
        assertEquals(dataSourcingObject.associatedRequestIds?.plus(newRequest), updatedSourcingObject.associatedRequestIds)
    }

    @Test
    fun `upload a dataset and verify that the corresponding data sourcing object's state is set to 'DataVerification'`() {
        uploadDummyDataForDataSourcingObject()

        ApiAwait.waitForData(
            supplier = { apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObject.id).state },
            condition = { DataSourcingState.valueOf(it) == DataSourcingState.DataVerification },
        )
    }

    @Test
    fun `accept a dataset and verify that the corresponding data sourcing object's state is set to 'Answered'`() {
        val dataSetId = uploadDummyDataForDataSourcingObject()

        ApiAwait.waitForData { apiAccessor.qaServiceControllerApi.changeQaStatus(dataSetId, QaStatus.Accepted) }
        ApiAwait.waitForData(
            supplier = { apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObject.id).state },
            condition = { DataSourcingState.valueOf(it) == DataSourcingState.Answered },
        )
    }

    @Test
    fun `verify that a data sourcing object's state 'Answered' can not be changed by data uploads`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(dataSourcingObject.id, DataSourcingState.Answered)
        }
        uploadDummyDataForDataSourcingObject()
        sleep(2000)
        assertEquals(
            DataSourcingState.Answered,
            apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObject.id),
        )
    }

    @Test
    fun `mark a sourcing process as 'Answered' and verify that all associated requests are now 'Processed'`() {
        createRequest(dataSourcingObject.companyId, user = TechnicalUser.PremiumUser)
        createRequest(dataSourcingObject.companyId, user = TechnicalUser.Admin)

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(dataSourcingObject.id, DataSourcingState.Answered)
        }

        val updatedSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObject.id)
        assertEquals(3, updatedSourcingObject.associatedRequestIds?.size)
        updatedSourcingObject.associatedRequestIds.forEach {
            val request = apiAccessor.dataSourcingRequestControllerApi.getRequest(it)
            assertEquals(RequestState.Processed, request.state)
        }
    }

    @Test
    fun `reopen a processed request and verify that the data sourcing process is started anew`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(dataSourcingObject.id, DataSourcingState.Answered)
        }

        val requestId = dataSourcingObject.associatedRequestIds.first()
        val request = apiAccessor.dataSourcingRequestControllerApi.getRequest(requestId)
        assertEquals(RequestState.Processed, request.state)

        apiAccessor.dataSourcingRequestControllerApi.patchDataRequestState(requestId, RequestState.Open)
        val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObject.id)
        assertEquals(DataSourcingState.Initialized, updatedDataSourcingObject.state)
    }

    @Test
    fun `verify that a data collector can patch a state to 'DocumentSourcingDone' but no other state`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        DataSourcingState.entries.forEach { initialState ->
            DataSourcingState.entries.forEach { finalState ->
                if (initialState == finalState) {
                    return@forEach
                }
                GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                    apiAccessor.dataSourcingControllerApi.patchDataSourcingState(dataSourcingObject.id, initialState)
                }
                if (initialState == DataSourcingState.DocumentSourcing && finalState == DataSourcingState.DocumentSourcingDone) {
                    apiAccessor.dataSourcingControllerApi.patchDataSourcingState(dataSourcingObject.id, finalState)
                    val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingObject.id)
                    assertEquals(finalState, DataSourcingState.valueOf(updatedDataSourcingObject.state))
                } else {
                    assertThrows<ClientException> {
                        apiAccessor.dataSourcingControllerApi.patchDataSourcingState(dataSourcingObject.id, finalState)
                    }
                }
            }
        }
    }

    @Test
    fun `verify that historization works for repeated data sourcing workflows for the same data dimensions`() {
        assert(false, { "Not yet implemented" })
    }

    @Test
    fun `verify that a non-existing document ID can not be added to a data sourcing object`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val nonExistingDocumentId = "nonExistingDocumentId"
        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            dataSourcingObject.id,
            setOf(nonExistingDocumentId),
            true,
        )
    }

    @Test
    fun `verify that documents of an existing data sourcing object can be patched`() {
        val testDocumentIds = documentControllerApiAccessor.uploadAllTestDocumentsAndAssurePersistence()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        verifyDataSourcingDocuments(dataSourcingObject.id, emptySet())
        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            dataSourcingObject.id,
            setOf(testDocumentIds.first()),
            true,
        )
        verifyDataSourcingDocuments(dataSourcingObject.id, setOf(testDocumentIds.first()))

        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            dataSourcingObject.id,
            testDocumentIds.take(3).toSet(),
            true,
        )
        verifyDataSourcingDocuments(dataSourcingObject.id, testDocumentIds.take(3).toSet())

        apiAccessor.dataSourcingControllerApi.patchDataSourcingDocuments(
            dataSourcingObject.id,
            testDocumentIds.takeLast(2).toSet(),
            false,
        )
        verifyDataSourcingDocuments(dataSourcingObject.id, testDocumentIds.takeLast(2).toSet())
    }

    @Test
    fun `verify that data sourcing attempt dates can be uploaded and lead to a historization`() {
        assert(false, { "Not yet implemented" })
    }
}
