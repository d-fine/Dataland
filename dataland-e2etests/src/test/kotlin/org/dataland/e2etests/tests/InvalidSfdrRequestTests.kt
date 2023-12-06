package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvalidSfdrRequestTests {
    private val apiAccessor = ApiAccessor()
    val errorCode400 = "Client error : 400"
    val errorMessage = "Input validation failed."

    fun getErrorFromApi(companyName: String): ClientException {
        val oneInvalidSfdrDataset = apiAccessor.testDataProviderForSfdrData
            .getSpecificCompanyByNameFromPreparedFixtures(companyName)
        Assertions.assertNotNull(oneInvalidSfdrDataset)
        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        val errorForInvalidInput = assertThrows<ClientException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
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
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with invalid negative extended data point BigDecimal`() {
        val errorForInvalidInput = getErrorFromApi(
            "Sfdr-dataset-with-invalid" +
                "-negative-big-decimal-input",
        )
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with invalid negative extended data point long`() {
        val errorForInvalidInput = getErrorFromApi("Sfdr-dataset-with-invalid-negative-long-input")
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains(errorMessage),
        )
    }

    @Disabled // enable once the @field:NotBlank annotation is back in the document reference file
    @Test
    fun `post a company with invalid percentage value`() {
        val errorForInvalidInput = getErrorFromApi("Sfdr-dataset-with-invalid-percentage-input")
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with two invalid inputs`() {
        val errorForInvalidInput = getErrorFromApi("Sfdr-dataset-with-two-invalid-inputs")
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains(errorMessage),
        )
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains("rateOfAccidentsInPercent"),
        )
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains("reportedConvictionsOfBriberyAndCorruption"),
        )
    }

    /*@Test
    fun `post a company with empty string document reference`() {
        val errorForInvalidInput = getErrorFromApi("Sfdr-dataset-with-empty-string-document-reference")
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
    }*/
}
