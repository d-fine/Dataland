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
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.UUID

class DataSourcingValidatorTest {
    @Mock
    private lateinit var companyDataControllerApi: CompanyDataControllerApi

    @Mock
    private lateinit var documentControllerApi: DocumentControllerApi

    @InjectMocks
    private lateinit var dataSourcingValidator: DataSourcingValidator

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this) // Initialize mocks
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

        `when`(companyDataControllerApi.postCompanyValidation(listOf(identifier)))
            .thenReturn(listOf(validationResult))

        // Act
        val result = dataSourcingValidator.validateAndGetCompanyId(identifier).getOrThrow()

        // Assert
        assertEquals(UUID.fromString(companyId), result)
        verify(companyDataControllerApi, times(1)).postCompanyValidation(listOf(identifier))
    }

    @Test
    fun `validateAndGetCompanyId should throw ResourceNotFoundApiException when company does not exist`() {
        // Arrange
        val identifier = "invalid_company_identifier"

        `when`(companyDataControllerApi.postCompanyValidation(listOf(identifier)))
            .thenReturn(emptyList())

        // Act & Assert
        val exception =
            assertThrows(ResourceNotFoundApiException::class.java) {
                dataSourcingValidator.validateAndGetCompanyId(identifier).getOrThrow()
            }

        assertEquals("The company identifier is unknown.", exception.summary)
        verify(companyDataControllerApi, times(1)).postCompanyValidation(listOf(identifier))
    }

    // Test validateDocumentId
    @Test
    fun `validateDocumentId should complete successfully when document exists`() {
        // Arrange
        val documentId = "valid_document_id"

        doNothing().`when`(documentControllerApi).checkDocument(documentId)

        // Act
        assertDoesNotThrow { dataSourcingValidator.validateDocumentId(documentId) }

        // Assert
        verify(documentControllerApi, times(1)).checkDocument(documentId)
    }

    @Test
    fun `validateDocumentId should throw ResourceNotFoundApiException when document does not exist`() {
        // Arrange
        val documentId = "invalid_document_id"

        doThrow(
            ClientException(),
        ).`when`(documentControllerApi)
            .checkDocument(documentId)

        // Act & Assert
        val exception =
            assertThrows(ResourceNotFoundApiException::class.java) {
                dataSourcingValidator.validateDocumentId(documentId)
            }

        assertEquals("Document with id $documentId not found.", exception.summary)
        verify(documentControllerApi, times(1)).checkDocument(documentId)
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
        // Arrange
        val validCompanyId = "550e8400-e29b-41d4-a716-446655440000"
        val invalidCompanyId = "invalid_company_id"
        val validReportingPeriod = "2023"
        val invalidReportingPeriod = "INVALID"

        val validDimension =
            BasicDataDimensions(
                companyId = validCompanyId,
                dataType = "sfdr",
                reportingPeriod = validReportingPeriod,
            )

        val invalidDimensions =
            listOf(
                BasicDataDimensions(
                    companyId = invalidCompanyId,
                    dataType = "invalidType",
                    reportingPeriod = validReportingPeriod,
                ),
                BasicDataDimensions(
                    companyId = validCompanyId,
                    dataType = "invalidType",
                    reportingPeriod = invalidReportingPeriod,
                ),
            )

        val dataDimensions = listOf(validDimension) + invalidDimensions

        val validCompanyInformation =
            BasicCompanyInformation(
                companyId = validCompanyId,
                companyName = "Valid Company",
                headquarters = "Headquarters",
                countryCode = "US",
                sector = "Finance",
            )

        val validResult =
            CompanyIdentifierValidationResult(
                identifier = validCompanyId,
                companyInformation = validCompanyInformation,
            )

        `when`(companyDataControllerApi.postCompanyValidation(listOf(validCompanyId))).thenAnswer {
            listOf(validResult)
        }
        `when`(companyDataControllerApi.postCompanyValidation(listOf(invalidCompanyId))).thenAnswer {
            emptyList<String>()
        }
        // Act
        val (validRequests, invalidRequests) = dataSourcingValidator.validateBulkDataRequest(dataDimensions)

        // Assert
        assertEquals(1, validRequests.size)
        assertEquals(2, invalidRequests.size)

        assertEquals(validCompanyId, validRequests.first().companyId)
        assertEquals(validReportingPeriod, validRequests.first().reportingPeriod)
        assertEquals(invalidCompanyId, invalidRequests.first().companyId)
        assertEquals(validReportingPeriod, invalidRequests.first().reportingPeriod)
        assertEquals(validCompanyId, invalidRequests[1].companyId)
        assertEquals(invalidReportingPeriod, invalidRequests[1].reportingPeriod)
    }
}
