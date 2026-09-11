package org.dataland.datalandqaservice.services

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService.ValueType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SignificanceCheckServiceResolveValueTypeTest : SignificanceCheckServiceTestFixtures() {
    @Test
    fun `extendedDecimal resolves to DECIMAL`() {
        assertEquals(ValueType.DECIMAL, service.resolveValueType("extendedDecimal"))
    }

    @Test
    fun `extendedInteger resolves to INTEGER`() {
        assertEquals(ValueType.INTEGER, service.resolveValueType("extendedInteger"))
    }

    @Test
    fun `extendedEnumYesNo resolves to BOOLEAN`() {
        assertEquals(ValueType.BOOLEAN, service.resolveValueType("extendedEnumYesNo"))
    }

    @Test
    fun `unknown base type id resolves to UNSUPPORTED`() {
        assertEquals(ValueType.UNSUPPORTED, service.resolveValueType("extendedEnumYesNoNa"))
        assertEquals(ValueType.UNSUPPORTED, service.resolveValueType("extendedCurrency"))
        assertEquals(ValueType.UNSUPPORTED, service.resolveValueType("plainDate"))
        assertEquals(ValueType.UNSUPPORTED, service.resolveValueType("some-unknown-type"))
    }
}
