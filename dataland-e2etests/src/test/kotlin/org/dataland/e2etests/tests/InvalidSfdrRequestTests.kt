package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvalidSfdrRequestTests {
    private val apiAccessor = ApiAccessor()
    val errorCode400 = "Client error : 400"
    val errorMessage = "Input failed the input validation"

    @Test
    fun `post a company with invalid Sfdr currency data`() {
        val oneInvalidSfdrDataset = apiAccessor.testDataProviderForSfdrData
            .getSpecificCompanyByNameFromPreparedFixtures("Sfdr-dataset-with-invalid-currency-input")
        Assertions.assertNotNull(oneInvalidSfdrDataset)
        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        val errorForInvalidInput = assertThrows<ClientException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
                "",
            )
        }
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with invalid negative extended data point BigDecimal`() {
        val oneInvalidSfdrDataset = apiAccessor.testDataProviderForSfdrData
            .getSpecificCompanyByNameFromPreparedFixtures(
                "Sfdr-dataset-with-invalid" +
                    "-negative-big-decimal-input",
            )
        Assertions.assertNotNull(oneInvalidSfdrDataset)
        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        val errorForInvalidInput = assertThrows<ClientException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
                "",
            )
        }
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with invalid negative extended data point long`() {
        val oneInvalidSfdrDataset = apiAccessor.testDataProviderForSfdrData
            .getSpecificCompanyByNameFromPreparedFixtures("Sfdr-dataset-with-invalid-negative-long-input")
        Assertions.assertNotNull(oneInvalidSfdrDataset)
        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        val errorForInvalidInput = assertThrows<ClientException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
                "",
            )
        }
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with invalid percentage value`() {
        val oneInvalidSfdrDataset = apiAccessor.testDataProviderForSfdrData
            .getSpecificCompanyByNameFromPreparedFixtures("Sfdr-dataset-with-invalid-percentage-input")
        Assertions.assertNotNull(oneInvalidSfdrDataset)
        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        val errorForInvalidInput = assertThrows<ClientException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
                "",
            )
        }
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
        Assertions.assertTrue(
            (errorForInvalidInput.response as ClientError<*>).body!!.toString()
                .contains(errorMessage),
        )
    }

    @Test
    fun `post a company with empty string document reference`() {
        val oneInvalidSfdrDataset = apiAccessor.testDataProviderForSfdrData
            .getSpecificCompanyByNameFromPreparedFixtures(
                "Sfdr-dataset-with-empty-string-document-reference",
            )
        Assertions.assertNotNull(oneInvalidSfdrDataset)
        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        val errorForInvalidInput = assertThrows<ClientException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
                "",
            )
        }
        Assertions.assertTrue(errorForInvalidInput.message!!.contains(errorCode400))
    }
}
