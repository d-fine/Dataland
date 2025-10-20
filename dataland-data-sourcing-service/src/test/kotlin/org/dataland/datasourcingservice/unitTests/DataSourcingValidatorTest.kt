package org.dataland.datasourcingservice.unitTests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanddocumentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.datalanddocumentmanager.openApiClient.infrastructure.ClientException
import org.dataland.datasourcingservice.model.request.BulkDataRequest
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DataSourcingValidatorTest {
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()

    private val mockDocumentControllerApi = mock<DocumentControllerApi>()

    private val dataSourcingValidator = DataSourcingValidator(mockCompanyDataControllerApi, mockDocumentControllerApi)

    @BeforeEach
    fun setUp() {
        reset(mockCompanyDataControllerApi, mockDocumentControllerApi)
    }

    @Test
    fun `validateDocumentId should complete successfully when document exists`() {
        val documentId = "valid_document_id"

        doNothing().whenever(mockDocumentControllerApi).checkDocument(documentId)

        Assertions.assertDoesNotThrow { dataSourcingValidator.validateDocumentId(documentId) }

        verify(mockDocumentControllerApi, times(1)).checkDocument(documentId)
    }

    @Test
    fun `validateDocumentId should throw ResourceNotFoundApiException when document does not exist`() {
        val documentId = "invalid_document_id"

        doThrow(
            ClientException(),
        ).whenever(mockDocumentControllerApi)
            .checkDocument(documentId)

        val exception =
            assertThrows<ResourceNotFoundApiException> {
                dataSourcingValidator.validateDocumentId(documentId)
            }

        Assertions.assertEquals("Document with id $documentId not found.", exception.summary)
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

        Assertions.assertEquals(UUID.fromString(companyId), uuid)
    }

    @Test
    fun `validateSingleDataRequest throws when company not found`() {
        val identifier = "NOTFOUND"
        val dataType = "sfdr"
        val reportingPeriod = "2022"
        whenever(mockCompanyDataControllerApi.postCompanyValidation(listOf(identifier)))
            .thenReturn(
                listOf(
                    CompanyIdentifierValidationResult(
                        identifier = identifier,
                        companyInformation = null,
                    ),
                ),
            )

        val request = SingleRequest(identifier, dataType, reportingPeriod, null)

        val ex =
            assertThrows<ResourceNotFoundApiException> {
                dataSourcingValidator.validateSingleDataRequest(request)
            }
        Assertions.assertTrue(ex.message == "The company identifier $identifier does not exist on Dataland.")
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
            assertThrows<InvalidInputApiException> {
                dataSourcingValidator.validateSingleDataRequest(request)
            }
        Assertions.assertTrue(ex.message == "The data type $dataType is invalid.")
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
            assertThrows<InvalidInputApiException> {
                dataSourcingValidator.validateSingleDataRequest(request)
            }
        Assertions.assertTrue(ex.message == "The reporting period $reportingPeriod is invalid.")
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
        val validationResult = CompanyIdentifierValidationResult(validCompanyId, validCompanyInfo)
        val companyIds = listOf(validCompanyId, invalidCompanyId)

        whenever(mockCompanyDataControllerApi.postCompanyValidation(companyIds))
            .thenReturn(listOf(validationResult))

        val req =
            BulkDataRequest(
                companyIdentifiers = companyIds.toSet(),
                dataTypes = setOf(validType, invalidType),
                reportingPeriods = setOf(validPeriod, invalidPeriod),
            )

        val result = dataSourcingValidator.validateBulkDataRequest(req)

        val companyValidation = result.companyIdValidation
        Assertions.assertEquals(UUID.fromString(validCompanyId), companyValidation[validCompanyId])
        assertNull(companyValidation[invalidCompanyId])

        val dataTypeValidation = result.dataTypeValidation
        Assertions.assertTrue(dataTypeValidation[validType] == true)
        Assertions.assertTrue(dataTypeValidation[invalidType] == false)

        val reportingPeriodValidation = result.reportingPeriodValidation
        Assertions.assertTrue(reportingPeriodValidation[validPeriod] == true)
        Assertions.assertTrue(reportingPeriodValidation[invalidPeriod] == false)
    }
}
