package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService
import java.math.BigDecimal

/**
 * Shared fixtures for the split [SignificanceCheckService] test classes: the service instance under test,
 * common dummy values, the default significance thresholds, and helper functions for building test values.
 */
abstract class SignificanceCheckServiceTestFixtures {
    protected val service = SignificanceCheckService()
    protected val dummyFramework = DataTypeEnum.sfdr
    protected val dummyDataPointType = "some-datapoint-type"

    protected val baseDecimalValue: BigDecimal = BigDecimal.valueOf(100.0)
    protected val baseIntegerValue = 10

    protected val decimalRelativeThreshold = 0.5
    protected val integerAbsoluteThreshold = 5L
    protected val emptyDecimalOverrides: Map<DataTypeEnum, Map<String, Double>> = emptyMap()
    protected val emptyIntegerOverrides: Map<DataTypeEnum, Map<String, Long>> = emptyMap()
    protected val defaultThresholds =
        SignificanceCheckService.SignificanceThresholds(
            decimalRelativeThreshold = decimalRelativeThreshold,
            integerAbsoluteThreshold = integerAbsoluteThreshold,
            individualDecimalThresholds = emptyDecimalOverrides,
            individualIntegerThresholds = emptyIntegerOverrides,
        )

    protected fun createDecimalNodeWithRelativeMultiplier(
        baseValue: BigDecimal,
        multiplier: Double,
    ): JsonNode = DecimalNode(baseValue.multiply(BigDecimal.valueOf(multiplier)))

    protected fun createIntegerNodeWithAbsoluteOffset(
        baseValue: Int,
        absoluteOffset: Int,
    ): JsonNode = IntNode(baseValue + absoluteOffset)
}
