package org.dataland.datalanduserservice.converter

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataTypeEnumConverterTest {
    private lateinit var dataTypeEnumConverter: DataTypeEnumConverter

    @BeforeEach
    fun setup() {
        dataTypeEnumConverter = DataTypeEnumConverter()
    }

    @ParameterizedTest
    @EnumSource(DataTypeEnum::class)
    fun `check that data types are properly converted to strings`(dataType: DataTypeEnum) {
        val result =
            assertDoesNotThrow {
                dataTypeEnumConverter.convertToDatabaseColumn(dataType)
            }
        assertEquals(dataType.toString(), result)
    }

    @ParameterizedTest
    @EnumSource(DataTypeEnum::class)
    @NullSource
    fun `check that strings are properly converted to data types`(dataType: DataTypeEnum?) {
        val result =
            assertDoesNotThrow {
                dataTypeEnumConverter.convertToEntityAttribute(dataType.toString())
            }
        assertEquals(dataType, result)
    }
}
