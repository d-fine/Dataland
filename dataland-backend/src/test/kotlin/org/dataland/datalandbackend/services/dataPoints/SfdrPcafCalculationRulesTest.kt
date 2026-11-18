package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.InternalStorageAdapter
import org.dataland.datalandbackend.services.SpecificationService
import org.dataland.datalandbackend.services.datapoints.DataPointCalculator
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.assertBigDecimalEquals
import org.dataland.datalandbackend.utils.createDecimalDataPointJson
import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.CalculationRule
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.math.BigDecimal
import java.util.stream.Stream

/**
 * Tests the calculation rules of the SFDR and PCAF specifications as they are shipped in the specification service,
 * together with the [DataPointCalculator]: rule ordering and fallback, skipping of rules with missing inputs or a
 * divisor of zero, and the precedence of stored values.
 */
class SfdrPcafCalculationRulesTest {
    companion object {
        private const val DATA_POINT_TYPES_PATH =
            "../dataland-specification-service/src/main/resources/specifications/dataPointTypes"
        private const val PREFIX_DECIMAL = "extendedDecimal"
        private const val SCOPE_1 = "${PREFIX_DECIMAL}Scope1GhgEmissionsInTonnes"
        private const val SCOPE_2 = "${PREFIX_DECIMAL}Scope2GhgEmissionsInTonnes"
        private const val SCOPE_3 = "${PREFIX_DECIMAL}Scope3GhgEmissionsInTonnes"
        private const val SCOPE_123 = "${PREFIX_DECIMAL}Scope1And2And3GhgEmissionsInTonnes"
        private const val REVENUE = "${PREFIX_DECIMAL}TotalRevenueInEUR"
        private const val ENTERPRISE_VALUE = "${PREFIX_DECIMAL}EnterpriseValueInEUR"
        private const val MARKET_CAP = "${PREFIX_DECIMAL}MarketCapitalizationInEUR"
        private const val BOOK_VALUE_OF_DEBT = "${PREFIX_DECIMAL}BookValueOfDebtInEUR"
        private const val MINORITIES_INTEREST = "${PREFIX_DECIMAL}MinoritiesInterestInEUR"
        private const val TOTAL_EQUITY = "${PREFIX_DECIMAL}TotalEquityInEUR"
        private const val TOTAL_EQUITY_AND_DEBT = "${PREFIX_DECIMAL}TotalEquityAndDebtInEUR"
        private const val GHG_INTENSITY = "${PREFIX_DECIMAL}GhgIntensityInTonnesPerMillionEURRevenue"
        private const val CARBON_FOOTPRINT = "${PREFIX_DECIMAL}CarbonFootprintInTonnesPerMillionEUREnterpriseValue"
        private const val SCOPE_1_AND_2 = "${PREFIX_DECIMAL}Scope1And2GhgEmissionsInTonnes"
        private const val DIVISION_FREE_METHOD = "Sum"

        val rulesByTarget: Map<String, List<CalculationRule>> by lazy {
            File(DATA_POINT_TYPES_PATH)
                .listFiles { f -> f.extension == "json" }
                .orEmpty()
                .map { defaultObjectMapper.readTree(it) }
                .filter { node -> node["frameworkOwnership"].any { it.asText() in setOf("sfdr", "pcaf") } }
                .filter { it["calculationRules"].size() > 0 }
                .associate { node ->
                    node["id"].asText() to
                        node["calculationRules"].map { rule ->
                            CalculationRule(rule["inputs"].map { it.asText() }, rule["calculationMethod"].asText())
                        }
                }
        }

        @JvmStatic
        fun allRules(): Stream<Arguments> =
            rulesByTarget.flatMap { (target, rules) -> rules.indices.map { Arguments.of(target, it) } }.stream()

        @JvmStatic
        fun allTargets(): Stream<Arguments> = rulesByTarget.keys.map { Arguments.of(it) }.stream()

        @JvmStatic
        fun divisionRules(): Stream<Arguments> =
            rulesByTarget
                .flatMap { (target, rules) -> rules.indices.map { target to it } }
                .filter { (target, index) -> rulesByTarget.getValue(target)[index].calculationMethod != DIVISION_FREE_METHOD }
                .map { (target, index) -> Arguments.of(target, index) }
                .stream()
    }

    private val dataCompositionService = mock<DataCompositionService>()
    private val internalStorageAdapter = mock<InternalStorageAdapter>()
    private val specificationService = mock<SpecificationService>()
    private val metaDataManager = mock<DataPointMetaInformationManager>()
    private val calculator = DataPointCalculator(dataCompositionService, internalStorageAdapter, specificationService, metaDataManager)

    private val companyId = "company"
    private val reportingPeriod = "2025"
    private val framework = "sfdr"
    private val dimensions = BasicDatasetDimensions(companyId, framework, reportingPeriod)

    @BeforeEach
    fun setUp() {
        doAnswer { invocation ->
            invocation.getArgument<List<String>>(0).associateWith { type ->
                DataPointTypeSpecification(
                    dataPointType = IdWithRef(id = type, ref = ""),
                    name = type,
                    businessDefinition = "",
                    dataPointBaseType = IdWithRef(id = "extendedDecimal", ref = ""),
                    usedBy = emptyList(),
                    calculationRules = emptyList(),
                )
            }
        }.whenever(specificationService).getDataPointSpecifications(any())
    }

    /**
     * Runs the calculation of [target] with the given stored values (data point type to decimal value). Types listed in
     * [deliverable] are treated as already available, as they are when a stored value exists for them.
     */
    private fun calculate(
        target: String,
        stored: Map<String, String>,
        deliverable: List<String> = emptyList(),
    ): ExtendedDataPoint<BigDecimal>? {
        val rules = rulesByTarget.getValue(target)
        doReturn((rules.flatMap { it.inputs } + target).distinct())
            .whenever(dataCompositionService)
            .getRelevantDataPointTypes(framework)
        doAnswer { invocation ->
            val requested = invocation.getArgument<Collection<String>>(0)
            mapOf(target to rules).filterKeys { it in requested }
        }.whenever(dataCompositionService).getAvailableCalculationRules(any())

        val storedById = stored.mapKeys { "id-${it.key}" }
        doReturn(
            storedById.keys.map {
                DataPointMetaInformationEntity(
                    dataPointId = it, companyId = companyId, dataPointType = it.removePrefix("id-"),
                    reportingPeriod = reportingPeriod, uploaderUserId = "user", uploadTime = 0,
                    currentlyActive = true, qaStatus = QaStatus.Accepted,
                )
            },
        ).whenever(metaDataManager).getActiveDataPointMetaInformationList(any<List<DataPointDimensions>>())
        doReturn(
            stored.entries.associate { (type, value) ->
                "id-$type" to UploadedDataPoint(createDecimalDataPointJson(value), type, companyId, reportingPeriod)
            },
        ).whenever(internalStorageAdapter).getDataPoints(any(), any())

        val result =
            calculator
                .getCalculatedData(listOf(dimensions), mapOf(dimensions to deliverable), "correlation-id")
                .getOrDefault(dimensions, emptyList())
                .firstOrNull { it.dataPointType == target }
        return result?.let { defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(it.dataPoint) }
    }

    private fun values(
        inputs: List<String>,
        value: String,
    ) = inputs.associateWith { value }

    private fun hasApplicableRule(
        target: String,
        stored: Set<String>,
    ) = rulesByTarget.getValue(target).any { stored.containsAll(it.inputs) }

    @ParameterizedTest
    @MethodSource("allRules")
    fun `check that every rule is calculated from its own inputs and carries a formula comment`(
        target: String,
        ruleIndex: Int,
    ) {
        val result = calculate(target, values(rulesByTarget.getValue(target)[ruleIndex].inputs, "2"))

        assertNotNull(result?.value)
        assertTrue(result!!.comment!!.contains("calculated using the following formula"))
    }

    @ParameterizedTest
    @MethodSource("allRules")
    fun `check that a rule with a missing input is skipped and the field stays empty if no other rule applies`(
        target: String,
        ruleIndex: Int,
    ) {
        val inputs = rulesByTarget.getValue(target)[ruleIndex].inputs
        inputs.forEach { missing ->
            val stored = values(inputs - missing, "2")
            val result = calculate(target, stored)
            if (hasApplicableRule(target, stored.keys)) {
                assertNotNull(result)
            } else {
                assertNull(result, "$target must not be calculated without $missing")
            }
        }
    }

    @ParameterizedTest
    @MethodSource("divisionRules")
    fun `check that a rule with a divisor of zero is skipped without an error`(
        target: String,
        ruleIndex: Int,
    ) {
        assertNull(calculate(target, values(rulesByTarget.getValue(target)[ruleIndex].inputs, "0")))
    }

    @ParameterizedTest
    @MethodSource("allTargets")
    fun `check that a stored value takes precedence over the calculation`(target: String) {
        val allInputs = rulesByTarget.getValue(target).flatMap { it.inputs }.distinct()

        assertNotNull(calculate(target, values(allInputs, "2")))
        assertNull(calculate(target, values(allInputs, "2"), deliverable = listOf(target)))
    }

    @Test
    fun `check that the enterprise value rule is tried before the total equity and debt rule for the carbon footprint`() {
        val result =
            calculate(
                CARBON_FOOTPRINT,
                mapOf(
                    SCOPE_123 to "60", ENTERPRISE_VALUE to "1000000", TOTAL_EQUITY_AND_DEBT to "2000000",
                    MARKET_CAP to "3000000", BOOK_VALUE_OF_DEBT to "1", MINORITIES_INTEREST to "1",
                ),
            )
        assertBigDecimalEquals("60", result?.value)
    }

    @Test
    fun `check that the carbon footprint falls back to the sum of the scopes and the raw enterprise value inputs`() {
        val result =
            calculate(
                CARBON_FOOTPRINT,
                mapOf(
                    SCOPE_1 to "10", SCOPE_2 to "20", SCOPE_3 to "30", MARKET_CAP to "500000",
                    BOOK_VALUE_OF_DEBT to "300000", MINORITIES_INTEREST to "200000",
                ),
            )
        assertBigDecimalEquals("60", result?.value)
    }

    @Test
    fun `check that the carbon footprint falls back to the total equity and debt inputs`() {
        val stored = mapOf(SCOPE_123 to "60", TOTAL_EQUITY to "400000", BOOK_VALUE_OF_DEBT to "600000")
        assertBigDecimalEquals("60", calculate(CARBON_FOOTPRINT, stored)?.value)
    }

    @Test
    fun `check that the carbon footprint skips an enterprise value of zero and uses the next rule`() {
        val stored = mapOf(SCOPE_123 to "60", ENTERPRISE_VALUE to "0", TOTAL_EQUITY_AND_DEBT to "1000000")
        assertBigDecimalEquals("60", calculate(CARBON_FOOTPRINT, stored)?.value)
    }

    @Test
    fun `check that the ghg intensity prefers the stored scope 1 and 2 and 3 value over the sum of the scopes`() {
        val stored = mapOf(SCOPE_123 to "60", SCOPE_1 to "1", SCOPE_2 to "1", SCOPE_3 to "1", REVENUE to "1000000")
        assertBigDecimalEquals("60", calculate(GHG_INTENSITY, stored)?.value)
    }

    @Test
    fun `check that the ghg intensity falls back to the sum of the scopes`() {
        val stored = mapOf(SCOPE_1 to "10", SCOPE_2 to "20", SCOPE_3 to "30", REVENUE to "1000000")
        assertBigDecimalEquals("60", calculate(GHG_INTENSITY, stored)?.value)
    }

    @Test
    fun `check that the ghg intensity stays empty without revenue`() {
        assertNull(calculate(GHG_INTENSITY, mapOf(SCOPE_123 to "60", SCOPE_1 to "10", SCOPE_2 to "20", SCOPE_3 to "30")))
    }

    @Test
    fun `check that enterprise value and scope sums are calculated with the correct units`() {
        assertBigDecimalEquals(
            "1230",
            calculate(ENTERPRISE_VALUE, mapOf(MARKET_CAP to "1000", BOOK_VALUE_OF_DEBT to "200", MINORITIES_INTEREST to "30"))?.value,
        )
        assertEquals(
            0,
            BigDecimal(30).compareTo(calculate(SCOPE_1_AND_2, mapOf(SCOPE_1 to "10", SCOPE_2 to "20"))?.value),
        )
    }
}
