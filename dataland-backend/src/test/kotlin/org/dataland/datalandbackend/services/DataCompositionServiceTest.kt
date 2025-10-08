package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.specificationservice.openApiClient.model.SimpleFrameworkSpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DataCompositionServiceTest {
    companion object {
        private const val ASSEMBLED = "sfdr"
        private const val NON_ASSEMBLED = "lksg"
        private const val DATA_POINT = "test"
        private const val NON_DATA_TYPE = "dummy"
    }

    private val specificationClient = mock<SpecificationControllerApi>()
    private val inputFrameworkSpecification = "./json/frameworkTemplate/frameworkSpecification.json"
    private val inputSimpleFrameworkSpecification = "./json/frameworkTemplate/simpleFrameworkSpecification.json"
    private lateinit var dataCompositionService: DataCompositionService
    private lateinit var specificationService: SpecificationService

    private val frameworkSpecification = TestResourceFileReader.getKotlinObject<FrameworkSpecification>(inputFrameworkSpecification)
    private val simpleFrameworkSpecification =
        TestResourceFileReader
            .getKotlinObject<SimpleFrameworkSpecification>(inputSimpleFrameworkSpecification)

    @BeforeEach
    fun executeSetup() {
        doReturn(frameworkSpecification).whenever(specificationClient).getFrameworkSpecification(any())
        doThrow(ClientException()).whenever(specificationClient).getDataPointTypeSpecification(any())
        doReturn(null).whenever(specificationClient).getDataPointTypeSpecification(DATA_POINT)
        doReturn(listOf(simpleFrameworkSpecification)).whenever(specificationClient).listFrameworkSpecifications()
        specificationService = SpecificationService(specificationClient)
        specificationService.initiateSpecifications(null)
        dataCompositionService = DataCompositionService(specificationService)
    }

    @ParameterizedTest
    @CsvSource(
        "$ASSEMBLED, true",
        "$NON_ASSEMBLED, true",
        "$DATA_POINT, false",
        "$NON_DATA_TYPE, false",
    )
    fun `check that frameworks are identified correctly`(
        dataType: String,
        expectedResult: String,
    ) {
        assertEquals(expectedResult.toBoolean(), specificationService.isFramework(dataType))
    }

    @ParameterizedTest
    @CsvSource(
        "$ASSEMBLED, true",
        "$NON_ASSEMBLED, false",
        "$DATA_POINT, false",
        "$NON_DATA_TYPE, false",
    )
    fun `check that assembled frameworks are identified correctly`(
        dataType: String,
        expectedResult: String,
    ) {
        assertEquals(expectedResult.toBoolean(), specificationService.isAssembledFramework(dataType))
    }

    @ParameterizedTest
    @CsvSource(
        "$ASSEMBLED, false",
        "$NON_ASSEMBLED, true",
        "$DATA_POINT, false",
        "$NON_DATA_TYPE, false",
    )
    fun `check that non assembled frameworks are identified correctly`(
        dataType: String,
        expectedResult: String,
    ) {
        assertEquals(expectedResult.toBoolean(), specificationService.isNonAssembledFramework(dataType))
    }

    @ParameterizedTest
    @CsvSource(
        "$ASSEMBLED, false",
        "$NON_ASSEMBLED, false",
        "$DATA_POINT, true",
        "$NON_DATA_TYPE, false",
    )
    fun `check that data points are identified correctly`(
        dataType: String,
        expectedResult: String,
    ) {
        assertEquals(expectedResult.toBoolean(), specificationService.isDataPointType(dataType))
    }

    @Test
    fun `check that relevant data points are identified correctly`() {
        assertEquals(setOf(DATA_POINT), dataCompositionService.getRelevantDataPointTypes(DATA_POINT))
        val expectedResult =
            setOf(
                "extendedEnumFiscalYearDeviationDummy", "extendedDateFiscalYearEnd", "extendedCurrencyEquity", "extendedCurrencyDebt",
                "extendedCurrencyBalanceSheetTotal", "extendedCurrencyEvic",
            )
        assertEquals(expectedResult, dataCompositionService.getRelevantDataPointTypes(ASSEMBLED))
        assertThrows<InvalidInputApiException> { dataCompositionService.getRelevantDataPointTypes(NON_ASSEMBLED) }
        assertThrows<InvalidInputApiException> { dataCompositionService.getRelevantDataPointTypes(NON_DATA_TYPE) }
    }
}
