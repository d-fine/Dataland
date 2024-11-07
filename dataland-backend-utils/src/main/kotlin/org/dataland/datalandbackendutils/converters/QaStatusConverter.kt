package org.dataland.datalandbackendutils.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Converts QaStatus entries in Entities to database values and vice versa
 */
@Converter(autoApply = true)
class QaStatusConverter : AttributeConverter<QaStatus, String> {
    override fun convertToDatabaseColumn(qaStatus: QaStatus?): String? = qaStatus?.toString()

    override fun convertToEntityAttribute(qaStatusAsString: String): QaStatus = QaStatus.valueOf(qaStatusAsString)
}
