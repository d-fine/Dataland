package org.dataland.datalanduserservice.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.PortfolioPayload
import org.dataland.datalanduserservice.service.PortfolioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortfolioControllerTest {
    private val mockPortfolioService = mock<PortfolioService>()
    private val mockCompanyDataController = mock<CompanyDataControllerApi>()
    private lateinit var portfolioController: PortfolioController

    private val dummyPortfolioId = UUID.randomUUID()
    private val dummyPortfolioName = "Test Portfolio"
    private val validCompanyId = "valid-company-id"
    private val invalidCompanyId = "invalid-company-id"

    private val validPortfolioPayload =
        PortfolioPayload(dummyPortfolioName, setOf(validCompanyId), setOf(DataTypeEnum.lksg))

    @BeforeEach
    fun setup() {
        reset(mockPortfolioService, mockCompanyDataController)
        doNothing().whenever(mockCompanyDataController).isCompanyIdValid(validCompanyId)
        doThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))
            .whenever(mockCompanyDataController)
            .isCompanyIdValid(invalidCompanyId)

        portfolioController = PortfolioController(mockCompanyDataController, mockPortfolioService)
    }

    @Test
    fun `test that adding a company with invalid companyId to a portfolio throws ResourceNotFoundApiException`() {
        assertThrows<ResourceNotFoundApiException> {
            portfolioController.patchPortfolio(dummyPortfolioId.toString(), invalidCompanyId)
        }
    }

    @Test
    fun `test that adding a company with valid companyId to a portfolio returns 200 response`() {
        val response =
            assertDoesNotThrow { portfolioController.patchPortfolio(dummyPortfolioId.toString(), validCompanyId) }
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `test that creating a portfolio with existing name throws throw ConflictApiException`() {
        doReturn(true).whenever(mockPortfolioService).existsPortfolioWithNameForUser(dummyPortfolioName)

        assertThrows<ConflictApiException> { portfolioController.createPortfolio(validPortfolioPayload) }
    }

    @Test
    fun `test that creating a valid portfolio returns 201 response`() {
        doReturn(false).whenever(mockPortfolioService).existsPortfolioWithNameForUser(dummyPortfolioName)
        val response = assertDoesNotThrow { portfolioController.createPortfolio(validPortfolioPayload) }
        assertEquals(HttpStatus.CREATED, response.statusCode)
    }

    @ParameterizedTest
    @ValueSource(ints = [400, 401, 402, 403, 405, 406, 407, 408, 409, 410])
    fun `test that a ClientException other than 404 is passed through the isCompanyIdValid function`(statusCode: Int) {
        doThrow(ClientException(statusCode = statusCode)).whenever(mockCompanyDataController).isCompanyIdValid(validCompanyId)

        val exception = assertThrows<ClientException> { portfolioController.createPortfolio(validPortfolioPayload) }
        assertEquals(statusCode, exception.statusCode)
    }

    @Test
    fun `test that replacing an nonexistent portfolio throws PortfolioNotFoundApiException`() {
        doReturn(false).whenever(mockPortfolioService).existsPortfolioForUser(dummyPortfolioId.toString())
        assertThrows<PortfolioNotFoundApiException> {
            portfolioController.replacePortfolio(dummyPortfolioId.toString(), validPortfolioPayload)
        }
    }

    @Test
    fun `test that replacing an existing portfolio by portfolio with invalid companyId throws ResourceNotFoundException`() {
        val invalidPortfolioPayload = validPortfolioPayload.copy(companyIds = setOf(validCompanyId, invalidCompanyId))
        doReturn(true).whenever(mockPortfolioService).existsPortfolioForUser(dummyPortfolioId.toString())

        assertThrows<ResourceNotFoundApiException> {
            portfolioController.replacePortfolio(dummyPortfolioId.toString(), invalidPortfolioPayload)
        }
    }

    @Test
    fun `test that replacing an existing portfolio by a valid portfolio returns 200 response`() {
        doReturn(true).whenever(mockPortfolioService).existsPortfolioForUser(dummyPortfolioId.toString())

        val response = assertDoesNotThrow { portfolioController.replacePortfolio(dummyPortfolioId.toString(), validPortfolioPayload) }
        assertEquals(HttpStatus.OK, response.statusCode)
    }
}
