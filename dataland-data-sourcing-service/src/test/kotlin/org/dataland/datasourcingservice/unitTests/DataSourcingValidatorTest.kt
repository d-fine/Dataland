package org.dataland.datasourcingservice.unitTests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.BasicDataDimensions
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanddocumentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.datalanddocumentmanager.openApiClient.infrastructure.ClientException
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DataSourcingValidatorTest {
    private var mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()

    private var mockDocumentControllerApi = mock<DocumentControllerApi>()

    private var dataSourcingValidator = DataSourcingValidator(mockCompanyDataControllerApi, mockDocumentControllerApi)

    @BeforeEach
    fun setUp() {
        reset(mockCompanyDataControllerApi, mockDocumentControllerApi)
    }

    // Test validateAndGetCompanyId
    @Test
    fun `validateAndGetCompanyId should return valid UUID when company exists`() {
        // Arrange
        val identifier = "valid_company_identifier"
        val companyId = "550e8400-e29b-41d4-a716-446655440000"

        val companyInformation =
            BasicCompanyInformation(
                companyId = companyId,
                companyName = "Test Company",
                headquarters = "Headquarters",
                countryCode = "US",
                sector = "Finance",
                lei = "12345",
            )
        val validationResult = CompanyIdentifierValidationResult(identifier, companyInformation)

        whenever(mockCompanyDataControllerApi.postCompanyValidation(listOf(identifier)))
            .thenReturn(listOf(validationResult))

        // Act
        val result = dataSourcingValidator.validateAndGetCompanyId(identifier).getOrThrow()

        // Assert
        assertEquals(UUID.fromString(companyId), result)
        verify(mockCompanyDataControllerApi, times(1)).postCompanyValidation(listOf(identifier))
    }

    @Test
    fun `validateAndGetCompanyId should throw ResourceNotFoundApiException when company does not exist`() {
        // Arrange
        val identifier = "invalid_company_identifier"

        whenever(mockCompanyDataControllerApi.postCompanyValidation(listOf(identifier)))
            .thenReturn(emptyList())

        // Act & Assert
        val exception =
            assertThrows(ResourceNotFoundApiException::class.java) {
                dataSourcingValidator.validateAndGetCompanyId(identifier).getOrThrow()
            }

        assertEquals("The company identifier is unknown.", exception.summary)
        verify(mockCompanyDataControllerApi, times(1)).postCompanyValidation(listOf(identifier))
    }

    // Test validateDocumentId
    @Test
    fun `validateDocumentId should complete successfully when document exists`() {
        // Arrange
        val documentId = "valid_document_id"

        doNothing().whenever(mockDocumentControllerApi).checkDocument(documentId)

        // Act
        assertDoesNotThrow { dataSourcingValidator.validateDocumentId(documentId) }

        // Assert
        verify(mockDocumentControllerApi, times(1)).checkDocument(documentId)
    }

    @Test
    fun `validateDocumentId should throw ResourceNotFoundApiException when document does not exist`() {
        // Arrange
        val documentId = "invalid_document_id"

        doThrow(
            ClientException(),
        ).whenever(mockDocumentControllerApi)
            .checkDocument(documentId)

        // Act & Assert
        val exception =
            assertThrows(ResourceNotFoundApiException::class.java) {
                dataSourcingValidator.validateDocumentId(documentId)
            }

        assertEquals("Document with id $documentId not found.", exception.summary)
        verify(mockDocumentControllerApi, times(1)).checkDocument(documentId)
    }

    // Test validateReportingPeriod
    @Test
    fun `validateReportingPeriod should return success when reporting period is valid`() {
        // Arrange
        val reportingPeriod = "2023"
        // Act
        val result = dataSourcingValidator.validateReportingPeriod(reportingPeriod).getOrThrow()

        // Assert
        assertEquals(reportingPeriod, result)
    }

    @Test
    fun `validateReportingPeriod should return failure when reporting period is invalid`() {
        // Arrange
        val reportingPeriod = "INVALID"

        // Act & Assert
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                dataSourcingValidator.validateReportingPeriod(reportingPeriod).getOrThrow()
            }

        assertEquals("Invalid reporting period: $reportingPeriod", exception.message)
    }

    // Test validateBulkDataRequest
    @Test
    fun `validateBulkDataRequest should partition valid and invalid requests`() {
        // Helpers for test data
        fun makeDimension(
            companyId: String,
            dataType: String,
            period: String,
        ) = BasicDataDimensions(companyId, dataType, period)

        val validCompanyId = "550e8400-e29b-41d4-a716-446655440000"
        val invalidCompanyId = "invalid_company_id"
        val validReportingPeriod = "2023"
        val invalidReportingPeriod = "INVALID"

        val dataDimensions =
            listOf(
                makeDimension(validCompanyId, "sfdr", validReportingPeriod),
                makeDimension(invalidCompanyId, "invalidType", validReportingPeriod),
                makeDimension(validCompanyId, "invalidType", invalidReportingPeriod),
            )

        val validCompanyInformation =
            BasicCompanyInformation(
                validCompanyId, "Valid Company", "Headquarters", "US", "Finance",
            )

        val validResult =
            CompanyIdentifierValidationResult(
                identifier = validCompanyId,
                companyInformation = validCompanyInformation,
            )

        whenever(mockCompanyDataControllerApi.postCompanyValidation(listOf(validCompanyId))).thenAnswer {
            listOf(validResult)
        }
        whenever(mockCompanyDataControllerApi.postCompanyValidation(listOf(invalidCompanyId))).thenAnswer {
            emptyList<String>()
        }

        // Act
        val (validRequests, invalidRequests) = dataSourcingValidator.validateBulkDataRequest(dataDimensions)

        // Assert
        assertEquals(1, validRequests.size)
        assertEquals(2, invalidRequests.size)
        with(validRequests.first()) {
            assertEquals(validCompanyId, companyId)
            assertEquals(validReportingPeriod, reportingPeriod)
        }
        with(invalidRequests) {
            assertEquals(invalidCompanyId, this[0].companyId)
            assertEquals(validReportingPeriod, this[0].reportingPeriod)
            assertEquals(validCompanyId, this[1].companyId)
            assertEquals(invalidReportingPeriod, this[1].reportingPeriod)
        }
    }
}
