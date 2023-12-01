package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvalidSfdrRequestTests {
    private val apiAccessor = ApiAccessor()

    @Test
    fun `post a company with invalid Sfdr currency data`() {
        val oneInvalidSfdrDataset = apiAccessor.testDataProviderForSfdrData
            .getSpecificCompanyByNameFromPreparedFixtures("Sfdr-dataset-with-invalid-currency-input")
        Assertions.assertNotNull(oneInvalidSfdrDataset)
        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        val errorForInvalidInput = assertThrows<ServerException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
                "",
            )
        }
        Assertions.assertTrue(errorForInvalidInput.message!!.contains("Server error : 500 "))
        Assertions.assertTrue(
            errorForInvalidInput.message!!.contains(
                "MethodArgumentNotValidException: " +
                    "Validation failed for argument",
            ),
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
        val errorForInvalidInput = assertThrows<ServerException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
                "",
            )
        }
        Assertions.assertTrue(errorForInvalidInput.message!!.contains("Server error : 500 "))
        Assertions.assertTrue(
            errorForInvalidInput.message!!.contains(
                "MethodArgumentNotValidException: " +
                    "Validation failed for argument",
            ),
        )
    }

    @Test
    fun `post a company with invalid negative extended data point long`() {
        val oneInvalidSfdrDataset = apiAccessor.testDataProviderForSfdrData
            .getSpecificCompanyByNameFromPreparedFixtures("Sfdr-dataset-with-invalid-negative-long-input")
        Assertions.assertNotNull(oneInvalidSfdrDataset)
        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        val errorForInvalidInput = assertThrows<ServerException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
                "",
            )
        }
        Assertions.assertTrue(errorForInvalidInput.message!!.contains("Server error : 500 "))
        Assertions.assertTrue(
            errorForInvalidInput.message!!.contains(
                "MethodArgumentNotValidException: " +
                    "Validation failed for argument",
            ),
        )
    }

    @Test
    fun `post a company with invalid percentage value`() {
        val oneInvalidSfdrDataset = apiAccessor.testDataProviderForSfdrData
            .getSpecificCompanyByNameFromPreparedFixtures("Sfdr-dataset-with-invalid-percentage-input")
        Assertions.assertNotNull(oneInvalidSfdrDataset)
        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()
        val errorForInvalidInput = assertThrows<ServerException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
                "",
            )
        }
        Assertions.assertTrue(errorForInvalidInput.message!!.contains("Server error : 500 "))
        Assertions.assertTrue(
            errorForInvalidInput.message!!.contains(
                "MethodArgumentNotValidException: " +
                    "Validation failed for argument",
            ),
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
        val errorForInvalidInput = assertThrows<ServerException> {
            apiAccessor.sfdrUploaderFunction(
                companyInformation.actualStoredCompany.companyId, oneInvalidSfdrDataset!!.t,
                "",
            )
        }
        Assertions.assertTrue(errorForInvalidInput.message!!.contains("Server error : 500 "))
    }
}
