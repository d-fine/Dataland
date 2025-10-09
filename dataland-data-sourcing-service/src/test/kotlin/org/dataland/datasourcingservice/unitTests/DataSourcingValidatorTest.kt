package org.dataland.datasourcingservice.unitTests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanddocumentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.datalanddocumentmanager.openApiClient.infrastructure.ClientException
import org.dataland.datasourcingservice.model.request.BulkDataRequest
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
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

    @Test
    fun `validateSingleDataRequest returns companyId if all are valid`() {
        val identifier = "company-id"
        val companyId = "7cd80dd6-4387-4ee6-879b-83377111dcb6"
        val dataType = "sfdr"
        val reportingPeriod = "2021"
        val companyInfo =
            BasicCompanyInformation(
                companyId = companyId,
                companyName = "Sample",
                headquarters = "H",
                countryCode = "XX",
                sector = "S",
            )
        val validationResult = CompanyIdentifierValidationResult(identifier, companyInfo)
        whenever(mockCompanyDataControllerApi.postCompanyValidation(listOf(identifier)))
            .thenReturn(listOf(validationResult))

        val request = SingleRequest(identifier, dataType, reportingPeriod, null)
        val uuid = dataSourcingValidator.validateSingleDataRequest(request)

        assertEquals(UUID.fromString(companyId), uuid)
    }

    @Test
    fun `validateSingleDataRequest throws when company not found`() {
        val identifier = "NOTFOUND"
        val dataType = "sfdr"
        val reportingPeriod = "2022"
        whenever(mockCompanyDataControllerApi.postCompanyValidation(listOf(identifier)))
            .thenReturn(emptyList())

        val request = SingleRequest(identifier, dataType, reportingPeriod, null)

        val ex =
            assertThrows(IllegalArgumentException::class.java) {
                dataSourcingValidator.validateSingleDataRequest(request)
            }
        assertTrue(ex.message!!.contains(identifier))
    }

    @Test
    fun `validateSingleDataRequest throws when data type is invalid`() {
        val identifier = "company"
        val companyId = "407243e4-d002-4e0b-bcfd-c7cff6b1c719"
        val dataType = "notAType"
        val reportingPeriod = "2023"
        val companyInfo =
            BasicCompanyInformation(
                companyId = companyId,
                companyName = "ABC Inc",
                headquarters = "HQ",
                countryCode = "YY",
                sector = "S",
            )
        val validationResult = CompanyIdentifierValidationResult(identifier, companyInfo)
        whenever(mockCompanyDataControllerApi.postCompanyValidation(listOf(identifier)))
            .thenReturn(listOf(validationResult))

        val request = SingleRequest(identifier, dataType, reportingPeriod, null)

        val ex =
            assertThrows(IllegalArgumentException::class.java) {
                dataSourcingValidator.validateSingleDataRequest(request)
            }
        assertTrue(ex.message!!.contains("not recognized"))
    }

    @Test
    fun `validateSingleDataRequest throws when reporting period is invalid`() {
        val identifier = "company"
        val companyId = "cc0aac9c-53b4-48b2-8693-22ab853be4c4"
        val dataType = "sfdr"
        val reportingPeriod = "INVALID_PERIOD"
        val companyInfo =
            BasicCompanyInformation(
                companyId = companyId,
                companyName = "DEF Corp",
                headquarters = "HQ2",
                countryCode = "ZZ",
                sector = "S2",
            )
        val validationResult = CompanyIdentifierValidationResult(identifier, companyInfo)
        whenever(mockCompanyDataControllerApi.postCompanyValidation(listOf(identifier)))
            .thenReturn(listOf(validationResult))

        val request = SingleRequest(identifier, dataType, reportingPeriod, null)

        val ex =
            assertThrows(IllegalArgumentException::class.java) {
                dataSourcingValidator.validateSingleDataRequest(request)
            }
        assertTrue(ex.message!!.contains("not valid"))
    }

    @Test
    fun `validateBulkDataRequest returns correct validation results`() {
        val validCompanyId = "537d2dbc-b1f6-4096-ad23-cee97fa69491"
        val invalidCompanyId = "no-company"
        val validType = "sfdr"
        val invalidType = "foo"
        val validPeriod = "2020"
        val invalidPeriod = "BAD"

        val validCompanyInfo =
            BasicCompanyInformation(
                companyId = validCompanyId,
                companyName = "Valids",
                headquarters = "HQ",
                countryCode = "A",
                sector = "B",
            )
        val validCompanyResult = CompanyIdentifierValidationResult(validCompanyId, validCompanyInfo)
        val companyIds = listOf(validCompanyId, invalidCompanyId)
        // Setup: Only validCompanyId returns a UUID, the rest is not found
        whenever(mockCompanyDataControllerApi.postCompanyValidation(companyIds))
            .thenReturn(listOf(validCompanyResult))

        val req =
            BulkDataRequest(
                companyIdentifiers = companyIds.toSet(),
                dataTypes = setOf(validType, invalidType),
                reportingPeriods = setOf(validPeriod, invalidPeriod),
            )

        val result = dataSourcingValidator.validateBulkDataRequest(req)

        // Check company validation mapping
        val companyValidation = result.companyIdValidation.associate { it.entries.first().toPair() }
        assertEquals(UUID.fromString(validCompanyId), companyValidation[validCompanyId])
        assertNull(companyValidation[invalidCompanyId])

        // Check data type validation mapping
        val dataTypeValidation = result.dataTypeValidation.associate { it.entries.first().toPair() }
        assertTrue(dataTypeValidation[validType] == true)
        assertTrue(dataTypeValidation[invalidType] == false)

        // Check reporting period validation mapping
        val reportingPeriodValidation = result.reportingPeriodValidation.associate { it.entries.first().toPair() }
        assertTrue(reportingPeriodValidation[validPeriod] == true)
        assertTrue(reportingPeriodValidation[invalidPeriod] == false)
    }
}
