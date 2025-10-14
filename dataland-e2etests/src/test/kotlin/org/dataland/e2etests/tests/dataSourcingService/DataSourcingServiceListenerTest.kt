package org.dataland.e2etests.tests.dataSourcingService

import org.dataland.dataSourcingService.openApiClient.model.DataSourcingState
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.api.ApiAwait
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.Thread.sleep

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataSourcingServiceListenerTest : DataSourcingTest() {
    private fun uploadDummyDataForDataSourcingObject(): String =
        apiAccessor
            .uploadDummyFrameworkDataset(
                storedDataSourcing.companyId,
                DataTypeEnum.valueOf(storedDataSourcing.dataType),
                storedDataSourcing.reportingPeriod,
                false,
            ).dataId

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
    fun `upload a dataset and verify that the state of the corresponding data sourcing object is set to DataVerification`() {
        uploadDummyDataForDataSourcingObject()

        ApiAwait.waitForData(
            supplier = { apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.dataSourcingId).state },
            condition = { it == DataSourcingState.DataVerification },
        )
    }

    @Test
    fun `accept a dataset and verify that the state of the corresponding data sourcing object is set to Answered`() {
        val datasetId = uploadDummyDataForDataSourcingObject()
        apiAccessor.qaServiceControllerApi.changeQaStatus(datasetId, QaStatus.Accepted)

        ApiAwait.waitForData(
            supplier = { apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.dataSourcingId) },
            condition = { it.state == DataSourcingState.Done },
        )
    }

    @Test
    fun `verify that the state Answered of a data sourcing object cannot be changed by data uploads`() {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingControllerApi.patchDataSourcingState(
                storedDataSourcing.dataSourcingId,
                DataSourcingState.Done,
            )
        }
        uploadDummyDataForDataSourcingObject()
        sleep(2000) // make sure events are processed throughout the system before proceeding
        val updatedDataSourcingObject = apiAccessor.dataSourcingControllerApi.getDataSourcingById(storedDataSourcing.dataSourcingId)
        assertEquals(
            DataSourcingState.Done,
            updatedDataSourcingObject.state,
        )
    }
}
