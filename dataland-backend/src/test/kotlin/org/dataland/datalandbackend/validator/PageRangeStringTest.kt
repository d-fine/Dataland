package org.dataland.datalandbackend.validator

import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PageRangeStringTest {

    @Test
    fun `should throw exception for invalid page range`() {
        assertThrows<IllegalArgumentException>() {
            ExtendedDocumentReference(page = "5-3", fileReference = "someFileReference")
        }
    }

    @Test
    fun `should create instance for valid page range`() {
        val document = ExtendedDocumentReference(page = "5-10", fileReference = "someFileReference")
        document.validatePageRange()
    }
}
