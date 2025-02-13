package org.dataland.datalandbackendutils.services.utils

import org.dataland.datalandbackendutils.converter.DocumentCategoryConverter
import org.dataland.datalandbackendutils.model.DocumentCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentCategoryConverterTest {
    private lateinit var documentCategoryConverter: DocumentCategoryConverter

    @BeforeEach
    fun setup() {
        documentCategoryConverter = DocumentCategoryConverter()
    }

    @ParameterizedTest
    @EnumSource(DocumentCategory::class)
    @NullSource
    fun `check that document categories are properly converted to strings`(category: DocumentCategory?) {
        val result =
            assertDoesNotThrow {
                documentCategoryConverter.convertToDatabaseColumn(category)
            }
        assertEquals(category.toString(), result)
    }

    @ParameterizedTest
    @EnumSource(DocumentCategory::class)
    @NullSource
    fun `check that strings are properly converted to document categories`(category: DocumentCategory?) {
        val result =
            assertDoesNotThrow {
                documentCategoryConverter.convertToEntityAttribute(category.toString())
            }
        assertEquals(category, result)
    }
}
