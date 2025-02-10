package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.dataland.e2etests.utils.testDataProvivders.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InvalidSfdrRequestTests {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    private val errorCode400 = "Client error : 400"
    private val errorMessage = "Input validation failed."

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    fun getErrorFromApi(companyName: String): ClientException {
        val oneInvalidSfdrDataset =
            FrameworkTestDataProvider
                .forFrameworkPreparedFixtures(SfdrData::class.java)
                .getByCompanyName(companyName)
        Assertions.assertNotNull(oneInvalidSfdrDataset)
        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        val errorForInvalidInput =
            assertThrows<ClientException> {
                apiAccessor.sfdrUploaderFunction(
                    companyInformation.actualStoredCompany.companyId,
                    oneInvalidSfdrDataset!!.t,
                    "",
                )
            }
        return errorForInvalidInput
    }

    @Test
    fun `post a company with invalid Sfdr currency data`() {
        val errorForInvalidInput = getErrorFromApi("Sfdr-dataset-with-invalid-currency-input")
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>)
                .body!!
                .toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with invalid negative extended data point BigDecimal`() {
        val errorForInvalidInput = getErrorFromApi("Sfdr-dataset-with-invalid-negative-big-decimal-input")
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>)
                .body!!
                .toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with invalid negative extended data point long`() {
        val errorForInvalidInput = getErrorFromApi("Sfdr-dataset-with-invalid-negative-long-input")
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>)
                .body!!
                .toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with invalid percentage value`() {
        val errorForInvalidInput = getErrorFromApi("Sfdr-dataset-with-invalid-percentage-input")
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>)
                .body!!
                .toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with two invalid inputs`() {
        val errorForInvalidInput = getErrorFromApi("Sfdr-dataset-with-two-invalid-inputs")
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>)
                .body!!
                .toString()
                .contains(errorMessage),
        )
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>)
                .body!!
                .toString()
                .contains("rateOfAccidents"),
        )
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>)
                .body!!
                .toString()
                .contains("reportedConvictionsOfBriberyAndCorruption"),
        )
    }

    @Test
    fun `post a company with empty string document reference`() {
        val errorForInvalidInput = getErrorFromApi("Sfdr-dataset-with-empty-string-document-reference")
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
    }
}
