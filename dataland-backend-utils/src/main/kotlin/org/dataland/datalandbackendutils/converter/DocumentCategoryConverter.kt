package org.dataland.datalandbackendutils.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandbackendutils.model.DocumentCategory

/**
 * Converts DocumentCategory entries in Entities to database values and vice versa
 */
@Converter
class DocumentCategoryConverter : AttributeConverter<DocumentCategory?, String> {
    override fun convertToDatabaseColumn(documentCategory: DocumentCategory?): String = documentCategory.toString()

    override fun convertToEntityAttribute(documentCategoryAsString: String): DocumentCategory =
        DocumentCategory
            .valueOf(documentCategoryAsString)
}
